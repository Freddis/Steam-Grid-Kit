package kit.interfaces;

import org.json.JSONObject;

public interface IJson {
    JSONObject toJson();

    @SuppressWarnings("UnusedReturnValue")
    boolean init(JSONObject obj);
}
