package kit.interfaces;

import org.json.JSONObject;

public interface IJson {
    JSONObject toJson();
    boolean init(JSONObject obj);
}
