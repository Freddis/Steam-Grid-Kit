package kit;

import javafx.util.Callback;
import kit.utils.Logger;
import java.util.List;

public abstract class ProgressedTask<T> {

    private Callback<Boolean, Void> finishCallback;
    protected final Logger logger;

    public ProgressedTask(Logger logger) {
        this.logger = logger;
        this.finishCallback = param -> null;
    }

    public void start(Callback<Double, Void> tickCallback) {
        Thread thread = new Thread(() -> {
            List<T> list = this.getList();
            for (int i = 0; i < list.size(); i++) {
                boolean result = false;
                try {
                    result = this.process(list.get(i));
                } catch (Exception e) {
                    this.logger.log(e.getMessage());
                    this.finishCallback.call(false);
                    return;
                }
                if (!result) {
                    this.finishCallback.call(false);
                    return;
                }

                double progress = ((double) (i + 1)) / list.size();
                tickCallback.call(progress);
            }
            this.finishCallback.call(true);
        });
        thread.setDaemon(true);
        thread.start();
    }

    protected abstract boolean process(T o);

    protected abstract List<T> getList();

    protected void error(String error) {
        logger.log(error);
    }

    public void onFinish(Callback<Boolean, Void> finishCallback) {
        this.finishCallback = finishCallback;
    }

}
