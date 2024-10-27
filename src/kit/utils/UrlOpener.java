package kit.utils;

import kit.interfaces.ILogger;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

/**
 * Opens URLs in the system browser.
 * UrlOpener incapsulates possibly non-crossplatform ways of opening urls.
 */
public class UrlOpener {

    private final ILogger logger;

    public UrlOpener(ILogger logger){
        this.logger = logger;
    }

    /**
     * Open URL in a web browser.
     * @param url Url
     */
    public void open(String url)
    {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            }
            catch (Exception e){
                this.logger.error("Couldn't open URL",e);
            }
        }
    }
}
