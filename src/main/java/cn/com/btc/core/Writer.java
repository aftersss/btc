package cn.com.btc.core;

import cn.com.btc.utils.CommonUntil;
import cn.com.btc.utils.MyDateFormat;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Writer extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(Writer.class);
    private static String limitTime = null;
    private static FileWriter orderWriter = null;
    private static FileWriter finishWriter = null;
    private static ConcurrentLinkedQueue<Pair<Order, Order>> orderQueue = new ConcurrentLinkedQueue<>();
    private static ConcurrentLinkedQueue<Pair<Order, Order>> finishQueue = new ConcurrentLinkedQueue<>();

    public static void addOrder(Pair<Order, Order> order) {
        orderQueue.add(order);
    }

    public static void addFinish(Pair<Order, Order> order) {
        finishQueue.add(order);
    }

    @Override
    public void run() {
        while (true) {
            try {
                Calendar calendar = Calendar.getInstance();
                String time = MyDateFormat.format("yyyyMMdd", calendar.getTime());
                if (orderWriter == null || finishWriter == null || time.compareTo(limitTime) >= 0) {
                    try {
                        if (orderWriter != null) {
                            orderWriter.close();
                        }
                        if (finishWriter != null) {
                            finishWriter.close();
                        }
                    } catch (Throwable ignored) {
                    }
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    limitTime = MyDateFormat.format("yyyyMMdd", calendar.getTime());
                    orderWriter = new FileWriter(new File(CommonUntil.dataDir, "order-" + time + ".dat"), true);
                    finishWriter = new FileWriter(new File(CommonUntil.dataDir, "finish-" + time + ".dat"), true);
                }
                while (!orderQueue.isEmpty()) {
                    String t = MyDateFormat.format("yyyy-MM-dd HH:mm:ss", new Date());
                    Pair<Order, Order> pair = orderQueue.poll();
                    assert pair != null;
                    orderWriter.write(t + " symbol=" + pair.getLeft().getSymbol() + " num=" + pair.getLeft().getNum() +
                            " buyId=" + pair.getLeft().getId() + " buyPrice=" + pair.getLeft().getPrice() +
                            " sellId=" + pair.getRight().getId() + " sellPrice=" + pair.getRight().getPrice());
                }
                orderWriter.flush();
                while (!finishQueue.isEmpty()) {
                    String t = MyDateFormat.format("yyyy-MM-dd HH:mm:ss", new Date());
                    Pair<Order, Order> pair = finishQueue.poll();
                    assert pair != null;
                    finishWriter.write(t + " symbol=" + pair.getLeft().getSymbol() + " num=" + pair.getLeft().getNum() +
                            " buyId=" + pair.getLeft().getId() + " buyPrice=" + pair.getLeft().getPrice() +
                            " sellId=" + pair.getRight().getId() + " sellPrice=" + pair.getRight().getPrice());
                }
                finishWriter.flush();
            } catch (Throwable t) {
                logger.error("Write log error!!!", t);
            }
        }
    }
}
