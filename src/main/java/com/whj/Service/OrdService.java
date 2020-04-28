package com.whj.Service;

import com.alipay.api.AlipayApiException;
import com.whj.Pojo.Ord;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface OrdService {

    //创建订单
    public String createOrd(HttpSession session);
    //查询全部订单
    public List<Ord> selectAll(int user_id);
    //查询订单详情
    public Ord ord(int user_id,String ord_id);
    //用户退款订单
    public String Order(int user_id,String ord_id,int status_ord) throws AlipayApiException;
    //订单支付
    public void pay(HttpServletResponse response, String ord_id,String price_sum) throws IOException, AlipayApiException;
    //支付异步响应
    public void notify(HttpServletResponse response, HttpServletRequest request) throws IOException, AlipayApiException;
    //用户完成交易
    public String finishOrder(Integer id, String ord_id, int desc);
}
