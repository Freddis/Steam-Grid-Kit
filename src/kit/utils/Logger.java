package kit.utils;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import kit.interfaces.ILogger;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

public class Logger implements ILogger {

    private final TextArea logTextArea;

    public Logger(TextArea area)
    {
        this.logTextArea = area;
    }
    public void log(String msg)
    {
        SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss"); //"yyyy-MM-dd hh:mm:ss"
        String date = format.format(new Date());
        String line = date + ": " + msg + "\n";
        System.out.print(line);
        Platform.runLater(() -> logTextArea.appendText(line));
    }

    @Override
    public void error(String error, Exception e) {
        String trace = Arrays.stream(e.getStackTrace()).map(x -> x.getFileName() + ":" + x.getLineNumber() + "\n")
                .collect(Collectors.joining());
        StringBuilder sb = new StringBuilder();
        sb.append(error);
        sb.append("\n");
        sb.append(e.getMessage());
        sb.append("\n");
        sb.append(trace);
        String line = sb.toString();
        Platform.runLater(() -> logTextArea.appendText(line));
    }
}
