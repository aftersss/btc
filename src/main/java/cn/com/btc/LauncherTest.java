package cn.com.btc;

import cn.com.btc.core.FcoinApiHandler;
import cn.com.btc.ft.FcoinApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LauncherTest {
    private static final Logger logger = LoggerFactory.getLogger(LauncherTest.class);

    public static void main(String[] args) throws IOException {
        FcoinApi fcoinApi = FcoinApiHandler.getInstance();
        Object obj = fcoinApi.queryOrderList("fteth", "submitted", null, 1000 + "", 2000 + "");
        logger.info(obj.toString());
    }
}
