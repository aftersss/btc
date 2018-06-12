package cn.com.btc.core;

public class ShutdownHook extends Thread {
    private static boolean isShutDown = false;

    @Override
    public void run() {
        isShutDown = true;
        try {
            Thread.sleep(3000L);
        } catch (Throwable t) {
        }
        Writer.save();
    }

    public static boolean isShutDown() {
        return isShutDown;
    }
}
