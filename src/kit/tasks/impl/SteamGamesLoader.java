package kit.tasks.impl;

import kit.Config;
import kit.interfaces.ITask;
import kit.utils.FileLoader;
import kit.utils.JsonHelper;
import kit.utils.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;

public class SteamGamesLoader implements ITask {

    private final FileLoader loader;
    private final Logger logger;
    private final File tempLocation;
    private final boolean useCache;
    private final JsonHelper helper;
    private Consumer<Boolean> finishCallback;

    public SteamGamesLoader(Logger logger, JSONObject config) {
        this.logger = logger;
        String url = "http://api.steampowered.com/ISteamApps/GetAppList/v0002/?format=json";
        tempLocation = new File(Config.getJarPath(), "tmp.json");
        loader = new FileLoader(url, tempLocation, 1024 * 5, 5.4);
        this.useCache = config.optBoolean(Config.Keys.USE_CACHE.getKey(), false);
        this.helper = new JsonHelper(logger);
    }

    @Override
    public String getStatusString() {
        return "Loading steam games";
    }

    @Override
    public void onFinish(Consumer<Boolean> finishCallback) {
        this.finishCallback = finishCallback;
        loader.onFinish((status) -> checkResult(status, finishCallback));
    }

    public void checkResult(boolean status, Consumer<Boolean> finishCallback) {
        JsonHelper jsonHelper = new JsonHelper(logger);

        JSONObject json = jsonHelper.readJsonFromFile(tempLocation.getAbsolutePath());
        if (!status) {
            logger.log("Could't get response from Steam API");
            finishCallback.accept(false);
            return;
        }

        if (!json.has("applist") || !json.optJSONObject("applist").has("apps")) {
            logger.log("Seems like json is malformed");
            finishCallback.accept(false);
            return;
        }

        JSONArray apps = json.getJSONObject("applist").getJSONArray("apps");
        logger.log("Loaded " + apps.length() + " game ids");
        if (apps.length() <= 0) {
            logger.log("Something went wrong");
            finishCallback.accept(false);
            return;
        }
        File destination = new File(Config.getSteamLibraryJsonFilePath());
        if (!destination.canWrite() || !destination.delete()) {
            logger.log("Cannot delete " + destination.getAbsolutePath());
            finishCallback.accept(false);
            return;
        }
        try {
            Files.move(tempLocation.toPath(), destination.toPath());
        } catch (IOException e) {
            logger.log("Cannot response move to " + destination.getAbsolutePath());
            finishCallback.accept(false);
            return;
        }
        finishCallback.accept(true);
    }


    @Override
    public void start(Consumer<Double> tickCallback) {

        if (useCache && canUseCache()) {
            logger.log("It's possible to use cached steam library");
            finishCallback.accept(true);
            return;
        }

        if (tempLocation.exists() && !tempLocation.delete()) {
            logger.log("Cannot write to " + tempLocation.getAbsolutePath());
            finishCallback.accept(false);
            return;
        }
        loader.start(tickCallback);
    }

    private boolean canUseCache() {
        JSONObject json = helper.readJsonFromFile(Config.getSteamLibraryJsonFilePath());
        if (!json.has("applist") || !json.optJSONObject("applist").has("apps")) {
            logger.log("Seems like no steam shop library present");
            return false;
        }

        JSONArray apps = json.getJSONObject("applist").getJSONArray("apps");
        logger.log("Loaded " + apps.length() + " game ids");
        if (apps.length() <= 0) {
            logger.log("Empty steam shop library");
            return false;
        }
        return true;
    }
}
