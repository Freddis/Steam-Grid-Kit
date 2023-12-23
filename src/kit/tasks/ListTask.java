package kit.tasks;

import kit.interfaces.ILogger;
import kit.interfaces.ITask;

import java.util.List;
import java.util.function.Consumer;

public abstract class ListTask<T> implements ITask {

    protected Consumer<Boolean> finishCallback;
    protected final ILogger logger;
    private Thread thread;

    public ListTask(ILogger logger) {
        this.logger = logger;
        this.finishCallback = a -> {
        };
    }

    public void start(Consumer<Double> tickCallback) {
         thread = new Thread(() -> {
            List<T> list = this.getList();
            for (int i = 0; i < list.size(); i++) {
                boolean result;
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
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    logger.log("Thread actually interrupted");
                    return;
                }
            }
            this.finishCallback.accept(true);
        });
        thread.setDaemon(true);
        thread.start();
    }

    protected abstract boolean process(T o);

    protected abstract List<T> getList();

    public void onFinish(Consumer<Boolean> finishCallback) {
        this.finishCallback = finishCallback;
    }

    @Override
    public void kill() {
        if (thread == null)
        {
            return;
        }
        logger.log(this.getClass().getName() + " interrupted");
        thread.interrupt();
    }
}
