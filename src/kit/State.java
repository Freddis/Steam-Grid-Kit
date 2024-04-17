package kit;

import com.sun.org.apache.xerces.internal.xs.StringList;
import kit.interfaces.ILogger;
import kit.models.Game;
import kit.utils.JsonHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import tests.utils.TestLogger;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
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

    public boolean shouldUseCache() {
        boolean result = this.state.optBoolean(Config.Keys.USE_CACHE.getKey(), false);
        return result;
    }

    public String[] getIgnoredFolders() {
        String[] result = this.helper.toStringArray(state.optJSONArray(Config.Keys.IGNORED_FOLDERS_NAMES.getKey()));
        return result;
    }

    public String getPrimaryGamesDirectoryPath() {
        String result = this.state.optString(Config.Keys.GAMES_DIRECTORY_PATH.getKey(), null);
        return result;
    }

    public String[] getAdditionalGamesDirectoryPaths() {
        String[] extra = this.helper.toStringArray(state.optJSONArray(Config.Keys.ADDITIONAL_GAMES_DIRECTORY_PATHS.getKey()));
        return extra;
    }
    public String[] getGamesDirectoryPaths() {
        String primary = this.getPrimaryGamesDirectoryPath();
        String[] extra = this.getAdditionalGamesDirectoryPaths();
        ArrayList<String> list = new ArrayList<>();
        list.add(primary);
        list.addAll(Arrays.asList(extra));
        return list.toArray(new String[0]);
    }
    public void addAdditionalGamesDirectory(String newpath) {
        String[] directories = this.getAdditionalGamesDirectoryPaths();
        ArrayList<String> list = new ArrayList<>(Arrays.asList(directories));
        list.add(newpath);
        String[] newList =  list.toArray(new String[0]);
        this.state.put(Config.Keys.ADDITIONAL_GAMES_DIRECTORY_PATHS.getKey(), this.helper.toJsonArray(newList));
    }

    public void removeAdditionalGamesDirectory(int index) {
        String[] paths = this.getAdditionalGamesDirectoryPaths();
        String[] newPaths = new String[paths.length-1];
        int counter = 0;
        for(int i =0; i < paths.length; i++){
            if(index != i){
                newPaths[counter++] = paths[i];
            }
        }
        this.state.put(Config.Keys.ADDITIONAL_GAMES_DIRECTORY_PATHS.getKey(), this.helper.toJsonArray(newPaths));
    }
}
