package com.whj.Enum;

//订单状态
public enum OrderStatus {

    //管理员退回，用户退款，管理员取消，正常，入库，发货，完成交易,申请退款，退款成功
    BACKBYADMIN(-3),CANCELBYUSER(-2),CANCELBYAMDIN(-1),NORMAL(0),WAREHOU(1),DELIVER(2),COMPLETE(3),REFUND(4),REFUNDSUCCESS(-3);

    private int desc;

    OrderStatus(int desc) {
        this.desc = desc;
    }

    public int getDesc(){
        return desc;
    }
}
