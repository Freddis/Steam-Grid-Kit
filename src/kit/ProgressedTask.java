package kit;

import javafx.util.Callback;
import kit.utils.Logger;

import java.util.List;

public abstract class ProgressedTask<T> {

    private final Logger logger;

    public ProgressedTask(Logger logger) {
        this.logger = logger;
    }

    public void start(Callback<Double, Void> tickCallback) {
        Thread thread = new Thread(() -> {
            List<T> list = this.getList();
            for (int i = 0; i < list.size(); i++) {
                this.process(list.get(i));
                double progress = ((double) (i + 1)) / list.size();
                tickCallback.call(progress);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            tickCallback.call(1.0);
        });
        thread.setDaemon(true);
        thread.start();
    }

    protected void log(String msg) {
        logger.log(msg);
    }

    protected abstract void process(T o);

    protected abstract List<T> getList();
}
