package kit.tasks;

import kit.interfaces.ITask;
import kit.utils.Logger;

import java.util.List;
import java.util.function.Consumer;

public abstract class ListTask<T> implements ITask {

    protected Consumer<Boolean> finishCallback;
    protected final Logger logger;

    public ListTask(Logger logger) {
        this.logger = logger;
        this.finishCallback = a -> {
        };
    }

    public void start(Consumer<Double> tickCallback) {
        Thread thread = new Thread(() -> {
            List<T> list = this.getList();
            for (int i = 0; i < list.size(); i++) {
                boolean result = false;
                try {
                    result = this.process(list.get(i));
                } catch (Exception e) {
                    this.logger.log(e.getMessage());
                    this.finishCallback.accept(false);
                    return;
                }
                if (!result) {
                    this.finishCallback.accept(false);
                    return;
                }
                double progress = ((double) (i + 1)) / list.size();
                tickCallback.accept(progress);
            }
            this.finishCallback.accept(true);
        });
        thread.setDaemon(true);
        thread.start();
    }

    protected abstract boolean process(T o);

    protected abstract List<T> getList();

    protected void error(String error) {
        logger.log(error);
    }

    public void onFinish(Consumer<Boolean> finishCallback) {
        this.finishCallback = finishCallback;
    }

}
