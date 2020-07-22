package kit.utils;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class Progress {

    private final Label labelProgress;
    private final ProgressBar progressBar;
    private final Node[] controls;
    private final Button startButton;
    private final BooleanProperty isRunning = new SimpleBooleanProperty(false);
    private String status;

    public Progress(Logger logger, Label label, ProgressBar bar, Node[] controls, Button startButton, Runnable startRequested, Runnable stopRequested) {
        this.labelProgress = label;
        this.progressBar = bar;
        this.controls = controls;
        this.startButton = startButton;
        startButton.setOnMouseClicked((e) -> {
            if (isRunning.getValue()) {
                stopRequested.run();
            } else {
                startRequested.run();
            }
        });
    }

    public void endTask(String status) {
        isRunning.set(false);
        this.status = status;
        Platform.runLater(() -> {
            this.toggleControls(true);
            progressBar.setProgress(0.0);
            labelProgress.setText(status);
            startButton.setText("Start");
        });
    }

    public void startTask(String taskStatus) {
        this.status = taskStatus;
        isRunning.set(true);
        Platform.runLater(() -> {
            this.toggleControls(false);
            startButton.setText("Stop");
            progressBar.setProgress(0.01);
            labelProgress.setText(taskStatus);
        });
    }

    public void setTaskProgress(double progress) {
        Platform.runLater(() -> {
            progressBar.setProgress(progress);
        });
    }

    public void toggleControls(boolean state) {
        Platform.runLater(() -> {
            for (Node control : controls) {
                control.setDisable(!state);
            }
        });
    }

    public ObservableBooleanValue getIsRunningProperty() {
        return isRunning;
    }

    public String getStatus() {
        return status;
    }
}
