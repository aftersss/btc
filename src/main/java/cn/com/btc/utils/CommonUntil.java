package cn.com.btc.utils;

import java.io.File;

public class CommonUntil {
    public static File binDir = new File(System.getProperty("user.dir")).getAbsoluteFile();
    public static File workingDir = binDir.getParentFile();
//        public static File workingDir = binDir;
    public static File confDir = new File(workingDir, "conf").exists() ? new File(workingDir, "conf") : new File(binDir, "conf");
    public static File dataDir = new File(workingDir, "data").exists() ? new File(workingDir, "data") : new File(binDir, "data");
}
