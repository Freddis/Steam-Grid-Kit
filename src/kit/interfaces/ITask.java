package kit.interfaces;
import javafx.util.Callback;

public interface ITask {
    String getStatusString();
    void onFinish(Callback<Boolean, Void> finishCallback) ;
    void start(Callback<Double, Void> tickCallback) ;
}
