package kit.models;

import javafx.scene.image.Image;
import kit.Config;
import kit.interfaces.IJson;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class Game implements IJson {

    private final String directory;
    private String name;
    private String steamId;
    private final ArrayList<String> execs = new ArrayList<>();
    private Image headerImage;

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
        obj.put("name", name);
        obj.put("steamId", steamId);
        obj.put("execs", new JSONArray(execs));
        return obj;
    }

    @Override
    public boolean init(JSONObject obj) {
        this.name = obj.optString("name");
        this.steamId = obj.optString("steamId");
        JSONArray arr = obj.getJSONArray("execs");
        for (int i = 0; i < arr.length(); i++) {
            execs.add(arr.getString(i));
        }
        return true;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSteamId() {
        return this.steamId;
    }

    public void setSteamId(int appId) {
        this.steamId = String.valueOf(appId);
    }

    public boolean isReadyToExport() {
        @SuppressWarnings("UnnecessaryLocalVariable")
        boolean ready = this.execs.size() > 0 && this.steamId != null && this.name != null;
        return ready;
    }

    public File getHeaderImageFile() {
        return this.returnFileIfExists(Config.getImageDirectory() + "/" + this.getDirectory() + "/header.jpg");
    }
    public File getCoverImageFile() {
        return this.returnFileIfExists(Config.getImageDirectory() + "/" + this.getDirectory() + "/library_600x900.jpg");
    }
    public File getBackgroundImageFile() {
        return this.returnFileIfExists(Config.getImageDirectory() + "/" + this.getDirectory() + "/library_hero.jpg");
    }
    public File getLogoImageFile() {
        return this.returnFileIfExists(Config.getImageDirectory() + "/" + this.getDirectory() + "/logo.png");
    }

    protected File returnFileIfExists(String path) {
        File file = new File(path);
        if (!file.exists() || !file.canRead()) {
            return null;
        }
        return file;
    }

    public String getExecName() {
        if(this.getExecs().size() == 0)
        {
            return null;
        }
        String first = this.getExecs().get(0);
        File file = new File(first);
        return file.getName();
    }
}
