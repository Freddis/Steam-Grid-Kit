package tests;

import kit.Config;
import kit.State;
import kit.models.Game;
import kit.tasks.impl.ShortcutParser;
import kit.utils.JsonHelper;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import tests.subclasses.ShortcutParserForTests;
import tests.utils.TestLogger;
import tests.utils.TestUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class ShortcutParserTest {

    @Test
    public void longToStringIrreversible() throws IOException {
        State state = TestUtils.createState();
        TestLogger logger = new TestLogger();
        JSONObject vdf = new JSONObject();
        Game original = new Game("Bioshock");
        Game anotherGame = new Game("Assasins creed");

        vdf.put("exe","\"\\Bioshock\\bioshock.exe\"");
        vdf.put("appname","Bioshock");
        ShortcutParserForTests task = new ShortcutParserForTests(logger,state.getJson());
        Game game = task.createGame(vdf,"E:\\Games");
        boolean shouldBeSame = game.isTheSame(original,state.getJson());
        boolean shouldntBeSame = game.isTheSame(anotherGame,state.getJson());
        assertTrue(shouldBeSame);
        assertFalse(shouldntBeSame);
    }

    @Test
    public void parsesShortcutFileCorrectly() throws IOException{
        State state = TestUtils.createState();
        ShortcutParser task = new ShortcutParser(new TestLogger(),state.getJson());

        task.start((param)->{});

        JsonHelper helper = new JsonHelper(new TestLogger());
        ArrayList<Game> games = state.getGames();
        assertNotEquals(games.size(),0);
    }

}