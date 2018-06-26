package cn.com.btc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShutdownHook extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(ShutdownHook.class);
    private static volatile boolean isShutDown = false;

    @Override
    public void run() {
        logger.info("Start to exit!!!!");
        isShutDown = true;
        try {
            Thread.sleep(3000L);
        } catch (Throwable t) {
        }
        Writer.save();
        logger.info("Finish to exit!!!!");
    }

    public static boolean isShutDown() {
        return isShutDown;
    }
}
