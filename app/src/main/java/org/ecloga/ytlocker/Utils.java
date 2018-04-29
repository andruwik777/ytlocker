package org.ecloga.ytlocker;

import java.io.IOException;

public class Utils {
    public static boolean requestRootPrivileges() {
        Boolean result = false;

        try {
            Process executor = Runtime.getRuntime().exec("su -c ls /data/data");
            executor.waitFor();
            result = executor.exitValue() == 0;
        } catch (InterruptedException | IOException ignored) {
        }
        return result;
    }
}
