package kit.utils;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import kit.interfaces.ILogger;

import java.text.SimpleDateFormat;
import java.util.Date;

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
}
