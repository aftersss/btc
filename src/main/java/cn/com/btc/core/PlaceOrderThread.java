package cn.com.btc.core;

import cn.com.btc.ft.FcoinApi;
import cn.com.btc.utils.MyDateFormat;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.Calendar;
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
    private final String coin;
    private final long sleepTime;
    private final boolean isFt;
    private FcoinApi fcoinApi = FcoinApiHandler.getInstance();

    public PlaceOrderThread(String symbol, OrderList orderList) {
        this.symbol = symbol.replace("-", "");
        String[] s = symbol.split("-");
        this.coin = s[0];
        this.currency = s[1];
        this.orderList = orderList;
        this.isFt = "ft".equalsIgnoreCase(currency) || "ft".equalsIgnoreCase(coin);
        this.level = ConfigHandler.getConf("btc." + symbol + ".level", "L20");
        this.num = Double.valueOf(ConfigHandler.getConf("btc." + symbol + ".num", "1"));
        this.profit = Double.valueOf(ConfigHandler.getConf("btc." + symbol + ".profit", "0.001")) + 1;
        this.sleepTime = Long.valueOf(ConfigHandler.getConf("btc.sleep", "1000"));
        setName(this.symbol + "-place-order-thread");
    }

    @Override
    public void run() {
        while (!ShutdownHook.isShutDown()) {
            try {
                if (!orderList.isSaturated()) {
                    if (isFt) {
                        Calendar calendar = Calendar.getInstance();
                        int min = calendar.get(Calendar.MINUTE);
                        if (min == 59 || (min >= 0 && min <= 3)) {
                            Thread.sleep(4 * sleepTime);
                            continue;
                        }
                    }
                    boolean flag = false;
                    double price = 0d;
                    double num = 0d;
                    Map<String, Object> map = (Map<String, Object>) fcoinApi.marketDepth(level, symbol);
                    long ts = ((Number) map.get("ts")).longValue();
                    long now = MyDateFormat.getLongTime();
                    if (Math.abs(now - ts) < 1000) {
                        List<Number> asks = (List<Number>) map.get("asks");
                        if (asks.size() > 0) {
                            price = asks.get(0).doubleValue();
                            num += asks.get(1).doubleValue();
                            flag = orderList.isAvail(price);
                        }
                    }
                    Order buy = null;
                    if (flag) {
                        num = Math.min(num, this.num);
                        num = AccountCache.getNum(currency, num, price);
                        num = Math.round(num * 10000 - 0.5) / 10000D;
                        String id = (String) fcoinApi.orders(symbol, "buy", "limit", price + "", num + "");
                        if (StringUtils.isNotBlank(id)) {
                            buy = new Order(id, symbol, price, num);
                            orderList.addBuyOrder(buy);
                            AccountCache.deleteAvailable(currency, num, price);
                        } else {
                            flag = false;
                        }
                    }
                    if (flag) {
                        int count = 0;
                        boolean f = true;
                        while (f) {
                            try {
                                double nn = AccountCache.getNum(this.coin, num, 1d);
                                if (nn == num) {
                                    double newPrice = price * profit;
                                    newPrice = Math.round(newPrice * 100 + 0.5) / 100D;
                                    String id1 = (String) fcoinApi.orders(symbol, "sell", "limit", newPrice + "", num + "");
                                    if (StringUtils.isNotBlank(id1)) {
                                        f = false;
                                        Order sell = new Order(id1, symbol, newPrice, num);
                                        orderList.addSellOrder(buy.getId(), sell);
                                        break;
                                    }
                                }
                                if (ShutdownHook.isShutDown()) {
                                    count++;
                                    if (count > 2) {
                                        logger.error("A buy is error!!! id=" + buy.getId());
                                        break;
                                    }
                                }
                                Thread.sleep(2 * sleepTime);
                            } catch (Throwable e) {
                                logger.error("", e);
                            }
                        }
                    }
                }
                Thread.sleep(4 * sleepTime);
            } catch (Throwable t) {
                logger.error("place order error!!!", t);
            }
        }
    }
}
