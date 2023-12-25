package kit.interfaces;

public interface ILogger {
    void log(String msg);

    void error(String error, Exception e);
}
