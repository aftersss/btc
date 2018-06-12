package cn.com.btc.core;

public class Pair {
    private Order buy;
    private Order sell;

    public Pair(Order buy, Order sell) {
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
}
