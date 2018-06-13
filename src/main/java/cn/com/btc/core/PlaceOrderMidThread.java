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

public class PlaceOrderMidThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(PlaceOrderMidThread.class);
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
    private final double buy;
    private FcoinApi fcoinApi = FcoinApiHandler.getInstance();

    public PlaceOrderMidThread(String symbol, OrderList orderList, Decimal decimal) {
        this.symbol = symbol.replace("-", "");
        this.decimal = decimal;
        String[] s = symbol.split("-");
        this.coin = s[0];
        this.currency = s[1];
        this.orderList = orderList;
        this.isFt = "ft".equalsIgnoreCase(currency) || "ft".equalsIgnoreCase(coin);
        this.level = ConfigHandler.getConf("btc." + symbol + ".level", "L20");
        this.num = Double.valueOf(ConfigHandler.getConf("btc." + symbol + ".num", "1"));
        this.profit = Double.valueOf(ConfigHandler.getConf("btc." + symbol + ".profit", "0.001"));
        this.discount = Double.valueOf(ConfigHandler.getConf("btc." + symbol + ".discount", "0.5"));
        this.buy = Double.valueOf(ConfigHandler.getConf("btc." + symbol + ".buy", "0.5"));
        this.sleepTime = Long.valueOf(ConfigHandler.getConf("btc.sleep", "1000"));
        this.minNum = Double.valueOf(ConfigHandler.getConf("btc." + symbol + ".minnum", "0"));
        setName(this.symbol + "-mid-place-order-thread");
    }

    @Override
    public void run() {
        if (decimal == null || decimal.getAmount_decimal() <= 0 || decimal.getPrice_decimal() <= 0) {
            logger.error("get decimal is error!!! " + decimal);
            return;
        }
        logger.info("start " + getName());
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
                    if (Math.abs(now - ts) < 2000) {
                        List<Number> asks = (List<Number>) map.get("asks");
                        List<Number> bids = (List<Number>) map.get("bids");
                        if (asks.size() > 0 && bids.size() > 0) {
                            price = (asks.get(0).doubleValue() + bids.get(0).doubleValue()) / 2;
                            num = Math.min(asks.get(1).doubleValue(), bids.get(1).doubleValue()) * discount;
                            flag = orderList.isAvail(price);
                            if (!flag) {
                                logger.info("fluctuate is full!!! price=" + price + " order=" + orderList.getOrders());
                            }
                        }
                    } else {
                        logger.info("time is error!!! dur=" + Math.abs(now - ts));
                    }
                    Order buy = null;
                    if (flag) {
                        num = Math.min(num, this.num);
                        double newPrice = price * (1 - profit * this.buy);
                        BigDecimal b1 = new BigDecimal(newPrice);
                        newPrice = b1.setScale(decimal.getPrice_decimal(), BigDecimal.ROUND_DOWN).doubleValue();
                        num = AccountCache.getNum(currency, num, newPrice);
                        BigDecimal b = new BigDecimal(num);
                        num = b.setScale(decimal.getAmount_decimal(), BigDecimal.ROUND_DOWN).doubleValue();
                        if (num >= minNum && num > 0) {
                            String id = (String) fcoinApi.orders(symbol, "buy", "limit", newPrice + "", num + "");
                            if (StringUtils.isNotBlank(id)) {
                                buy = new Order(id, symbol, newPrice, num);
                                orderList.addBuyOrder(buy, "mid");
                                AccountCache.deleteAvailable(currency, num, newPrice);
                                logger.info(buy.toString());
                            } else {
                                logger.error("buy order is fail!!!");
                                flag = false;
                            }
                        } else {
                            logger.error("num is error!!! " + num);
                            flag = false;
                        }
                    }
                    if (flag) {
                        int count = 0;
                        boolean f = true;
                        double newPrice = price * (1 + profit * (1 - this.buy));
                        BigDecimal b = new BigDecimal(newPrice);
                        newPrice = b.setScale(decimal.getPrice_decimal(), BigDecimal.ROUND_UP).doubleValue();
                        while (f) {
                            try {
                                double nn = AccountCache.getNum(this.coin, num, 1d);
                                if (nn == num) {
                                    String id1 = (String) fcoinApi.orders(symbol, "sell", "limit", newPrice + "", num + "");
                                    if (StringUtils.isNotBlank(id1)) {
                                        f = false;
                                        Order sell = new Order(id1, symbol, newPrice, num);
                                        logger.info(sell.toString());
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
                } else {
                    logger.info("order size=" + orderList.getTotal() + " nowsize=" + orderList.getOrders().size());
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
        logger.info("exit " + getName());
    }
}
