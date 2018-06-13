package cn.com.btc.core;

import cn.com.btc.ft.FcoinApi;
import cn.com.btc.utils.MyDateFormat;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class PlaceOrderSellThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(PlaceOrderSellThread.class);
    private final String symbol;
    private final OrderList orderList;
    private final String level;
    private final double num;
    private final double profit;
    private final String currency;
    private final String coin;
    private final long sleepTime;
    private final boolean isFt;
    private final double discount;
    private final double minNum;
    private final Decimal decimal;
    private FcoinApi fcoinApi = FcoinApiHandler.getInstance();

    public PlaceOrderSellThread(String symbol, OrderList orderList, Decimal decimal) {
        this.symbol = symbol.replace("-", "");
        this.decimal = decimal;
        String[] s = symbol.split("-");
        this.coin = s[0];
        this.currency = s[1];
        this.orderList = orderList;
        this.isFt = "ft".equalsIgnoreCase(currency) || "ft".equalsIgnoreCase(coin);
        this.level = ConfigHandler.getConf("btc." + symbol + ".level", "L20");
        this.num = Double.valueOf(ConfigHandler.getConf("btc." + symbol + ".num", "1"));
        this.profit = 1 - Double.valueOf(ConfigHandler.getConf("btc." + symbol + ".profit", "0.001"));
        this.discount = Double.valueOf(ConfigHandler.getConf("btc." + symbol + ".discount", "0.5"));
        this.sleepTime = Long.valueOf(ConfigHandler.getConf("btc.sleep", "1000"));
        this.minNum = Double.valueOf(ConfigHandler.getConf("btc." + symbol + ".minnum", "0"));
        setName(this.symbol + "-place-order-thread");
    }

    @Override
    public void run() {
        if (decimal == null || decimal.getAmount_decimal() <= 0 || decimal.getPrice_decimal() <= 0) {
            logger.error("get decimal is error!!! " + decimal);
            return;
        }
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
                        List<Number> bids = (List<Number>) map.get("bids");
                        if (bids.size() > 0) {
                            price = bids.get(0).doubleValue();
                            num += bids.get(1).doubleValue() * discount;
                            flag = orderList.isAvail(price);
                        }
                    }
                    Order buy = null;
                    if (flag) {
                        num = Math.min(num, this.num);
                        num = AccountCache.getNum(currency, num, price);
                        BigDecimal b = new BigDecimal(num);
                        num = b.setScale(decimal.getAmount_decimal(), BigDecimal.ROUND_DOWN).doubleValue();
                        if (num > minNum) {
                            String id = (String) fcoinApi.orders(symbol, "sell", "limit", price + "", num + "");
                            if (StringUtils.isNotBlank(id)) {
                                buy = new Order(id, symbol, price, num);
                                orderList.addBuyOrder(buy);
                                AccountCache.deleteAvailable(coin, num, price);
                            } else {
                                flag = false;
                            }
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
                                    BigDecimal b = new BigDecimal(newPrice);
                                    newPrice = b.setScale(decimal.getPrice_decimal(), BigDecimal.ROUND_UP).doubleValue();
                                    String id1 = (String) fcoinApi.orders(symbol, "buy", "limit", newPrice + "", num + "");
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
            } catch (Throwable t) {
                logger.error("place order error!!!", t);
            } finally {
                try {
                    Thread.sleep(sleepTime);
                } catch (Throwable e) {
                }
            }
        }
    }
}