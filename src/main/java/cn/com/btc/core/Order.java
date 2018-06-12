package cn.com.btc.core;

public class Order {
    private final String id;
    private double price;
    private double num;

    public Order(String id, double price, double num) {
        this.id = id;
        this.price = price;
        this.num = num;
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
}
