package cn.com.btc;

import cn.com.btc.core.FcoinApiHandler;

public class LauncherTest {

    public static void main(String[] args) {
        Object obj = FcoinApiHandler.getInstance().marketDepth("L20", "etcusdt");
        System.out.println(obj);
//        System.out.println(FCoin.marketTicker("fteth"));
    }
}
