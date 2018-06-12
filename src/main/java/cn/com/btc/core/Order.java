package cn.com.btc.core;

import cn.com.btc.utils.MyDateFormat;

public class Order {
    private final String id;
    private final String symbol;
    private double price;
    private double num;
    private long time;

    public Order(String id, String symbol, double price, double num) {
        this.id = id;
        this.symbol = symbol;
        this.price = price;
        this.num = num;
        this.time = MyDateFormat.getLongTime();
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getNum() {
        return num;
    }

    public void setNum(double num) {
        this.num = num;
    }

    public String getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public long getTime() {
        return time;
    }
}
