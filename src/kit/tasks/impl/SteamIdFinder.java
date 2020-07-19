package kit.tasks.impl;

import kit.Game;
import kit.tasks.GameTask;
import kit.utils.Logger;
import org.json.JSONObject;

public class SteamIdFinder extends GameTask {

    public SteamIdFinder(Logger logger, JSONObject settings) {
        super(logger, settings);
    }

    @Override
    protected boolean processGame(Game game) {
        return false;
    }

    @Override
    public String getStatusString() {
        return "Getting Steam IDs";
    }
}
