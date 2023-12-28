package kit;

import kit.interfaces.ILogger;
import kit.models.Game;
import kit.utils.JsonHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import tests.utils.TestLogger;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class State {

    private final JsonHelper helper;
    protected JSONObject state;

    public State(JSONObject state, ILogger logger){
        this.state = state;
        this.helper = new JsonHelper(logger);
    }

    public ArrayList<Game> getGames(){
        ArrayList<Game> games = this.helper.toList(Game::new, this.state.optJSONArray(Config.Keys.GAMES.getKey()));
        return games;
    }

    public JSONObject getJson() {
        return this.state;
    }

    public void setGames(List<Game> list) {
        JsonHelper helper = new JsonHelper(new TestLogger());
        Game[] games = list.toArray(new Game[0]);
        JSONArray newGames = this.helper.toJsonArray(games);
        this.state.put(Config.Keys.GAMES.getKey(), newGames);

    }

    public Path getVdfFilePath() {
        String result = this.state.getString(Config.Keys.VDF_FILE.getKey());
        File file = new File(result);
        return file.toPath();
    }

    public void setVDFFilePath(Path vdfFilePath) {
        this.state.put(Config.Keys.VDF_FILE.getKey(), vdfFilePath.toString());
    }

    public void setGamesDirectory(String string) {
        this.state.put(Config.Keys.GAMES_DIRECTORY_PATH.getKey(), string);
    }

    public Path getGamesDirectoryPath() {
        String result = this.state.getString(Config.Keys.GAMES_DIRECTORY_PATH.getKey());
        File file = new File(result);
        return file.toPath();
    }
}
