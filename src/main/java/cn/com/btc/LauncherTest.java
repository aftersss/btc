package cn.com.btc;

import cn.com.btc.core.FcoinApiHandler;
import cn.com.btc.core.Pair;
import cn.com.btc.core.Writer;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LauncherTest {
    public static void main(String[] args) throws IOException {
        ConcurrentLinkedQueue<String> orderQueue = new ConcurrentLinkedQueue<>();
        orderQueue.add("1");
        orderQueue.add("2");
        System.out.println(orderQueue.poll());
        System.out.println(orderQueue.poll());
    }
}
