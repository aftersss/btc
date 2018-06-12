package cn.com.btc.core;

import cn.com.btc.ft.FcoinApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountSyncThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(AccountSyncThread.class);
    private static final long sleepTimes = 5 * 60 * 1000;
    private static final Map<String, Double> availableMap = new HashMap<>();

    public AccountSyncThread() {
        setName("account-sync-thread");
    }

    @Override
    public void run() {
        FcoinApi fcoinApi = FcoinApiHandler.getInstance();
        while (true) {
            try {
                List<Map<String, String>> mapList = (List<Map<String, String>>) fcoinApi.accountsBalance();
                if (mapList != null) {
                    for (Map<String, String> m : mapList) {
                        String currency = m.get("currency");
                        double available = Double.parseDouble(m.get("available"));
                        updateAvailable(currency, available);
                    }
                }
                Thread.sleep(sleepTimes);
            } catch (Throwable e) {
                logger.error("get account error!!!", e);
            }
        }
    }

    public synchronized static void updateAvailable(String currency, double available) {
        availableMap.put(currency, available);
    }

    public static double getNum(String currency, double num, double price) {
        Double hasNum = availableMap.get(currency);
        if (hasNum != null) {
            if (hasNum > num * price) {
                return num;
            } else {
                return hasNum / price * 0.99;
            }
        } else {
            return 0.0;
        }
    }

    public synchronized static void deleteAvailable(String currency, double num, double price) {
        availableMap.put(currency, availableMap.get(currency) - num * price);
    }
}
