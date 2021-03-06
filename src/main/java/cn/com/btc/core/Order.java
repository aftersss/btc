package cn.com.btc.core;

import cn.com.btc.utils.MyDateFormat;

public class Order {
    private final String id;
    private final String symbol;
    private double price;
    private double num;
    private long time;
    private boolean isFinsih;

    public Order(String id, String symbol, double price, double num) {
        this.id = id;
        this.symbol = symbol;
        this.price = price;
        this.num = num;
        this.time = MyDateFormat.getLongTime();
        this.isFinsih = false;
    }

    public void finish() {
        this.isFinsih = true;
    }

    public boolean isFinish() {
        return isFinsih;
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

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", symbol='" + symbol + '\'' +
                ", price=" + price +
                ", num=" + num +
                ", time=" + time +
                ", isFinsih=" + isFinsih +
                '}';
    }
}
