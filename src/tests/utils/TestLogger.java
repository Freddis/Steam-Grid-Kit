package tests.utils;

import kit.interfaces.ILogger;

import java.util.Arrays;
import java.util.stream.Collectors;

public class TestLogger implements ILogger {

    public void log(String msg) {
        System.out.println(msg);
    }

    @Override
    public void error(String error, Exception e) {
        System.out.println(error);
        System.out.println(e.getMessage());
        String trace = Arrays.stream(e.getStackTrace()).map(x -> "--"+x.getFileName() + ":" + x.getLineNumber()+"\n")
                .limit(10)
                .collect(Collectors.joining());
        System.out.println(trace);
    }

}
