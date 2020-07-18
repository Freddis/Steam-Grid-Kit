package kit.utils;

import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JsonHelper {

    private final Logger logger;

    public JsonHelper(Logger logger)
    {
        this.logger = logger;
    }

    public JSONObject readJsonFromFile(String path) {
        this.logger.log("Loading data from file " + path);
        File json = new File(path);
        JSONObject root;
        String content = null;
        try {
            content = new String(Files.readAllBytes(Paths.get(json.toURI())));
            root = content != null ? new JSONObject(content) : new JSONObject();
        } catch (IOException e) {
            this.logger.log("Can't parse json from: " + path);
            e.printStackTrace();
            return new JSONObject();
        }
        return root;
    }
    public boolean writeJsonToFile(String path, JSONObject obj) {
        this.logger.log("Writing data to file " + path);
        String output = obj.toString();
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
}
