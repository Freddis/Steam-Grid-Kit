package kit;

import kit.interfaces.IJson;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Game implements IJson {

    private String directory;
    private String name;
    private String steamId;
    private ArrayList<String> execs = new ArrayList<>();

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
}
