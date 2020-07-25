package kit;

import java.io.File;
import java.io.IOException;

public class Config {

    boolean isRemote = true;

    public static String getPropsJsonFilePath() {
        return getJarPath() + "/SteamGridKit.json";
    }

    public static String getSteamLibraryJsonFilePath() {
        return getJarPath() + "/steam.json";
    }

    public static String getVdfBackupFolder() {
        return getJarPath() + "/backups";
    }

    public static String getVdfFilePath() {
        return getJarPath() + "/shortcuts.vdf";
    }

    public static String getImageDirectory() {
        return getJarPath() + "/images";
    }

    public static String getSetImagesDirectory() {
        return getJarPath() + "/set images";
    }

    public static String getJarPath() {

        //As artifact, getResource(".") doesn't work
        String url;
        try {
            //noinspection ConstantConditions
            url = ClassLoader.getSystemClassLoader().getResource(".").getPath();
        } catch (Exception e) {
            url = ".";
        }

        File jarDir = new File(url);
        try {
            @SuppressWarnings("UnnecessaryLocalVariable")
            String path = jarDir.getCanonicalPath();
            return path;
        } catch (IOException e) {
            return ".";
        }
    }

    public static String getVersion() {
        return "1.0";
    }

    public static String getUserAgent() {
        return "Steam Grid Kit " + Config.getVersion();
    }

    public enum Keys {
        GAMES_DIRECTORY_PATH("gamesDirectory"),
        GAMES("games"),
        USE_CACHE("useCache"),
        IGNORED_FOLDERS_NAMES("ignoredDirectoryNames"),
        VDF_FILE("vdfFile"),
        LOCAL_GAMES_DIRECTORY_PATH("localGamesDirectory"),
        LOCAL_VDF_PATH("localVdfFile"),
        STEAM_GRID_DB_API_KEY("steamGridDbApiKey");

        private final String key;

        Keys(String name) {
            this.key = name;
        }

        public String getKey() {
            return key;
        }
    }

    public enum Task {
        ALL("All"),
        FIND_EXISTING_SHORTCUTS("Find Existing Shortcuts"),
        FIND_EXISTING_IMAGES("Find Existing images"),
        FIND_GAME_FOLDERS("Find Game Folders"),
        LOAD_STEAM_GAMES("Load Steam Games"),
        FIND_EXECUTABLES("Find Executable"),
        FIND_GAME_IDS("Find Steam IDs"),
        LOAD_STEAM_IMAGES("Load Steam Images");

        public String title;

        Task(String name) {
            this.title = name;
        }

        public String getTitle() {
            return title;
        }
    }
}
