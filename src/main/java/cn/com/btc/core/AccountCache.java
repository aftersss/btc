package cn.com.btc.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AccountCache {
    private static final Map<String, Double> availableMap = new ConcurrentHashMap<>();

    public static void updateAvailable(String currency, double available) {
        availableMap.put(currency, available);
    }

    public static double getNum(String currency, double num, double price) {
        Double hasNum = availableMap.get(currency);
        if (hasNum != null) {
            hasNum = hasNum * 0.99;
            if (hasNum > num * price) {
                return num;
            } else {
                return hasNum / price;
            }
        } else {
            return 0.0;
        }
    }

    public static void deleteAvailable(String currency, double num, double price) {
        availableMap.put(currency, availableMap.get(currency) - num * price);
    }
}
