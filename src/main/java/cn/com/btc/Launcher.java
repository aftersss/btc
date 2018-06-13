package cn.com.btc;

import cn.com.btc.core.*;
import cn.com.btc.utils.CommonUntil;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Launcher {
    private static final AccountSyncThread accountSyncThread = new AccountSyncThread();
    private static final Writer writer = new Writer();
    private static final Map<String, PlaceOrderBuyThread> buyPlaceOrderMap = new HashMap<>();
    private static final Map<String, PlaceOrderSellThread> sellPlaceOrderMap = new HashMap<>();
    private static final Map<String, CheckOrderThread> checkOrderMap = new HashMap<>();

    public static void main(String[] args) throws IOException {
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
        PropertyConfigurator.configure(new File(CommonUntil.confDir, "log4j.properties").getAbsolutePath());
        accountSyncThread.start();
        writer.start();
        Map<String, Decimal> decimalMap = new HashMap<>();
        List<Map<String, Object>> mapList = (List<Map<String, Object>>) FcoinApiHandler.getInstance().symbols();
        for (Map<String, Object> m : mapList) {
            String name = (String) m.get("name");
            int price_decimal = ((Number) m.get("price_decimal")).intValue();
            int amount_decimal = ((Number) m.get("amount_decimal")).intValue();
            decimalMap.put(name, new Decimal(price_decimal, amount_decimal));
        }
        Map<String, Map<String, Pair>> mapMap = Writer.load();
        String symbolStr = ConfigHandler.getConf("btc.symbol");
        String[] symbols = symbolStr.split("\\s+");
        for (String symbol : symbols) {
            String symbolReal = symbol.replace("-", "");
            String type = ConfigHandler.getConf("btc." + symbol + ".type", "buy");
            int size = Integer.parseInt(ConfigHandler.getConf("btc." + symbol + ".ordersize", "10"));
            if ("all".equalsIgnoreCase(type) || "buy".equalsIgnoreCase(type)) {
                OrderList buyOrderList = new OrderList(symbol, size);
                Map<String, Pair> buyMap = mapMap.get(symbolReal + "buy");
                if (buyMap != null) {
                    buyOrderList.setOrders(buyMap);
                }
                PlaceOrderBuyThread placeOrderBuyThread = new PlaceOrderBuyThread(symbol, buyOrderList, decimalMap.get(symbolReal));
                buyPlaceOrderMap.put(symbol, placeOrderBuyThread);
                placeOrderBuyThread.start();
                CheckOrderThread buyCheckOrderThread = new CheckOrderThread(symbol, buyOrderList);
                checkOrderMap.put(symbol, buyCheckOrderThread);
                buyCheckOrderThread.start();
            }

            if ("all".equalsIgnoreCase(type) || "sell".equalsIgnoreCase(type)) {
                OrderList sellOrderList = new OrderList(symbol, size);
                Map<String, Pair> sellMap = mapMap.get(symbolReal + "sell");
                if (sellMap != null) {
                    sellOrderList.setOrders(sellMap);
                }
                PlaceOrderSellThread placeOrderSellThread = new PlaceOrderSellThread(symbol, sellOrderList, decimalMap.get(symbolReal));
                sellPlaceOrderMap.put(symbol, placeOrderSellThread);
                placeOrderSellThread.start();
                CheckOrderThread sellCheckOrderThread = new CheckOrderThread(symbol, sellOrderList);
                checkOrderMap.put(symbol, sellCheckOrderThread);
                sellCheckOrderThread.start();
            }
        }
    }
}
