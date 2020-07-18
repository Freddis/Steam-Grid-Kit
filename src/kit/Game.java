package kit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Game {

    private String directory;
    private String name;
    private String steamId;
    private ArrayList<String> execs = new ArrayList<>();

    public Game(JSONObject obj) {
        this(obj.getString("directory"));
        this.name =  obj.has("name") ? obj.getString("name") : null;
        this.steamId = obj.has("steamId") ? obj.getString("steamId") : null;
        JSONArray arr = obj.getJSONArray("execs");
        for (int i = 0; i < arr.length(); i++) {
            execs.add(arr.getString(i));
        }
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
        String[] arr = execs.toArray(new String[0]);
        obj.put("execs", arr);
        return obj;
    }
}
