package cn.com.btc;

import cn.com.btc.core.FcoinApiHandler;
import cn.com.btc.core.Writer;

import java.io.IOException;

public class LauncherTest {
    public static void main(String[] args) throws IOException {
        System.out.println(Writer.load());
    }
}
