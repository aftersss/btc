package com.slp.btc;

import com.slp.btc.core.*;
import com.slp.btc.util.CommonUntil;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Launcher {
    private static final Logger logger = LoggerFactory.getLogger(Launcher.class);
    private static final AccountSyncThread accountSyncThread = new AccountSyncThread();
    private static final Map<String, Thread> placeOrderMap = new HashMap<>();
    private static final Map<String, Thread> checkOrderMap = new HashMap<>();

    public static void main(String[] args) {
        PropertyConfigurator.configure(new File(CommonUntil.confDir, "log4j.properties").getAbsolutePath());
        accountSyncThread.start();
        String typeStr = ConfigHandler.getConf("btc.type");
        String[] types = typeStr.split("\\s+");
        for (String type : types) {
            OrderList orderList = new OrderList(Integer.parseInt(ConfigHandler.getConf("btc." + type + ".oredersize", "10")));
            PlaceOrderThread placeOrderThread = new PlaceOrderThread(type, orderList);
            placeOrderMap.put(type, placeOrderThread);
            placeOrderThread.start();
            CheckOrderThread checkOrderThread = new CheckOrderThread(type, orderList);
            checkOrderMap.put(type, checkOrderThread);
            checkOrderThread.start();
        }
    }
}
