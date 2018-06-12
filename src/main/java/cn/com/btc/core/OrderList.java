package cn.com.btc.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderList {
    private final int total;
    private final String symbol;
    private final List<Order> orders = new ArrayList<>();
    private final String intervalKey;
    private final double interval;

    public OrderList(String symbol, int total) {
        this.total = total;
        this.symbol = symbol;
        this.intervalKey = "btc." + symbol + ".interval";
        this.interval = Double.valueOf(ConfigHandler.getConf(intervalKey, "1"));
    }

    public boolean isSaturated() {
        return total <= orders.size();
    }

    public List<Order> getOrders() {
        return Collections.unmodifiableList(orders);
    }

    public boolean isAval(double price) {
        for (Order order : orders) {
            if (Math.abs(order.getPrice() - price) <= interval) {
                return false;
            }
        }
        return true;
    }

    public double getInterval() {
        return interval;
    }

    public void addOrder(Order order) {
        orders.add(order);
    }

    public void removeOrder(String id) {
        int index = -1;
        for (int i = 0; i < orders.size(); i++) {
            if (id.equalsIgnoreCase(orders.get(i).getId())) {
                index = i;
            }
        }
        if (index >= 0) {
            orders.remove(index);
        }
    }

    public void removeOrder(Order order) {
        orders.remove(order);
    }
}
