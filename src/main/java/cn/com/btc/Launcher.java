package cn.com.btc;

import cn.com.btc.core.*;
import cn.com.btc.utils.CommonUntil;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Launcher {
    private static final AccountSyncThread accountSyncThread = new AccountSyncThread();
    private static final Writer writer = new Writer();
    private static final Map<String, Thread> placeOrderMap = new HashMap<>();
    private static final Map<String, Thread> checkOrderMap = new HashMap<>();

    public static void main(String[] args) throws IOException {
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
        Map<String, Map<String, Pair>> mapMap = Writer.load();
        PropertyConfigurator.configure(new File(CommonUntil.confDir, "log4j.properties").getAbsolutePath());
        accountSyncThread.start();
        writer.start();
        String symbolStr = ConfigHandler.getConf("btc.symbol");
        String[] symbols = symbolStr.split("\\s+");
        for (String symbol : symbols) {
            String symbolReal = symbol.replace("-", "");
            OrderList orderList = new OrderList(symbol, Integer.parseInt(ConfigHandler.getConf("btc." + symbol + ".ordersize", "10")));
            Map<String, Pair> map = mapMap.get(symbolReal);
            if (map != null) {
                orderList.setOrders(map);
            }
            PlaceOrderThread placeOrderThread = new PlaceOrderThread(symbol, orderList);
            placeOrderMap.put(symbol, placeOrderThread);
            placeOrderThread.start();
            CheckOrderThread checkOrderThread = new CheckOrderThread(symbol, orderList);
            checkOrderMap.put(symbol, checkOrderThread);
            checkOrderThread.start();
        }
    }
}
