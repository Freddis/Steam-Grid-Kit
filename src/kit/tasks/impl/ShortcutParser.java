package kit.tasks.impl;

import kit.Config;
import kit.interfaces.ITask;
import kit.models.Game;
import kit.utils.JsonHelper;
import kit.utils.Logger;
import kit.vdf.VdfReader;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ShortcutParser implements ITask {

    private final Logger logger;
    private final JSONObject settings;
    private final boolean useCache;
    private Consumer<Boolean> finishCallback;

    public ShortcutParser(Logger logger, JSONObject settings) {
        this.logger = logger;
        this.settings = settings;
        this.useCache = settings.optBoolean(Config.Keys.USE_CACHE.getKey(), false);
        finishCallback = a -> {
        };
    }

    @Override
    public String getStatusString() {
        return "Parsing existing shortcuts";
    }

    @Override
    public void onFinish(Consumer<Boolean> finishCallback) {
        this.finishCallback = finishCallback;
    }

    @Override
    public void start(Consumer<Double> tickCallback) {
        File file = this.copyVdfFile();
        if (file == null) {
            finishCallback.accept(false);
            return;
        }

        VdfReader reader = new VdfReader();
        JSONArray data = reader.parse(file);

        String gamePath = settings.optString(Config.Keys.LOCAL_GAMES_DIRECTORY_PATH.getKey(), null);
        if (gamePath == null) {
            gamePath = settings.optString(Config.Keys.GAMES_DIRECTORY_PATH.getKey(), null);
        }

        JsonHelper helper = new JsonHelper(logger);
        ArrayList<Game> games = new ArrayList<>();
//        if(this.useCache)
//        {
//            games = helper.toList(Game::new, this.settings.optJSONArray(Config.Keys.GAMES.getKey()));
//        }
        for (int i = 0; i < data.length(); i++) {
            JSONObject row = data.getJSONObject(i);
            Game game =  this.createGame(row,gamePath);
            Optional<Game> original = Arrays.stream(games.toArray(new Game[0])).filter(g -> g.isTheSame(game)).findFirst();
            if (!original.isPresent()) {
                games.add(game);
            }
        }

        JSONArray arr = helper.toJsonArray(games.toArray(new Game[0]));
        settings.put(Config.Keys.GAMES.getKey(), arr);

        finishCallback.accept(true);
    }

    private Game createGame(JSONObject row, String path) {
        String separator = "\\\\";
        if(path.charAt(path.length()-1) != '\\')
        {
            path += '\\';
        }
        String exe = row.getString("exe");
        exe = exe.substring(1,exe.length()-1);

        String directory = exe;
        boolean inGamesFolder = exe.indexOf(path) == 0;
        if(inGamesFolder)
        {
            String relativeExe = exe.replace(path,"");
            String[] parts = relativeExe.split(separator);
            directory = parts[0];
        }

        Game game = new Game(directory);
        game.setAltName(row.getString("appname"));
        game.getExecs().add(exe);
        game.setVdf(row);

        return game;
    }

    private File copyVdfFile() {
        String filePath = settings.optString(Config.Keys.VDF_FILE.getKey(), null);
        logger.log("Trying to copy vdf from " + filePath);
        String localVdfPath = Config.getVdfFilePath();
        File destination = new File(localVdfPath);
        if (destination.exists() && destination.canRead() && this.useCache) {
            logger.log("Using cached VDF file");
            return destination;
        }
        File existingVdfFile = new File(filePath);
        if (!existingVdfFile.exists() || !existingVdfFile.canRead()) {
            logger.log("Can't read steam VDF file");
            return null;
        }
        if (destination.exists() && !destination.delete()) {
            logger.log("Couldn't wipe cached VDF data");
            return null;
        }

        try {
            Files.copy(existingVdfFile.toPath(), destination.toPath());
        } catch (IOException e) {
            logger.log("Unable to copy file");
            return null;
        }
        return destination;
    }

    @Override
    public void kill() {
        //No need to kill, this one is quick AF
    }
}
