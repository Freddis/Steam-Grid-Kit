package kit.utils;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

public class UrlOpener {

    public void open(String url)
    {
        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
