package com.slp.btc.core;

public class OrderList {
    private final int total;
    private int count = 0;

    public OrderList(int total) {
        this.total = total;
    }

    public boolean isSaturated() {
        return total > count;
    }
}
