package cn.com.btc;

import cn.com.btc.core.FcoinApiHandler;

import java.io.IOException;

public class LauncherTest {
    public static void main(String[] args) throws IOException {
        System.out.println(FcoinApiHandler.getInstance().symbols());
    }
}
