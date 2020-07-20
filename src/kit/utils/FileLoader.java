package kit.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Consumer;

public class FileLoader {

    private final File file;
    private final String url;
    private final int bufferSize;
    private final double expectedFileSizeMb;
    private Consumer<Boolean> finishCallback;

    @SuppressWarnings("unused")
    public FileLoader(String from, File to) {
        this(from, to, 1024 * 5, 10.0);
    }

    public FileLoader(String from, File to, int bufferSize, double expectedFileSizeMb) {
        this.url = from;
        this.file = to;
        this.finishCallback = a -> {
        };
        this.bufferSize = bufferSize;
        this.expectedFileSizeMb = expectedFileSizeMb;
    }

    public void start(Consumer<Double> tickCallback) {
        Thread thread = new Thread(() -> {
            try {
                URL url = new URL(this.url);
                HttpURLConnection httpConnection = (HttpURLConnection) (url.openConnection());

                //No luck with Steam
                int completeFileSize = httpConnection.getContentLength() > 0 ? httpConnection.getContentLength() : (int) (this.expectedFileSizeMb * 1024 * 1024);
                BufferedInputStream in = new BufferedInputStream(httpConnection.getInputStream());
                FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
                int bufferSize = this.bufferSize;
                BufferedOutputStream bout = new BufferedOutputStream(fos, bufferSize);
                byte[] data = new byte[bufferSize];
                int downloadedFileSize = 0;
                int x;
                double currentProgress;
                while ((x = in.read(data, 0, bufferSize)) >= 0) {
                    bout.write(data, 0, x);
                    downloadedFileSize += x;
                    // calculate progress
                    currentProgress = (double) downloadedFileSize / (double) completeFileSize;
                    tickCallback.accept(currentProgress);
                }
                bout.close();
                in.close();
                this.finishCallback.accept(true);
            } catch (MalformedURLException e) {
                this.finishCallback.accept(false);
            } catch (IOException e) {
                e.printStackTrace();
                this.finishCallback.accept(false);
            }
        });
        thread.start();
    }

    public void onFinish(Consumer<Boolean> finishCallback) {
        this.finishCallback = finishCallback;
    }
}
