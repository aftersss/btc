package cn.com.btc.core;

import cn.com.btc.ft.FcoinApi;
import cn.com.btc.utils.MyDateFormat;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class PlaceOrderThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(PlaceOrderThread.class);
    private final String symbol;
    private final OrderList orderList;
    private final String level;
    private final double num;
    private final double profit;
    private final String currency;
    private final long sleepTime;
    private FcoinApi fcoinApi = FcoinApiHandler.getInstance();

    public PlaceOrderThread(String symbol, OrderList orderList) {
        this.symbol = symbol.replace("-", "");
        this.currency = symbol.split("-")[1];
        this.orderList = orderList;
        this.level = ConfigHandler.getConf("btc." + symbol + ".level", "L20");
        this.num = Double.valueOf(ConfigHandler.getConf("btc." + symbol + ".num", "1"));
        this.profit = Double.valueOf(ConfigHandler.getConf("btc." + symbol + ".profit", "0.001")) + 1;
        this.sleepTime = Long.valueOf(ConfigHandler.getConf("btc.sleep", "1000"));
        setName(this.symbol + "-place-order-thread");
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (!orderList.isSaturated()) {
                    boolean flag = false;
                    double price = 0d;
                    double num = 0d;
                    Map<String, Object> map = (Map<String, Object>) fcoinApi.marketDepth(level, symbol);
                    long ts = ((Number) map.get("ts")).longValue();
                    long now = MyDateFormat.getLongTime();
                    if (ts < now && now - ts < 1000) {
                        List<Number> asks = (List<Number>) map.get("asks");
                        for (int i = 0; i < asks.size(); i++) {
                            if (i % 2 == 0) {
                                price = asks.get(i).doubleValue();
                            } else {
                                num += asks.get(i).doubleValue();
                                if (!orderList.isAval(price) || num >= 2 * this.num) {
                                    break;
                                }
                                flag = true;
                            }
                        }
                    }
                    String id = null;
                    if (flag) {
                        double n = Math.min(num, this.num);
                        n = AccountSyncThread.getNum(currency, n, price);
                        id = (String) fcoinApi.orders(symbol, "buy", "limit", price + "", n + "");
                    }
                    double subNum = 0d;
                    if (StringUtils.isNotBlank(id)) {
                        while (true) {
                            Thread.sleep(200L);
                            Map<String, Object> result = (Map<String, Object>) fcoinApi.getOrder(id);
                            if (result != null) {
                                String state = (String) result.get("state");
                                double filled_amount_str = Double.valueOf(StringUtils.defaultIfBlank((String) result.get("filled_amount"), "0.0"));
                                double fill_fees = Double.valueOf(StringUtils.defaultIfBlank((String) result.get("fill_fees"), "0.0"));
                                if ("filled".equalsIgnoreCase(state)) {
                                    subNum = filled_amount_str - fill_fees;
                                    break;
                                }
                                Map<String, Object> map1 = (Map<String, Object>) fcoinApi.marketDepth(level, symbol);
                                List<Number> asks1 = (List<Number>) map1.get("asks");
                                int index = Math.min(8, asks1.size());
                                double nowP = asks1.get(index).doubleValue();
                                if (nowP - price > orderList.getInterval()) {
                                    int count = 0;
                                    while (!fcoinApi.orderSubmitCancel(id) && count < 10) {
                                        count++;
                                        Thread.sleep(sleepTime);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    if (subNum > 0.0d) {
                        String id1 = null;
                        do {
                            id1 = (String) fcoinApi.orders(symbol, "sell", "limit", (price * profit) + "", subNum + "");
                            Thread.sleep(sleepTime);
                        } while (StringUtils.isBlank(id1));
                        Order order = new Order(id1, price * profit, subNum);
                        orderList.addOrder(order);
                    }
                }
                Thread.sleep(sleepTime);
            } catch (Throwable t) {
                logger.error("place order error!!!", t);
            }
        }
    }
}
