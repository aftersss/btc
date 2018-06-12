package cn.com.btc.core;

import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class OrderList {
    private final int total;
    private final String symbol;
    private final Map<String, Pair<Order, Order>> orders = new HashMap<>();
    private final double fluctuate;
    private final int fluctuatesize;

    public OrderList(String symbol, int total) {
        this.total = total;
        this.symbol = symbol;
        this.fluctuate = Double.valueOf(ConfigHandler.getConf("btc." + symbol + ".fluctuate", "0.05"));
        this.fluctuatesize = Integer.valueOf(ConfigHandler.getConf("btc." + symbol + ".fluctuatesize", "1"));
    }

    public boolean isSaturated() {
        return total <= orders.size();
    }

    public Map<String, Pair<Order, Order>> getOrders() {
        return Collections.unmodifiableMap(orders);
    }

    public void addBuyOrder(Order buy) {
        orders.put(buy.getId(), Pair.of(buy, null));
    }

    public void addSellOrder(String buyid, Order sell) {
        Pair<Order, Order> pair = orders.get(buyid);
        pair.setValue(sell);
        Writer.addOrder(pair);
    }

    public void removeOrder(String id) {
        orders.remove(id);
    }

    public boolean isAvail(double price) {
        int count = 0;
        for (Pair<Order, Order> pair : orders.values()) {
            if (Math.abs(pair.getLeft().getPrice() - price) / price < fluctuate) {
                count++;
                if (count > fluctuatesize) {
                    return false;
                }
            }
        }
        return true;
    }

    public int getTotal() {
        return total;
    }
}
