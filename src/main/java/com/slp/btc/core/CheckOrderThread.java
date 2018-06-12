package com.slp.btc.core;

public class CheckOrderThread extends Thread {
    private final String type;
    private final OrderList orderList;

    public CheckOrderThread(String type, OrderList orderList) {
        this.type = type;
        this.orderList = orderList;
        setName(type + "-check-order-thread");
    }

    @Override
    public void run() {
        super.run();
    }
}
