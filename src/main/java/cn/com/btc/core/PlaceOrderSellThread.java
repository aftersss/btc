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
        setName(this.symbol + "-sell-place-order-thread");
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
                        List<Number> bids = (List<Number>) map.get("bids");
                        if (bids.size() > 0) {
                            price = bids.get(0).doubleValue();
                            num += bids.get(1).doubleValue() * discount;
                            flag = orderList.isAvail(price);
                            if (!flag) {
                                logger.info("fluctuate is full!!! price=" + price);
                            }
                        }
                    } else {
                        logger.info("time is error!!! dur=" + Math.abs(now - ts));
                    }
                    Order sell = null;
                    if (flag) {
                        num = Math.min(num, this.num);
                        num = AccountCache.getNum(coin, num, 1d);
                        BigDecimal b = new BigDecimal(num);
                        num = b.setScale(decimal.getAmount_decimal(), BigDecimal.ROUND_DOWN).doubleValue();
                        if (num >= minNum && num > 0) {
                            String id = (String) fcoinApi.orders(symbol, "sell", "limit", price + "", num + "");
                            if (StringUtils.isNotBlank(id)) {
                                sell = new Order(id, symbol, price, num);
                                orderList.addSellOrder(sell);
                                AccountCache.deleteAvailable(coin, num, price);
                                logger.info(sell.toString());
                            } else {
                                logger.error("sell order is fail!!!");
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
                        double newPrice = price * profit;
                        BigDecimal b = new BigDecimal(newPrice);
                        newPrice = b.setScale(decimal.getPrice_decimal(), BigDecimal.ROUND_UP).doubleValue();
                        while (f) {
                            try {
                                double nn = AccountCache.getNum(currency, num, newPrice);
                                if (nn == num) {
                                    String id1 = (String) fcoinApi.orders(symbol, "buy", "limit", newPrice + "", num + "");
                                    if (StringUtils.isNotBlank(id1)) {
                                        f = false;
                                        Order buy = new Order(id1, symbol, newPrice, num);
                                        logger.info(buy.toString());
                                        orderList.addBuyOrder(sell.getId(), buy);
                                        break;
                                    }
                                }
                                if (ShutdownHook.isShutDown()) {
                                    count++;
                                    if (count > 2) {
                                        logger.error("A buy is error!!! id=" + sell.getId());
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
