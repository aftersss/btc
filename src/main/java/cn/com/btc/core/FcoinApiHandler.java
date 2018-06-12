package cn.com.btc.core;

import cn.com.btc.ft.FcoinApi;

public class FcoinApiHandler {
    private static FcoinApi fcoinApi = null;

    static {
        String key = ConfigHandler.getConf("btc.key");
        String secret = ConfigHandler.getConf("btc.secret");
        fcoinApi = new FcoinApi(key, secret);
    }

    public static FcoinApi getInstance() {
        return fcoinApi;
    }
}
