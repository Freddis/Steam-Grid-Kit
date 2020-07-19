package kit.interfaces;

import java.util.function.Consumer;

public interface ITask {
    String getStatusString();

    void onFinish(Consumer<Boolean> finishCallback);

    void start(Consumer<Double> tickCallback);
}
