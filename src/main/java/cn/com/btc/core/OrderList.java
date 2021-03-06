package cn.com.btc.core;

import java.util.*;

public class OrderList {
    private final int total;
    private final String symbol;
    private Map<String, Pair> orders = new HashMap<>();
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

    public Map<String, Pair> getOrders() {
        Map<String, Pair> map = new HashMap<>();
        map.putAll(orders);
        return map;
    }

    public void addBuyOrder(Order buy) {
        orders.put(buy.getId(), new Pair("buy", buy, null));
    }

    public void addBuyOrder(Order buy, String type) {
        orders.put(buy.getId(), new Pair(type, buy, null));
    }

    public void addSellOrder(String buyid, Order sell) {
        Pair pair = orders.get(buyid);
        pair.setSell(sell);
        Writer.addOrder(pair);
    }

    public void addSellOrder(Order sell) {
        orders.put(sell.getId(), new Pair("sell", null, sell));
    }

    public void addBuyOrder(String sellid, Order sell) {
        Pair pair = orders.get(sellid);
        pair.setBuy(sell);
        Writer.addOrder(pair);
    }

    public void removeOrder(String id) {
        orders.remove(id);
    }

    public boolean isAvail(double price) {
        int count = 0;
        for (Pair pair : orders.values()) {
            if (Math.abs(pair.getBuy().getPrice() - price) / price < fluctuate) {
                count++;
                if (count >= fluctuatesize) {
                    return false;
                }
            }
        }
        return true;
    }

    public int getTotal() {
        return total;
    }

    public void setOrders(Map<String, Pair> orders) {
        this.orders = orders;
    }
}
