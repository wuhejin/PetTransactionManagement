package com.whj.Service.Impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.whj.Config.AlipayConfig;
import com.whj.Enum.OrderPay;
import com.whj.Enum.OrderStatus;
import com.whj.Mapper.OrdMapper;
import com.whj.Mapper.ShopcartMapper;
import com.whj.Pojo.Cart;
import com.whj.Pojo.Ord;
import com.whj.Pojo.User;
import com.whj.Service.OrdService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class OrdServiceImpl implements OrdService {

    @Autowired
    private AlipayConfig alipayConfig;

    @Autowired
    private OrdMapper ordMapper;

    @Autowired
    private ShopcartMapper shopcartMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    @Transactional
    public String createOrd(HttpSession session) {
        Ord ord = new Ord();
        User user = (User) SecurityUtils.getSubject().getPrincipals().getPrimaryPrincipal();
        List<Cart> carts = shopcartMapper.selectCart(user.getId());
        Double price_sum = 0.0;
        for (Cart cart : carts) {
            price_sum += (cart.getPet().getSelling_price() * cart.getNum());
        }
        ord.setPrice_sum(price_sum);
        ord.setUser_id(user.getId());
        ord.setStatus_ord(OrderStatus.NORMAL.getDesc());
        ord.setStatus_pay(OrderPay.UNPAID.getDesc());
        ord.setShipaddress(user.getAddress());
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("YYYYMMddHHmmss");
        ord.setOrd_id(dateFormat.format(date) + RandomStringUtils.randomNumeric(6));
        DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ord.setCreate_time(dateFormat1.format(date));
        int result = ordMapper.createOrd(ord);
        if (result > 0) {
            redisTemplate.delete("ordList" + user.getId());
            for (Cart cart : carts) {
                ordMapper.insertOrdPet(ord.getId(), cart.getPet().getPet_id(), cart.getNum());
            }
            shopcartMapper.clear(user.getId());
            session.setAttribute("shopcart", shopcartMapper.selectShopcart(user.getId()));
            return ord.getOrd_id();
        }
        return "flase";
    }

    @Override
    public List<Ord> selectAll(int user_id) {
        //ordList所有订单缓存
        //ord单个订单缓存
        List<Ord> ordList = (List<Ord>) redisTemplate.opsForValue().get("ordList" + user_id);
        if (ordList == null) {
            ordList = ordMapper.selectAll(user_id);
            redisTemplate.opsForValue().set("ordList" + user_id, ordList);
        }
        return ordList;
    }

    @Override
    public Ord ord(int user_id, String ord_id) {
        Ord ord = (Ord) redisTemplate.opsForValue().get("ord" + ord_id);
        if (ord == null) {
            ord = ordMapper.ord(user_id, ord_id);
            redisTemplate.opsForValue().set("ord" + ord_id, ord);
        }
        return ord;
    }

    /*
     * 支付宝退款请求
     * out_trade_no 订单号 必填
     * out_request_no 退款请求号 部分退款专用（每次请求号需不同） 选填（查询用到）
     * refund_amount 退款的金额 必填
     * refund_reason 退款的原因说明 选填
     * */
    @Override
    public String Order(int user_id, String ord_id, int status_ord) throws AlipayApiException {
        // 初始化的AlipayClient
        AlipayClient apAlipayClient
                = new DefaultAlipayClient(alipayConfig.gatewayUrl,
                alipayConfig.app_id, alipayConfig.merchant_private_key, "json",
                alipayConfig.charset, alipayConfig.alipay_public_key, alipayConfig.sign_type);
        AlipayTradeRefundRequest alipayRequest = new AlipayTradeRefundRequest();
        Ord ord = ord(user_id, ord_id);
        alipayRequest.setBizContent("{\"out_trade_no\":\"" + ord_id + "\","
                + "\"refund_amount\":\"" + ord.getPrice_sum() + "\"}");
//                + "\"refund_reason\":\"" + map.get("refund_reason") + "\","
//                + "\"out_request_no\":\"" + map.get("out_request_no") + "\"}");
        AlipayTradeRefundResponse aliPayResponse = apAlipayClient.execute(alipayRequest);
        if ("Y".equals(aliPayResponse.getFundChange())) {
            ordMapper.Order(user_id, ord_id, status_ord);
            redisTemplate.delete("ordList" + user_id);
            redisTemplate.delete("ord" + ord_id);
            return "success";
        }
        return "false";
    }


    @Override
    public void pay(HttpServletResponse response, String ord_id, String price_sum) throws IOException, AlipayApiException {
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();
        AlipayTradePagePayRequest aliPayRequest = new AlipayTradePagePayRequest();
        aliPayRequest.setReturnUrl(alipayConfig.return_url);
        aliPayRequest.setNotifyUrl(alipayConfig.notify_url);
        Double sum = Double.parseDouble(price_sum);
        DecimalFormat df = new DecimalFormat("0.00");
        sum = Double.parseDouble(df.format(sum));
        String subject = "宠物";
        aliPayRequest.setBizContent("{"
                + "\"out_trade_no\":\"" + ord_id + "\","
                + "\"total_amount\":\"" + sum + "\","
                + "\"subject\":\"" + subject + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\""
                + "}");
        String result;
        AlipayClient apAlipayClient
                = new DefaultAlipayClient(alipayConfig.gatewayUrl,
                alipayConfig.app_id, alipayConfig.merchant_private_key, "json",
                alipayConfig.charset, alipayConfig.alipay_public_key, alipayConfig.sign_type);
        AlipayTradePagePayResponse aliPayResponse = apAlipayClient.pageExecute(aliPayRequest);
        result = aliPayResponse.getBody();
        out.println(result);
    }

    @Override
    public void notify(HttpServletResponse response, HttpServletRequest request) throws IOException, AlipayApiException {
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            System.out.println(name + ":" + valueStr);
            //乱码解决，这段代码在出现乱码时使用
//            valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }
        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayConfig.alipay_public_key, alipayConfig.charset, alipayConfig.sign_type); //调用SDK验证签名
        if (signVerified) {//验证成功
            //商户订单号
            String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"), "UTF-8");
            //支付宝交易号
            String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"), "UTF-8");
            //交易状态
            String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"), "UTF-8");
            try {
                // 用来判定是退款还是付款
                String buyer_pay_amount = new String(request.getParameter("buyer_pay_amount").getBytes("ISO-8859-1"), "UTF-8");
                System.out.println(buyer_pay_amount);
            } catch (Exception e) {
                System.out.println("退款进入");
                //必须输出"success"给支付宝，
                out.println("success");
                return;
            }
            // 用户在交易中支付的金额
            String buyer_pay_amount = new String(request.getParameter("buyer_pay_amount").getBytes("ISO-8859-1"), "UTF-8");
            // 交易付款时间
            String gmt_payment = new String(request.getParameter("gmt_payment").getBytes("ISO-8859-1"), "UTF-8");
            /*
             * 交易完成状态，不能再退款
             * 如果商品不允许退款，交易成功状态变为交易完成状态
             * 退款日期超过可退款期限后（如三个月可退款），支付宝系统订单转为交易完成
             * */
            if (trade_status.equals("TRADE_FINISHED")) {
                redisTemplate.delete(redisTemplate.keys("ordList" + "*"));
                redisTemplate.delete("ord" + out_trade_no);
                // 交易结束时间
                ordMapper.payOrder(out_trade_no, OrderPay.PAID.getDesc());
            }
            /*
             * 交易成功状态，三个月内可退款
             * 扫码成功trade_status是TRADE_SUCCESS
             * 部分退款时也会调用 ,trade_status 也是TRADE_SUCCESS
             * */
            else if (trade_status.equals("TRADE_SUCCESS")) {
                redisTemplate.delete(redisTemplate.keys("ordList" + "*"));
                redisTemplate.delete("ord" + out_trade_no);
                ordMapper.payOrder(out_trade_no, OrderPay.PAID.getDesc());
            }
            //必须输出"success"给支付宝，
            out.println("success");

        } else {//验证失败
            System.out.println("fail");
            out.println("fail");
            //调试用，写文本函数记录程序运行情况是否正常
            String sWord = AlipaySignature.getSignCheckContentV1(params);
            alipayConfig.logResult(sWord);
        }
    }

    @Override
    public String finishOrder(Integer id, String ord_id, int desc) {
        ordMapper.Order(id, ord_id, desc);
        redisTemplate.delete("ordList" + id);
        redisTemplate.delete("ord" + ord_id);
        return "success";
    }

}
