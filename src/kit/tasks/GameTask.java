package kit.tasks;

import kit.Config;
import kit.Game;
import kit.utils.JsonHelper;
import kit.utils.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class GameTask extends ListTask<Game> {

    private final ArrayList<Game> games;
    protected JSONObject settings;
    protected boolean useCache;
    private final JsonHelper helper;

    public GameTask(Logger logger, JSONObject settings) {
        super(logger);
        this.settings = settings;
        this.helper = new JsonHelper(logger);
        this.games = this.helper.toList(Game::new, this.settings.optJSONArray(Config.Keys.GAMES.getKey()));
        this.useCache = settings.optBoolean(Config.Keys.USE_CACHE.getKey(),false);
    }

    @Override
    protected List<Game> getList() {
        return games;
    }

    @Override
    protected boolean process(Game game) {
        boolean result = processGame(game);
        if (result) {
            JSONArray arr = helper.toJsonArray(games.toArray(new Game[0]));
            settings.put(Config.Keys.GAMES.getKey(), arr);
        }
        return result;
    }

    protected abstract boolean processGame(Game game);
}
