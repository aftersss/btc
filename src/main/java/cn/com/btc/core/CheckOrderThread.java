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
                List<String> sellIdList = new ArrayList<>();
                List<String> buyIdList = new ArrayList<>();
                if (orders != null && orders.size() > 0) {
                    List<Map<String, String>> mapList = (List<Map<String, String>>) fcoinApi.queryOrderList(symbol, "submitted", "10", null, limit);
                    if (mapList != null) {
                        for (Map<String, String> map : mapList) {
                            String id = map.get("id");
                            String type = map.get("side");
                            if ("buy".equalsIgnoreCase(type)) {
                                buyIdList.add(id);
                            } else {
                                sellIdList.add(id);
                            }
                        }
                    }
                    if (orders.size() > 0) {
                        List<Map<String, String>> mapList1 = (List<Map<String, String>>) fcoinApi.queryOrderList(symbol, "partial_filled", "10", null, limit);
                        if (mapList1 != null) {
                            for (Map<String, String> map : mapList1) {
                                String id = map.get("id");
                                String type = map.get("side");
                                if ("buy".equalsIgnoreCase(type)) {
                                    buyIdList.add(id);
                                } else {
                                    sellIdList.add(id);
                                }
                            }
                        }
                    }
                    if (orders.size() > 0) {
                        for (Pair pair : orders.values()) {
                            if (!buyIdList.contains(pair.getBuy().getId()) && !sellIdList.contains(pair.getSell().getId())) {
                                orderList.removeOrder(pair.getBuy().getId());
                                Writer.addFinish(pair);
                            }
                        }
                    }
                }
                Thread.sleep(sleepTime * 30);
            } catch (Throwable t) {
                logger.error("check order error!!!", t);
            }
        }
    }
}
