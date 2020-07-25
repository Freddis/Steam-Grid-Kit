package kit.tasks.impl;

import kit.interfaces.ITask;

import java.util.function.Consumer;

public class FindSteamGridDbGame implements ITask {


    @Override
    public String getStatusString() {
        return "";
    }

    @Override
    public void onFinish(Consumer<Boolean> finishCallback) {

    }

    @Override
    public void start(Consumer<Double> tickCallback) {

    }

    @Override
    public void kill() {

    }
}
