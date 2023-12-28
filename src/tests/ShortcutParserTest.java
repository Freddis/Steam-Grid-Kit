package tests;

import kit.State;
import kit.models.Game;
import kit.tasks.impl.ShortcutParser;
import kit.utils.JsonHelper;
import kit.vdf.VdfKey;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import tests.subclasses.ShortcutParserForTests;
import tests.utils.TestLogger;
import tests.utils.TestUtils;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class ShortcutParserTest {

    @Test
    public void canDetectSameGamesFromShortcuts() throws IOException {
        State state = TestUtils.createState();
        TestLogger logger = new TestLogger();
        JSONObject vdf = new JSONObject();
        Game original = new Game("Bioshock");
        Game anotherGame = new Game("Assasins creed");
        vdf.put(VdfKey.EXE_PATH.getKey(), "\"\\Bioshock\\bioshock.exe\"");
        vdf.put(VdfKey.APP_NAME.getKey(), "Bioshock");

        ShortcutParserForTests task = new ShortcutParserForTests(logger,state.getJson());
        Game game = task.createGame(vdf,"E:\\Games");

        boolean shouldBeSame = game.isTheSame(original,state.getJson());
        boolean shouldntBeSame = game.isTheSame(anotherGame,state.getJson());
        assertTrue(shouldBeSame);
        assertFalse(shouldntBeSame);
    }

    @Test
    public void parsesSteamShortcutFileCorrectly() throws IOException{
        State state = TestUtils.createState();
        state.setGamesDirectory("E:\\Games");
        ShortcutParser task = new ShortcutParser(new TestLogger(),state.getJson());

        task.start((param)->{});

        JsonHelper helper = new JsonHelper(new TestLogger());
        ArrayList<Game> games = state.getGames();
        assertEquals(games.size(),3);
    }

}