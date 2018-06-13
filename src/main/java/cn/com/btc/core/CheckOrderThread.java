package cn.com.btc.core;

import cn.com.btc.ft.FcoinApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CheckOrderThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(CheckOrderThread.class);
    private final String symbol;
    private final OrderList orderList;
    private final long sleepTime;
    private final String limit;
    private FcoinApi fcoinApi = FcoinApiHandler.getInstance();

    public CheckOrderThread(String symbol, OrderList orderList) {
        this.symbol = symbol.replace("-", "");
        this.orderList = orderList;
        this.sleepTime = Long.valueOf(ConfigHandler.getConf("btc.sleep", "1000"));
        this.limit = (orderList.getTotal() * 2) + "";
        setName(this.symbol + "-check-order-thread");

    }

    @Override
    public void run() {
        while (!ShutdownHook.isShutDown()) {
            try {
                Map<String, Pair> orders = orderList.getOrders();
                for (Pair pair : orders.values()) {
                    if (pair.getSell() == null || pair.getBuy() == null) {
                        continue;
                    }
                    if (!pair.getBuy().isFinish()) {
                        Map<String, String> buyMap = (Map<String, String>) fcoinApi.getOrder(pair.getBuy().getId());
                        if (buyMap == null) {
                            continue;
                        }
                        if ("filled".equalsIgnoreCase(buyMap.get("state"))) {
                            orderList.removeOrder(pair.getBuy().getId());
                            Writer.addFinish(pair);
                            pair.getBuy().finish();
                        }
                    }
                    Thread.sleep(sleepTime * 4);
                    if (!pair.getSell().isFinish()) {
                        Map<String, String> sellMap = (Map<String, String>) fcoinApi.getOrder(pair.getSell().getId());
                        if (sellMap == null) {
                            continue;
                        }
                        if ("filled".equalsIgnoreCase(sellMap.get("state"))) {
                            orderList.removeOrder(pair.getBuy().getId());
                            Writer.addFinish(pair);
                            pair.getSell().finish();
                        }
                    }
                    Thread.sleep(sleepTime * 4);
                }
            } catch (Throwable t) {
                logger.error("check order error!!!", t);
            } finally {
                try {
                    Thread.sleep(sleepTime * 10);
                } catch (Throwable e) {
                }
            }
        }
    }
}
