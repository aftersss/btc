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

public class PlaceOrderBuyOldThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(PlaceOrderBuyOldThread.class);
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
    private final double buymax;
    private FcoinApi fcoinApi = FcoinApiHandler.getInstance();

    public PlaceOrderBuyOldThread(String symbol, OrderList orderList, Decimal decimal) {
        this.symbol = symbol.replace("-", "");
        this.decimal = decimal;
        String[] s = symbol.split("-");
        this.coin = s[0];
        this.currency = s[1];
        this.orderList = orderList;
        this.isFt = "ft".equalsIgnoreCase(currency) || "ft".equalsIgnoreCase(coin);
        this.level = ConfigHandler.getConf("btc." + symbol + ".level", "L20");
        this.num = Double.valueOf(ConfigHandler.getConf("btc." + symbol + ".num", "1"));
        this.buymax = Double.valueOf(ConfigHandler.getConf("btc." + symbol + ".buymax", "0"));
        this.profit = Double.valueOf(ConfigHandler.getConf("btc." + symbol + ".profit", "0.001")) + 1;
        this.discount = Double.valueOf(ConfigHandler.getConf("btc." + symbol + ".discount", "0.5"));
        this.sleepTime = Long.valueOf(ConfigHandler.getConf("btc.sleep", "1000"));
        this.minNum = Double.valueOf(ConfigHandler.getConf("btc." + symbol + ".minnum", "0"));
        setName(this.symbol + "-buy-place-order-thread");
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
                            logger.info("in no order time!!!");
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
                        if (asks.size() > 0) {
                            price = asks.get(0).doubleValue();
                            num += asks.get(1).doubleValue() * discount;
                            flag = (buymax <= 0 || price < buymax) && orderList.isAvail(price);
                            if (!flag) {
                                logger.info("fluctuate is full!!! price=" + price);
                            }
                        }
                    } else {
                        logger.info("time is error!!! dur=" + Math.abs(now - ts));
                    }
                    Order buy = null;
                    if (flag) {
                        num = Math.min(num, this.num);
                        num = AccountCache.getNum(currency, num, price);
                        BigDecimal b = new BigDecimal(num);
                        num = b.setScale(decimal.getAmount_decimal(), BigDecimal.ROUND_DOWN).doubleValue();
                        if (num >= minNum && num > 0) {
                            String id = (String) fcoinApi.orders(symbol, "buy", "limit", price + "", num + "");
                            if (StringUtils.isNotBlank(id)) {
                                buy = new Order(id, symbol, price, num);
                                orderList.addBuyOrder(buy);
                                AccountCache.deleteAvailable(currency, num, price);
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
                        double newPrice = price * profit;
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
                                        orderList.addSellOrder(buy.getId(), sell);
                                        logger.info(sell.toString());
                                        break;
                                    } else {
                                        logger.info(" sell order fail!!!");
                                    }
                                } else {
                                    logger.info(" con't full num!!! nn" + nn + " num=" + num);
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
