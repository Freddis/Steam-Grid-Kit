package kit;

import java.io.File;
import java.io.IOException;

public class Config {

    public static String getPropsJsonFilePath() {
        return getJarPath() + "/SteamGridKit.json";
    }

    public static String getSteamLibraryJsonFilePath() {
        return getJarPath() + "/steam.json";
    }

    public static String getJarPath() {
        File jarDir = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath());
        try {
            String path = jarDir.getCanonicalPath();
            return path;
        } catch (IOException e) {
            return "";
        }
    }
}
