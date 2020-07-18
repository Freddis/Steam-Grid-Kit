package kit.utils;

import javafx.util.Callback;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class FileLoader {

    private final File file;
    private final String url;
    private final int bufferSize;
    private final double expectedFileSizeMb;
    private Callback<Boolean, Void> finishCallback;

    public FileLoader(String from, File to)
    {
        this(from,to,1024*5, 10.0);
    }

    public FileLoader(String from, File to, int bufferSize,double expectedFileSizeMb)
    {
        this.url = from;
        this.file = to;
        this.finishCallback = param -> null;
        this.bufferSize = bufferSize;
        this.expectedFileSizeMb = expectedFileSizeMb;
    }

    public void start(Callback<Double,Void> tickCallback)
    {
        Thread thread = new Thread(() -> {
            try {
                URL url = new URL(this.url);
                HttpURLConnection httpConnection = (HttpURLConnection) (url.openConnection());

                //No luck with Steam
                int completeFileSize = httpConnection.getContentLength() > 0 ? httpConnection.getContentLength() : (int) (this.expectedFileSizeMb * 1024 * 1024); ;
                BufferedInputStream in = new BufferedInputStream(httpConnection.getInputStream());
                FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
                int bufferSize = this.bufferSize;
                BufferedOutputStream bout = new BufferedOutputStream(fos, bufferSize);
                byte[] data = new byte[bufferSize];
                int downloadedFileSize = 0;
                int x = 0;
                double currentProgress = 0;
                while ((x = in.read(data, 0, bufferSize)) >= 0) {
                    bout.write(data, 0, x);
                    downloadedFileSize += x;
                    // calculate progress
                    currentProgress = (double) downloadedFileSize / (double) completeFileSize;
                    tickCallback.call(currentProgress);
                }
                bout.close();
                in.close();
                this.finishCallback.call(true);
            } catch (MalformedURLException e) {
                this.finishCallback.call(false);
            } catch (IOException e) {
                e.printStackTrace();
                this.finishCallback.call(false);
            }
        });
        thread.start();
    }

    public void onFinish(Callback<Boolean,Void> finishCallback) {
        this.finishCallback = finishCallback;
    }
}
