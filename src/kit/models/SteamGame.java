package kit.models;

import kit.interfaces.IJson;
import org.json.JSONObject;

public class SteamGame implements IJson {

    int appId;
    String name;

    public SteamGame(JSONObject json) {
        this.init(json);
    }

    @Override
    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.put("name", this.name);
        obj.put("appid", this.appId);
        return obj;
    }

    @Override
    public boolean init(JSONObject obj) {
        try {
            this.appId = obj.getInt("appid");
            this.name = obj.getString("name");
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public String getName() {
        return name;
    }

    public int getAppId() {
        return appId;
    }
}
