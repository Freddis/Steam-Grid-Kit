package kit.utils;

import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.image.ImageView;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;

public class ImageCache {
    HashMap<File, ImageView> cache = new HashMap<>();

    public ImageView getImageView(File file) {
        return getImageView(file,null);
    }
    public ImageView getImageView(File file, ObservableDoubleValue widthProp) {
        if(file == null)
        {
            return null;
        }
        if(cache.containsKey(file))
        {
            return cache.get(file);
        }
        ImageView view;
        try {
            view = new ImageView(file.toURI().toURL().toString());
            view.setPreserveRatio(true);
            if(widthProp != null)
            {
                view.fitWidthProperty().bind(widthProp);
            }
        } catch (MalformedURLException e) {
            return null;
        }
        cache.put(file,view);
        return view;
    }
}
