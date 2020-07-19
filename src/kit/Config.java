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

        //As artifact, getResource(".") doesn't work
        String url;
        try {
            url = ClassLoader.getSystemClassLoader().getResource(".").getPath();
        } catch (Exception e) {
            url = ".";
        }

        File jarDir = new File(url);
        try {
            String path = jarDir.getCanonicalPath();
            return path;
        } catch (IOException e) {
            return ".";
        }
    }

    public enum Keys {
        GAMES_DIRECTORY_PATH("gamesDirectory"),
        GAMES("games"),
        USE_CACHE("useCache"),
        VDF_FILE("vdfFile");

        private String key;

        Keys(String name) {
            this.key = name;
        }
        public String getKey() {
            return key;
        }
    }

    public enum Task {
        ALL("All"),
        FIND_GAME_FOLDERS("Find Game Folders"),
        LOAD_STEAM_GAMES("Load Steam Games"),
        FIND_EXECUTABLES("Find Executable"),
        FIND_GAME_IDS("Find Steam IDs");

        public String title;

        Task(String name) {
            this.title = name;
        }
        public String getTitle() {
            return title;
        }
    }
}
