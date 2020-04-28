package com.whj.Enum;

//订单支付状态
public enum OrderPay {

    //0未支付，1已支付
    UNPAID(0),PAID(1);


    private int desc;

    OrderPay(int desc) {
        this.desc = desc;
    }

    public int getDesc(){
        return desc;
    }

}
