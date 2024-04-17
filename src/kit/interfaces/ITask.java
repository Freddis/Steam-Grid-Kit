package kit.interfaces;

import kit.State;

import java.util.function.Consumer;

public interface ITask {
    String getStatusString();

    void onFinish(Consumer<Boolean> finishCallback);

    void start(State state, Consumer<Double> tickCallback);

    void kill();
}
