package com.slp.btc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlaceOrderThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(PlaceOrderThread.class);
    private final String type;
    private final OrderList orderList;

    public PlaceOrderThread(String type, OrderList orderList) {
        this.type = type;
        this.orderList = orderList;
        setName(type + "-place-order-thread");
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (!orderList.isSaturated()) {

                }
                Thread.sleep(1000L);
            } catch (Throwable t) {
                logger.error("place order error!!!", t);
            }
        }
    }
}
