package kit.tasks.impl;

import javafx.util.Callback;
import kit.Config;
import kit.interfaces.ITask;
import kit.utils.FileLoader;
import kit.utils.JsonHelper;
import kit.utils.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

public class SteamGamesLoader implements ITask {

    private final FileLoader loader;
    private final Logger logger;

    public SteamGamesLoader(Logger logger, JSONObject settings) {
        this.logger = logger;
        String url = "http://api.steampowered.com/ISteamApps/GetAppList/v0002/?format=json";
        File file = new File(Config.getSteamLibraryJsonFilePath());
        loader = new FileLoader(url, file, 1024 * 5, 5.4);
    }

    @Override
    public String getStatusString() {
        return "Loading steam games";
    }

    @Override
    public void onFinish(Callback<Boolean, Void> finishCallback) {
        loader.onFinish((status) -> {
            checkResult(status, finishCallback);
            return null;
        });
    }

    public void checkResult(boolean status, Callback<Boolean, Void> finishCallback) {
        JsonHelper jsonHelper = new JsonHelper(logger);
        JSONObject json = jsonHelper.readJsonFromFile(Config.getSteamLibraryJsonFilePath());

        if(!status)
        {
            logger.log("Could't get response from Steam API");
            finishCallback.call(false);
            return;
        }

        if (!json.has("applist") || !json.optJSONObject("applist").has("apps")) {
            logger.log("Seems like json is malformed");
            finishCallback.call(false);
            return;
        }

        JSONArray apps = json.getJSONObject("applist").getJSONArray("apps");
        logger.log("Loaded " + apps.length() + " game ids");
        boolean good = apps.length() > 0;
        if(apps.length() <= 0)
        {
            logger.log("Something went wrong");
            finishCallback.call(false);
            return;
        }
        finishCallback.call(true);
    }


    @Override
    public void start(Callback<Double, Void> tickCallback) {
        loader.start(tickCallback);
    }
}
