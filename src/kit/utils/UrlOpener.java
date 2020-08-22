package kit.utils;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

/**
 * Opens URLs in the system browser.
 * UrlOpener incapsulates possibly non-crossplatform ways of opening urls.
 */
public class UrlOpener {

    /**
     * Open URL in a web browser.
     * @param url Url
     */
    public void open(String url)
    {
        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
