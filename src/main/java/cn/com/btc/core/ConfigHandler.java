package cn.com.btc.core;

import cn.com.btc.utils.CommonUntil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigHandler {
    private static final Logger logger = LoggerFactory.getLogger(ConfigHandler.class);
    private static volatile Properties properties;

    static {
        update();
    }

    public static void update() {
        try {
            properties = new Properties();
            properties.load(new FileInputStream(new File(CommonUntil.confDir, "btc.properties")));
        } catch (IOException e) {
            logger.error("load conf error!!!", e);
        }
    }

    public static String getConf(String key) {
        return properties.getProperty(key);
    }

    public static String getConf(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
