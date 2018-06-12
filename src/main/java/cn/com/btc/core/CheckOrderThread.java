package cn.com.btc.core;

import cn.com.btc.ft.FcoinApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class CheckOrderThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(CheckOrderThread.class);
    private final String symbol;
    private final OrderList orderList;
    private final String currency;
    private final long sleepTime;
    private FcoinApi fcoinApi = FcoinApiHandler.getInstance();

    public CheckOrderThread(String symbol, OrderList orderList) {
        this.symbol = symbol.replace("-", "");
        this.currency = symbol.split("-")[1];
        this.orderList = orderList;
        this.sleepTime = Long.valueOf(ConfigHandler.getConf("btc.sleep", "1000"));
        setName(this.symbol + "-check-order-thread");
    }

    @Override
    public void run() {
        int count = 0;
        while (true) {
            try {
                if (count >= 250) {
                    count = 0;
                    List<Order> orders = orderList.getOrders();
                    for (Order order : orders) {
                        Map<String, Object> result = (Map<String, Object>) fcoinApi.getOrder(order.getId());
                        if (result != null) {
                            String state = (String) result.get("state");
                            if ("filled".equalsIgnoreCase(state)) {
                                orderList.removeOrder(order.getId());
                            }
                        }
                        Thread.sleep(sleepTime);
                    }
                } else {
                    List<Order> orders = orderList.getOrders();
                    Order order = orders.get(orders.size() - 1);
                    Map<String, Object> result = (Map<String, Object>) fcoinApi.getOrder(order.getId());
                    if (result != null) {
                        String state = (String) result.get("state");
                        if ("filled".equalsIgnoreCase(state)) {
                            orderList.removeOrder(order.getId());
                        }
                    }
                }
                count++;
                Thread.sleep(sleepTime);
            } catch (Throwable t) {
                logger.error("check order error!!!", t);
            }
        }
    }
}
