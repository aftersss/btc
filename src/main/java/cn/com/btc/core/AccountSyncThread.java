package cn.com.btc.core;

import cn.com.btc.ft.FcoinApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class AccountSyncThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(AccountSyncThread.class);
    private static final long sleepTimes = Long.valueOf(ConfigHandler.getConf("btc.account.sleep","3000"));
    private static FcoinApi fcoinApi = FcoinApiHandler.getInstance();

    public AccountSyncThread() {
        setName("account-sync-thread");
    }

    @Override
    public void run() {
        while (true) {
            try {
                update();
                Thread.sleep(sleepTimes);
            } catch (Throwable e) {
                logger.error("get account error!!!", e);
            }
        }
    }

    public static synchronized void update() {
        try {
            List<Map<String, String>> mapList = (List<Map<String, String>>) fcoinApi.accountsBalance();
            if (mapList != null) {
                for (Map<String, String> m : mapList) {
                    String currency = m.get("currency");
                    double available = Double.parseDouble(m.get("available"));
                    AccountCache.updateAvailable(currency, available);
                }
            }
        } catch (Throwable e) {
            logger.error("get account error!!!", e);
        }
    }
}
