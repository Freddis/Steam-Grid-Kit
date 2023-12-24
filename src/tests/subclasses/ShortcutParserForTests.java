package tests.subclasses;

import kit.interfaces.ILogger;
import kit.models.Game;
import kit.tasks.impl.ShortcutParser;
import org.json.JSONObject;

public class ShortcutParserForTests extends ShortcutParser {

    public ShortcutParserForTests(ILogger logger, JSONObject settings) {
        super(logger, settings);
    }

    @Override
    public Game createGame(JSONObject vdf, String gamesFolderPath) {
        return super.createGame(vdf, gamesFolderPath);
    }
}
