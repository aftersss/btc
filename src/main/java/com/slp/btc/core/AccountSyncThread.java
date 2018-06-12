package com.slp.btc.core;

import com.google.gson.Gson;
import com.slp.btc.fcoin.FcoinApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountSyncThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(AccountSyncThread.class);
    private static final long sleepTimes = 5 * 60 * 1000;
    private static final Map<String, Double> availableMap = new HashMap<>();
    private static final Map<String, Double> frozenMap = new HashMap<>();
    private static final Map<String, Double> balanceMap = new HashMap<>();

    public AccountSyncThread() {
        setName("account-sync-thread");
    }

    @Override
    public void run() {
        FcoinApi fcoinApi = FcoinApiHandler.getInstance();
        while (true) {
            try {
                List<Map<String, String>> mapList = fcoinApi.accountsBalance();
                if (mapList != null) {
                    for (Map<String, String> m : mapList) {
                        String currency = m.get("currency");
                        double available = Double.parseDouble(m.get("available"));
                        double frozen = Double.parseDouble(m.get("frozen"));
                        double balance = Double.parseDouble(m.get("balance"));
                        availableMap.put(currency, available);
                        frozenMap.put(currency, frozen);
                        balanceMap.put(currency, balance);
                    }
                }
                Thread.sleep(sleepTimes);
            } catch (Throwable e) {
                logger.error("get account error!!!", e);
            }
        }
    }

    public static double getAvailable(String currency) {
        return availableMap.get(currency);
    }

    public static double getFrozen(String currency) {
        return frozenMap.get(currency);
    }

    public static double getBalance(String currency) {
        return balanceMap.get(currency);
    }
}
