package kit.utils;

import javafx.util.Callback;
import kit.interfaces.IJson;
import kit.interfaces.ILogger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class JsonHelper {

    private final ILogger logger;

    public JsonHelper(ILogger logger) {
        this.logger = logger;
    }

    public JSONObject readJsonFromFile(String path) {
        this.logger.log("Loading data from file " + path);
        File json = new File(path);
        JSONObject root;
        String content;
        try {
            content = new String(Files.readAllBytes(Paths.get(json.toURI())));
            this.logger.log("Bytes loaded");
            root = new JSONObject(content);
            this.logger.log("Content loaded");
        } catch (Exception e) {
            this.logger.log("Can't parse json from: " + path);
            //e.printStackTrace();
            return new JSONObject();
        }
        return root;
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean writeJsonToFile(String path, JSONObject obj) {
        this.logger.log("Writing data to file " + path);
        String output = obj.toString(2);
        try {
            PrintWriter writer = new PrintWriter(path, "UTF-8");
            writer.write(output);
            writer.close();
            return true;
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
            this.logger.log("Couldn't write json to file: " + path);
        }
        return false;
    }

    public String[] toStringArray(JSONArray arr) {
        if (arr == null) {
            return new String[]{};
        }
        String[] result = new String[arr.length()];
        for (int i = 0; i < arr.length(); i++) {
            result[i] = arr.getString(i);
        }
        return result;
    }


    public <T extends IJson> ArrayList<T> toList(Callback<JSONObject, T> factory, JSONArray array) {
        ArrayList<T> result = new ArrayList<>();
        if (array == null) {
            return result;
        }
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.optJSONObject(i);
            T instance = factory.call(obj);
            result.add(instance);
        }
        return result;
    }

    public JSONArray toJsonArray(IJson[] list) {
        JSONArray result = new JSONArray();
        for (IJson obj : list) {
            result.put(obj.toJson());
        }
        return result;
    }
    public JSONArray toJsonArray(String[] arr) {
        JSONArray result = new JSONArray();
        for (String obj : arr) {
            result.put(obj);
        }
        return result;
    }
}
