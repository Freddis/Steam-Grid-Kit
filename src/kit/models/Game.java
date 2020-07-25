package kit.models;

import kit.Config;
import kit.interfaces.IJson;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class Game implements IJson {

    private final String directory;
    private final ArrayList<String> execs = new ArrayList<>();
    private String altName;
    private final ArrayList<SteamGame> foundSteamGames = new ArrayList<>();
    private int selectedExeIndex;
    private int selectedSteamGameIndex;
    private JSONObject vdf;

    public Game(JSONObject obj) {
        this(obj.getString("directory"));
        this.init(obj);
    }

    public Game(String directory) {
        this.directory = directory;
    }

    public String getDirectory() {
        return directory;
    }

    public ArrayList<String> getExecs() {
        return this.execs;
    }

    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.put("directory", directory);
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

    public boolean isTheSame(Game game) {
        if (this.getDirectory().equals(game.getDirectory())) {
            return true;
        }
        return false;
    }

    public void setVdf(JSONObject obj) {
        this.vdf = obj;
    }

    public boolean hasVdf() {
        return vdf != null;
    }

    public boolean isLocatedIn(String path) {
        return getSelectedExe() != null && getSelectedExe().indexOf(path) == 0;
    }

    public void wipe() {
        foundSteamGames.clear();
    }

    public JSONObject getVdf() {
        return vdf;
    }

    public String getIntendedTitle() {
        if (this.getSelectedSteamGame() != null) {
            return this.getSelectedSteamGame().getName();
        }
        if (this.getAltName() != null && !this.getAltName().isEmpty()) {
            return this.getAltName();
        }
        return this.getDirectory();
    }

    public String getId() {
        String target = this.getSelectedExe() != null ? this.getSelectedExe() : "";
        String name = this.getIntendedTitle();
        String seed = '"' + target + '"' + name;

        Checksum checksum = new CRC32();

        // update the current checksum with the specified array of bytes
        byte[] bytes = seed.getBytes(StandardCharsets.UTF_8);
        checksum.update(bytes, 0, bytes.length);
        // get the current checksum value
        long checksumValue = checksum.getValue();
        long x = 0x80000000;
        long res = checksumValue | -1 * x;
        return String.valueOf(res);
    }

    public String getCustomHeaderImagePath() {
        return Config.getSetImagesDirectory() + "/" + this.getId() + "header.jpg";
    }

    public String getCustomCoverImagePath() {
        return Config.getSetImagesDirectory() + "/" + this.getId() + "p.jpg";
    }

    public String getCustomBackgroundImagePath() {
        return Config.getSetImagesDirectory() + "/" + this.getId() + "_hero.jpg";
    }

    public String getCustomLogoImagePath() {
        return Config.getSetImagesDirectory() + "/" + this.getId() + "_logo.png";
    }
}
