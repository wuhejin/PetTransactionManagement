package com.whj.Controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.whj.Config.AlipayConfig;
import com.whj.Enum.OrderPay;
import com.whj.Enum.OrderStatus;
import com.whj.Pojo.Ord;
import com.whj.Pojo.User;
import com.whj.Service.OrdService;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/order")
public class OrdController {
    @Autowired
    private AlipayConfig alipayConfig;
    @Autowired
    private OrdService ordService;

    //跳转到生成订单页面
    @GetMapping("/Ord")
    public String Ord(){
        return "/front/order-settle";
    }



    //创建订单
    @RequestMapping("/createOrd")
    @ResponseBody
    public String createOrd(HttpSession session){
        return ordService.createOrd(session);
    }

    //跳转到我的订单
    @RequestMapping("/my_order")
    public String my_order(){
        return "/front/my-order";
    }

    //查询全部订单
    @PostMapping("/selectAll")
    @ResponseBody
    public List<Ord> selectAll(){
        User user = (User) SecurityUtils.getSubject().getPrincipals().getPrimaryPrincipal();
        return ordService.selectAll(user.getId());
    }

    //查看订单详情
    @RequestMapping("/ord/{ord_id}")
    public String ord(@PathVariable("ord_id") String ord_id, Model model){
        User user = (User) SecurityUtils.getSubject().getPrincipals().getPrimaryPrincipal();
        Ord ord = ordService.ord(user.getId(),ord_id);
        model.addAttribute("ord",ord);
        return "/front/order-detail";
    }

    //用户手动退款订单
    @PostMapping("/cancelOrder")
    @ResponseBody
    public String cancelOrder(@RequestBody String ord_id ) throws AlipayApiException {
        User user = (User) SecurityUtils.getSubject().getPrincipals().getPrimaryPrincipal();
        System.out.println(ord_id);
        ord_id = ord_id.substring(1,ord_id.length()-1);
        return ordService.Order(user.getId(),ord_id,OrderStatus.CANCELBYUSER.getDesc());
    }
    //用户支付同步调用
    /*
     * 支付宝扫码支付成功返回，一般只做展示
     * 业务逻辑处理请勿在该执行
     * 后面去掉，直接跳转支付成功页面
     * */
    @RequestMapping(value = "/return", method = RequestMethod.GET)
    public String payreturn(HttpServletResponse response, HttpServletRequest request, Model model) throws IOException {
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();
        //获取支付宝GET过来反馈信息
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
            try {
                valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            params.put(name, valueStr);
        }
        boolean signVerified = false; //调用SDK验证签名
        try {
            signVerified = AlipaySignature.rsaCheckV1(params, alipayConfig.alipay_public_key, alipayConfig.charset, alipayConfig.sign_type);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        //——请在这里编写您的程序（以下代码仅作参考）——
        if (signVerified) {
            String out_trade_no = null;
            try {
                //商户订单号
                out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                e.printStackTrace();}
            User user = (User) SecurityUtils.getSubject().getPrincipals().getPrimaryPrincipal();
            Ord ord = ordService.ord(user.getId(),out_trade_no);
            model.addAttribute("ord",ord);
            return "/front/order-detail";

        } else {
            out.println("支付失败");
            return "";
        }
    }
    //用户支付异步调用
    /*
     * 异步通知地址 notify_url，通过 POST 请求的形式将支付结果作为参数通知到商户系统
     * 程序执行完后必须通过 PrintWriter 类打印输出"success"（不包含引号）。
     * 如果商户反馈给支付宝的字符不是 success 这7个字符，支付宝服务器会不断重发通知，直到超过 24 小时 22 分钟。一般情况下，25 小时以内完成 8 次通知（通知的间隔频率一般是：4m,10m,10m,1h,2h,6h,15h）；
     * 该页面不能在本机电脑测试，请到服务器上做测试。请确保外部可以访问该页面。
     * 建议该页面只做支付成功的业务逻辑处理，退款的处理请以调用退款查询接口的结果为准。
     * */
    @RequestMapping(value = "/notify", method = RequestMethod.POST)
    public void notify(HttpServletResponse response, HttpServletRequest request) throws IOException, AlipayApiException {
        System.out.println("notify");
        ordService.notify(response,request);
    }

    //支付订单
    @RequestMapping(value = "/pay/{ord_id}/{price_sum}", method = RequestMethod.GET)
    public void pay(HttpServletResponse response,@PathVariable("ord_id")String ord_id,@PathVariable("price_sum")String price_sum ) throws IOException, AlipayApiException {
        ordService.pay(response,ord_id,price_sum);
    }
    //用户确认收货
    @PostMapping("/finishOrder")
    @ResponseBody
    public String finishOrder(@RequestBody String ord_id){
        User user = (User) SecurityUtils.getSubject().getPrincipals().getPrimaryPrincipal();
        ord_id = ord_id.substring(1,ord_id.length()-1);
        return ordService.finishOrder(user.getId(),ord_id, OrderStatus.COMPLETE.getDesc());
    }

}
