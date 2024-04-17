package kit.tasks;

import kit.Config;
import kit.State;
import kit.interfaces.ILogger;
import kit.models.Game;
import kit.utils.JsonHelper;
import kit.utils.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public abstract class GameTask extends ListTask<Game> {

    private ArrayList<Game> games;
    protected JSONObject settings;
    protected boolean useCache;
    private final JsonHelper helper;
    private Game game;

    public GameTask(ILogger logger, JSONObject settings) {
        super(logger);
        this.settings = settings;
        this.helper = new JsonHelper(logger);
        this.useCache = settings.optBoolean(Config.Keys.USE_CACHE.getKey(),false);
    }

    @Override
    protected List<Game> getList() {
        if(this.game != null)
        {
            ArrayList<Game> list = new ArrayList<>();
            list.add(game);
            return list;
        }
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

    @Override
    public void start(State state, Consumer<Double> tickCallback) {
        //before start
        games = this.helper.toList(Game::new, this.settings.optJSONArray(Config.Keys.GAMES.getKey()));
        if(this.game != null)
        {
            Game copy = Arrays.stream(games.toArray(new Game[0])).filter(g -> g.getDirectory().equals(game.getDirectory())).findFirst().get();
            int i = games.indexOf(copy);
            games.remove(i);
            games.add(i,game);
        }
        super.start(state,tickCallback);
    }

    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }

    protected abstract boolean processGame(Game game);

    public void setGame(Game game) {
        this.game = game;
    }
}
