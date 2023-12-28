package kit.tasks.impl;

import kit.Config;
import kit.interfaces.ILogger;
import kit.interfaces.ITask;
import kit.models.Game;
import kit.utils.JsonHelper;
import kit.utils.Logger;
import kit.vdf.VdfKey;
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

public class ShortcutParser implements ITask {

    private final ILogger logger;
    private final JSONObject settings;
    private final boolean useCache;
    private Consumer<Boolean> finishCallback;

    public ShortcutParser(ILogger logger, JSONObject settings) {
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
        if (gamePath == null || gamePath.isEmpty()) {
            gamePath = settings.optString(Config.Keys.GAMES_DIRECTORY_PATH.getKey(), null);
        }

        JsonHelper helper = new JsonHelper(logger);
        String[] ignored = helper.toStringArray(settings.optJSONArray(Config.Keys.IGNORED_FOLDERS_NAMES.getKey()));
        ArrayList<Game> games = helper.toList(Game::new, settings.optJSONArray(Config.Keys.GAMES.getKey()));

        for (int i = 0; i < data.length(); i++) {
            JSONObject row = data.getJSONObject(i);
            logger.log("Processing game: "+row.getString(VdfKey.EXE_PATH.getKey()));
            Game game =  this.createGame(row,gamePath);
            logger.log("Directory: "+game.getDirectory());
            Optional<Game> original = Arrays.stream(games.toArray(new Game[0])).filter(g -> g.isTheSame(game, settings)).findFirst();
            boolean gameIgnored = Arrays.stream(ignored).anyMatch(str -> str.equals(game.getDirectory()));
            if(gameIgnored)
            {
                logger.log("Game Ignored "+ game.getDirectory());
                continue;
            }
            if (!original.isPresent()) {
                games.add(game);
            }
            else {
                original.get().setVdf(row);
            }
        }

        JSONArray arr = helper.toJsonArray(games.toArray(new Game[0]));
        settings.put(Config.Keys.GAMES.getKey(), arr);

        finishCallback.accept(true);
    }

    protected Game createGame(JSONObject vdf, String gamesFolderPath) {

        String exe = vdf.getString(VdfKey.EXE_PATH.getKey());
        if(exe.charAt(0) == '"') {
            exe = exe.substring(1,exe.length()-1);
        }
        String directoryPath = exe;
        if(directoryPath.charAt(0) == '\\') {
            // exe should start with \, while directory shouldn't
            directoryPath = directoryPath.substring(1);
        }
        boolean inGamesFolder = !gamesFolderPath.isEmpty() && exe.indexOf(gamesFolderPath+"\\") == 0;
        if(inGamesFolder) {
            directoryPath = exe.replace(gamesFolderPath+"\\","");
        }
        String separatorRegex = "\\\\";
        String[] parts = directoryPath.split(separatorRegex);
        String directory = parts[0];

        Game game = new Game(directory);
        String name = vdf.getString(VdfKey.APP_NAME.getKey());
        if(!name.equals(game.getDirectory())) {
            game.setAltName(name);
        }
        game.getExecs().add(exe);
        game.setSelectedExe(exe);
        game.setVdf(vdf);

        return game;
    }

    protected File copyVdfFile() {
        String filePath = settings.optString(Config.Keys.VDF_FILE.getKey(), null);
        if(filePath == null)
        {
            logger.log("VDF file is not set");
            return null;
        }
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
