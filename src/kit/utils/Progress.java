package kit.utils;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class Progress {

    private final Label labelProgress;
    private final ProgressBar progressBar;
    private final Node[] controls;

    public Progress(Label label, ProgressBar bar, Node[] controls)
    {
        this.labelProgress = label;
        this.progressBar = bar;
        this.controls = controls;
    }

    public void endTask() {
        Platform.runLater(() -> {
            this.toggleControls(true);
            progressBar.setProgress(0.0);
            labelProgress.setText("Done");
        });
    }

    public void startTask(String taskStatus) {
        Platform.runLater(() -> {
            this.toggleControls(false);
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
}
