package kit.tasks.impl;

import kit.Config;
import kit.interfaces.ITask;
import kit.models.Game;
import kit.utils.JsonHelper;
import kit.utils.Logger;
import kit.utils.StringHelper;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

public class GameFolderFinder implements ITask {
    private final JSONObject settings;
    private final Logger logger;
    private Consumer<Boolean> finishCallback;

    public GameFolderFinder(Logger logger, JSONObject settings) {
        this.settings = settings;
        this.logger = logger;
        this.finishCallback = a -> {
        };
    }

    @Override
    public String getStatusString() {
        return "Reading game folders";
    }

    public void onFinish(Consumer<Boolean> finishCallback) {
        this.finishCallback = finishCallback;
    }

    public void start(Consumer<Double> tickCallback) {
        String path = settings.optString(Config.Keys.GAMES_DIRECTORY_PATH.getKey(), null);
        this.logger.log("Reading game folders from " + path);
        File dir = new File(path);
        if (!dir.isDirectory()) {
            logger.log("Path is not a directory");
            this.finishCallback.accept(false);
            return;
        }
        JsonHelper jsonHelper = new JsonHelper(this.logger);
        boolean useCache = settings.optBoolean(Config.Keys.USE_CACHE.getKey(), false);
        String[] ignored = jsonHelper.toStringArray(settings.optJSONArray(Config.Keys.IGNORED_FOLDERS_NAMES.getKey()));
        File[] files = dir.listFiles();
        ArrayList<Game> list = jsonHelper.toList(Game::new, settings.optJSONArray(Config.Keys.GAMES.getKey()));
        //I don't think the next might happen, but better safe than sorry, right?
        if (files == null) {
            logger.log("Couldn't obtain files");
            return;
        }
        logger.log("Found " + files.length + " files");

        int count = 0;
        for (File file : files) {
            if (!file.isDirectory()) {
                continue;
            }
            if (StringHelper.hasSubstrings(file.getName(), ignored, true)) {
                logger.log("File " + file.getName() + " ignored");
                continue;
            }
            Game game = new Game(file.getName());
            //If we already have the information about the game, it's better we just swap the game with the cached data
            Optional<Game> same = Arrays.stream(list.toArray(new Game[0])).filter(g -> g.isTheSame(game)).findFirst();
            if(!same.isPresent())
            {
                count++;
                list.add(game);
            }
        }
        logger.log("Added " + count + " games");

        settings.put(Config.Keys.GAMES.getKey(), jsonHelper.toJsonArray(list.toArray(new Game[]{})));
        this.finishCallback.accept(true);
    }

    @Override
    public void kill() {
        logger.log("Reading directories is not killable. I bet you will never read this message.");
    }
}
