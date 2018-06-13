package cn.com.btc.core;

public class Pair {
    private String type;
    private Order buy;
    private Order sell;

    public Pair(String type, Order buy, Order sell) {
        this.type = type;
        this.buy = buy;
        this.sell = sell;
    }

    public Order getBuy() {
        return buy;
    }

    public void setBuy(Order buy) {
        this.buy = buy;
    }

    public Order getSell() {
        return sell;
    }

    public void setSell(Order sell) {
        this.sell = sell;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
