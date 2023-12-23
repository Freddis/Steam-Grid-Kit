package tests.utils;

import kit.interfaces.ILogger;

public class TestLogger implements ILogger {

    public void log(String msg) {
        System.out.println(msg);
    }
}
