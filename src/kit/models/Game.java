package kit.models;

import kit.Config;
import kit.State;
import kit.interfaces.IJson;
import kit.interfaces.ILogger;
import kit.utils.BinaryOperations;
import kit.vdf.VdfKey;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class Game implements IJson {
    /**
     * Name of the game directory, serves as the game ID in the app
     */
    private String directory;

    /**
     * Path to the parent directory where the game is located
     */
    private String parentDirectory;
    /**
     * Relative path to executable from the games directory. Starts with "\"
     */
    private ArrayList<String> execs = new ArrayList<>();
    /**
     * Name that is going to be used for the shortcut if the user doesn't like directory name
     */
    private String altName;

    /**
     * List of Steam games that fit the name of the game.
     */
    private ArrayList<SteamGame> foundSteamGames = new ArrayList<>();
    /**
     * Index of the selected exe file
     */
    private int selectedExeIndex;

    /**
     * Index of the selected steam game
     */
    private int selectedSteamGameIndex;
    /**
     * App Id that Steam uses to identify the shortcut. Can be generated.
     * Without knowing this ID it's not possible to add images, since they have to have this ID in the name.
     */
    private long appId;

    /**
     * Original content of VDF line from Steam. Filled when the game already exists in Steam.
     */
    private JSONObject vdf;

    public Game(JSONObject obj) {
        this(obj.getString("directory"),obj.getString("parentDirectory"));
        this.init(obj);
    }

    public Game(String directory, String gamesDirectory) {
        this.directory = directory;
        this.parentDirectory = gamesDirectory;
        this.appId = this.generateAppId();
    }

    private long generateAppId() {
        return BinaryOperations.generateLong();
    }

    public String getDirectory() {
        return directory;
    }
    public String getParentDirectory() {
        return this.parentDirectory;
    }

    public ArrayList<String> getExecs() {
        return this.execs;
    }

    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.put("parentDirectory", parentDirectory);
        obj.put("directory", directory);
        obj.put("appId", appId);
        obj.put("altName", altName);
        obj.put("selectedExeIndex", selectedExeIndex);
        obj.put("selectedSteamGameIndex", selectedSteamGameIndex);
        obj.put("execs", new JSONArray(execs));
        obj.put("vdf", vdf);
        JSONArray foundGamesJson = new JSONArray();
        foundSteamGames.forEach(el -> foundGamesJson.put(el.toJson()));
        obj.put("foundSteamGames", foundGamesJson);
        return obj;
    }

    @Override
    public boolean init(JSONObject obj) {
        selectedExeIndex = obj.optInt("selectedExeIndex", 0);
        altName = obj.optString("altName", null);
        selectedSteamGameIndex = obj.optInt("selectedSteamGameIndex", 0);
        vdf = obj.optJSONObject("vdf");
        if(vdf != null){
            this.appId = this.getAppIdFromVDF();
        }
        else{
            this.appId = obj.getLong("appId");
        }
        JSONArray execsJson = obj.optJSONArray("execs");
        for (int i = 0; i < execsJson.length(); i++) {
            execs.add(execsJson.getString(i));
        }
        JSONArray gamesJson = obj.optJSONArray("foundSteamGames");
        gamesJson = gamesJson == null ? new JSONArray() : gamesJson;
        for (int i = 0; i < gamesJson.length(); i++) {
            SteamGame game = new SteamGame(gamesJson.getJSONObject(i));
            foundSteamGames.add(game);
        }
        return true;
    }

    private long getAppIdFromVDF() {
        JSONObject obj = this.vdf.optJSONObject(VdfKey.APP_ID.getKey());
        if(obj == null){
            return this.appId;
        }
        long val = obj.getLong("value");
        return val;
    }

    public boolean isReadyToExport() {
        boolean hasExec = getSelectedExe() != null;
        boolean hasImages = getHeaderImageFile() != null && getCoverImageFile() != null && getBackgroundImageFile() != null && getLogoImageFile() != null;
        return hasExec && hasImages;
    }

    public File getHeaderImageFile() {
        String path1 = getCustomHeaderImagePath();
        String path2 = getCustomHeaderImagePath().replace(".jpg", ".png");
        String path3 = Config.getImageDirectory() + "/" + this.getImageDirectoryName() + "/header.jpg";
        String[] paths = {path1, path2, path3};
        return this.returnFileIfExists(paths);
    }

    public File getCoverImageFile() {
        String path1 = getCustomCoverImagePath();
        String path2 = getCustomCoverImagePath().replace(".jpg", ".png");
        String path3 = Config.getImageDirectory() + "/" + this.getImageDirectoryName() + "/library_600x900.jpg";
        String[] paths = {path1, path2, path3};
        return this.returnFileIfExists(paths);
    }

    public File getBackgroundImageFile() {
        String path1 = getCustomBackgroundImagePath();
        String path2 = getCustomBackgroundImagePath().replace(".jpg", ".png");
        String path3 = Config.getImageDirectory() + "/" + this.getImageDirectoryName() + "/library_hero.jpg";
        String[] paths = {path1, path2, path3};
        return this.returnFileIfExists(paths);
    }

    public File getLogoImageFile() {
        String path1 = getCustomLogoImagePath();
        String path2 = getCustomLogoImagePath().replace(".png", ".jpg");
        String path3 = Config.getImageDirectory() + "/" + this.getImageDirectoryName() + "/logo.png";
        String[] paths = {path1, path2, path3};
        return this.returnFileIfExists(paths);
    }

    public String getImageDirectoryName() {
        return this.getDirectory().replaceAll("[^A-Za-z0-9 ]", "");
    }

    public String getExecName() {
        String exec = getSelectedExe();
        if (exec == null) {
            return null;
        }
        String[] parts = exec.split("\\\\");
        return parts[parts.length - 1];
    }

    protected File returnFileIfExists(String[] paths) {
        for (String path : paths) {
            File file = new File(path);
            if (file.exists() && file.canRead()) {
                return file;
            }
        }
        return null;
    }


    public String getAltName() {
        return this.altName;
    }

    public ArrayList<SteamGame> getFoundSteamGames() {
        return this.foundSteamGames;
    }

    public int getSelectedSteamGameIndex() {
        return this.selectedSteamGameIndex;
    }

    public int getSelectedExeIndex() {
        return this.selectedExeIndex;
    }

    public String getSelectedExe() {
        return execs.size() > selectedExeIndex ? execs.get(selectedExeIndex) : null;
    }

    public SteamGame getSelectedSteamGame() {
        return foundSteamGames.size() > selectedSteamGameIndex ? foundSteamGames.get(selectedSteamGameIndex) : null;
    }

    public void setSelectedExe(String newExe) {
        int index = execs.indexOf(newExe);
        selectedExeIndex = index != -1 ? index : 0;
    }

    public void setSelectedSteamGame(SteamGame game) {
        int index = foundSteamGames.indexOf(game);
        selectedSteamGameIndex = index != -1 ? index : 0;
    }

    public void setAltName(String name) {
        altName = name;
    }

    public boolean isTheSame(Game game, State settings) {
        return this.isTheSame(game,settings.getJson());
    }
    public boolean isTheSame(Game game, JSONObject settings) {
        if(this.appId == game.appId && this.appId != 0){
            return true;
        }

        String ownDir =  parseRealDirectory(settings);
        String alienDir = game.parseRealDirectory(settings);
        if (ownDir.equals(alienDir)) {
            return true;
        }
        return false;
    }

    protected String parseRealDirectory(JSONObject settings)
    {
        String gamesDir = settings.optString(Config.Keys.LOCAL_GAMES_DIRECTORY_PATH.getKey(),"");
        if(!gamesDir.isEmpty()) {
            char lastChar = gamesDir.charAt(gamesDir.length() - 1);
            gamesDir = lastChar == '\\' ? gamesDir : gamesDir + '\\';
            String[] parts =  this.getDirectory().replace(gamesDir,"").split("\\\\");
            return parts[0];
        }

        String result =  this.getDirectory();
        return result;
    }

    public void setVdf(JSONObject obj) {
        this.vdf = obj;
        this.appId = this.getAppIdFromVDF();
    }

    public boolean hasVdf() {
        return vdf != null;
    }

    public boolean isLocatedIn(String directory) {
        String dirPath = directory +"\\"+this.getDirectory();
        File file = new File(dirPath);
        return file.exists();
    }

    public void wipe() {
        foundSteamGames.clear();
    }

    public JSONObject getVdf() {
        return vdf;
    }

    public String getIntendedTitle() {
        if (this.getAltName() != null && !this.getAltName().isEmpty()) {
            return this.getAltName();
        }
        if (this.getSelectedSteamGame() != null) {
            return this.getSelectedSteamGame().getName();
        }
        return this.getDirectory();
    }

    public String getId() {
        return String.valueOf(this.appId);

        // decided to keep the old code as a reminder how fucked up it was to figure that out

//        String target = this.getSelectedExe() != null ? this.getSelectedExe() : "";
//        String name = this.getIntendedTitle();
//        String seed = '"' + target + '"' + name;
//
//        Checksum checksum = new CRC32();
//        // update the current checksum with the specified array of bytes
//        byte[] bytes = seed.getBytes(StandardCharsets.UTF_8);
//        checksum.update(bytes, 0, bytes.length);
//        // get the current checksum value
//        long checksumValue = checksum.getValue();
//        long x = 0x80000000;
//        long res = checksumValue | -1 * x;
//        return String.valueOf(res);
    }

    public String getCustomHeaderImagePath() {
        return Config.getImageDirectory() + "/" + this.getImageDirectoryName() + "/setheader.jpg";
    }

    public String getCustomCoverImagePath() {
        return Config.getImageDirectory() + "/" + this.getImageDirectoryName() + "/setcover.jpg";
    }

    public String getCustomBackgroundImagePath() {
        return Config.getImageDirectory() + "/" + this.getImageDirectoryName() + "/setbackground.jpg";
    }

    public String getCustomLogoImagePath() {
        return Config.getImageDirectory() + "/" + this.getImageDirectoryName() + "/setlogo.png";
    }

    public String getAppIdAsString(ILogger logger) {
         String result = BinaryOperations.longToString(this.appId);
         return result;
    }

    public void setAppId(long i) {
        this.appId = i;
    }

    public long getAppId() {
        return this.appId;
    }
}
