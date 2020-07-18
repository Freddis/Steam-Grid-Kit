package kit;

import javafx.util.Callback;
import java.util.List;

public abstract class ProgressedTask<T> {

    private final Logger logger;

    ProgressedTask(Logger logger){
        this.logger = logger;
    }

    public void start(Callback<Double,Void> tickCallback)
    {
        List<T> list = this.getList();
        for(int i =0; i < list.size(); i++)
        {
            this.process(list.get(i));
            double progress = (((double)(i+1))/ list.size())*100;
            tickCallback.call(progress);
        }
        tickCallback.call(1.0);
    }

    protected void log(String msg)
    {
        logger.log(msg);
    }

    protected abstract void process(T o);
    protected abstract List<T> getList();
}
