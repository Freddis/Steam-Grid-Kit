package kit.tasks.impl;

import kit.Config;
import kit.State;
import kit.interfaces.ILogger;
import kit.interfaces.ITask;
import kit.models.Game;
import kit.utils.JsonHelper;
import kit.utils.Logger;
import kit.utils.StringHelper;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Consumer;

public class GameFolderFinder implements ITask {
    private final ILogger logger;
    private Consumer<Boolean> finishCallback;

    public GameFolderFinder(ILogger logger, JSONObject settings) {
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

    public void start(State state, Consumer<Double> tickCallback) {
        String[] paths = state.getGamesDirectoryPaths();
        for(int i = 0; i < paths.length; i++) {
            this.processGameFolder(state,paths[i]);
        }
    }
    protected void processGameFolder(State state, String gamesDirectoryPath){
        this.logger.log("Reading game folders from " + gamesDirectoryPath);
        File dir = new File(gamesDirectoryPath);
        if (!dir.isDirectory()) {
            logger.log("Path is not a directory");
            this.finishCallback.accept(false);
            return;
        }
        JsonHelper jsonHelper = new JsonHelper(this.logger);
        boolean useCache = state.shouldUseCache();
        String[] ignored = state.getIgnoredFolders();
        File[] files = dir.listFiles();
        Arrays.sort(files);

        ArrayList<Game> list = state.getGames();
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
            Game game = new Game(file.getName(),gamesDirectoryPath);
            //If we already have the information about the game, it's better we just swap the game with the cached data
            Optional<Game> same = Arrays.stream(list.toArray(new Game[0])).filter(g -> g.isTheSame(game,state)).findFirst();
            //This one is also can be used, since it can more easily check the folders.
//            Optional<Game> same = Arrays.stream(list.toArray(new Game[0])).filter(g -> g.getDirectory().contains(game.getDirectory())).findFirst();
            if(!same.isPresent())
            {
                count++;
                list.add(game);
            }
        }
        logger.log("Added " + count + " games");

        state.setGames(list);
        this.finishCallback.accept(true);
    }

    @Override
    public void kill() {
        logger.log("Reading directories is not killable. I bet you will never read this message.");
    }
}
