package cn.com.btc.core;

import cn.com.btc.utils.CommonUntil;
import cn.com.btc.utils.MyDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Writer extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(Writer.class);
    private static String limitTime = null;
    private static FileWriter orderWriter = null;
    private static FileWriter finishWriter = null;
    private static ConcurrentLinkedQueue<Pair> orderQueue = new ConcurrentLinkedQueue<>();
    private static ConcurrentLinkedQueue<Pair> finishQueue = new ConcurrentLinkedQueue<>();

    public static void addOrder(Pair order) {
        orderQueue.add(order);
    }

    public static void addFinish(Pair order) {
        finishQueue.add(order);
    }

    @Override
    public void run() {
        while (!ShutdownHook.isShutDown()) {
            save();
            try {
                Thread.sleep(500L);
            } catch (Throwable e) {
            }
        }
    }

    public synchronized static void save() {
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
                if(!CommonUntil.dataDir.exists()){
                    CommonUntil.dataDir.mkdirs();
                }
                File orderFile = new File(CommonUntil.dataDir, "order-" + time + ".dat");
                File finsihFile = new File(CommonUntil.dataDir, "finish-" + time + ".dat");
                if (!orderFile.exists()){
                    orderFile.createNewFile();
                }
                if (!finsihFile.exists()){
                    finsihFile.createNewFile();
                }
                orderWriter = new FileWriter(orderFile, true);
                finishWriter = new FileWriter(finsihFile, true);
            }
            while (!orderQueue.isEmpty()) {
                String t = MyDateFormat.format("yyyyMMddHHmmss", new Date());
                Pair pair = orderQueue.poll();
                assert pair != null;
                orderWriter.write(t + " " + pair.getBuy().getSymbol() + " " + pair.getBuy().getNum() +
                        " " + pair.getBuy().getId() + " " + pair.getBuy().getPrice() +
                        " " + pair.getSell().getId() + " " + pair.getSell().getPrice() + "\n");
            }
            orderWriter.flush();
            while (!finishQueue.isEmpty()) {
                String t = MyDateFormat.format("yyyyMMddHHmmss", new Date());
                Pair pair = finishQueue.poll();
                assert pair != null;
                finishWriter.write(t + " " + pair.getBuy().getSymbol() + " " + pair.getBuy().getNum() +
                        " " + pair.getBuy().getId() + " " + pair.getBuy().getPrice() +
                        " " + pair.getSell().getId() + " " + pair.getSell().getPrice() + "\n");
            }
            finishWriter.flush();
        } catch (Throwable t) {
            logger.error("Write log error!!!", t);
        }
    }

    public static Map<String, Map<String, Pair>> load() throws IOException {
        Map<String, Map<String, Pair>> mapMap = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
        String now = MyDateFormat.format("yyyyMMdd", calendar.getTime());
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        String yes = MyDateFormat.format("yyyyMMdd", calendar.getTime());
        File nowOrder = new File(CommonUntil.dataDir, "order-" + now + ".dat");
        File yesOrder = new File(CommonUntil.dataDir, "order-" + yes + ".dat");
        File nowFinish = new File(CommonUntil.dataDir, "finish-" + now + ".dat");
        File yesFinish = new File(CommonUntil.dataDir, "finish-" + yes + ".dat");
        if (nowOrder.exists() && nowOrder.isFile()) {
            BufferedReader br = new BufferedReader(new FileReader(nowOrder));
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] ll = line.split(" ");
                String symbol = ll[1];
                Order buy = new Order(ll[3], symbol, Double.parseDouble(ll[4]), Double.parseDouble(ll[2]));
                Order sell = new Order(ll[5], symbol, Double.parseDouble(ll[6]), Double.parseDouble(ll[2]));
                Map<String, Pair> map = mapMap.computeIfAbsent(symbol, k -> new HashMap<>());
                map.put(buy.getId(), new Pair(buy, sell));
            }
            br.close();
        }
        if (yesOrder.exists() && yesOrder.isFile()) {
            BufferedReader br = new BufferedReader(new FileReader(yesOrder));
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] ll = line.split(" ");
                String symbol = ll[1];
                Order buy = new Order(ll[3], symbol, Double.parseDouble(ll[4]), Double.parseDouble(ll[2]));
                Order sell = new Order(ll[5], symbol, Double.parseDouble(ll[6]), Double.parseDouble(ll[2]));
                Map<String, Pair> map = mapMap.computeIfAbsent(symbol, k -> new HashMap<>());
                map.put(buy.getId(), new Pair(buy, sell));
            }
            br.close();
        }
        if (nowFinish.exists() && nowFinish.isFile()) {
            BufferedReader br = new BufferedReader(new FileReader(nowFinish));
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] ll = line.split(" ");
                String symbol = ll[1];
                String id = ll[3];
                Map<String, Pair> map = mapMap.get(symbol);
                if (map == null) {
                    continue;
                }
                map.remove(id);
            }
            br.close();
        }
        if (yesFinish.exists() && yesFinish.isFile()) {
            BufferedReader br = new BufferedReader(new FileReader(nowFinish));
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] ll = line.split(" ");
                String symbol = ll[1];
                String id = ll[3];
                Map<String, Pair> map = mapMap.get(symbol);
                if (map == null) {
                    continue;
                }
                map.remove(id);
            }
            br.close();
        }
        return mapMap;
    }
}
