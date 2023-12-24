package tests;

import kit.Config;
import kit.models.Game;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import tests.subclasses.ShortcutParserForTests;
import tests.utils.TestLogger;

import static org.junit.jupiter.api.Assertions.*;

public class ShortcutParserTest {

    @Test
    public void longToStringIrreversible(){
        JSONObject settings = new JSONObject();
        settings.put(Config.Keys.GAMES_DIRECTORY_PATH.getKey(),"E:\\Games");
        settings.put(Config.Keys.LOCAL_GAMES_DIRECTORY_PATH.getKey(),"");
        TestLogger logger = new TestLogger();
        JSONObject vdf = new JSONObject();
        Game original = new Game("Bioshock");
        Game anotherGame = new Game("Assasins creed");

        vdf.put("exe","\"\\Bioshock\\bioshock.exe\"");
        vdf.put("appname","Bioshock");
        ShortcutParserForTests task = new ShortcutParserForTests(logger,settings);
        Game game = task.createGame(vdf,"E:\\Games");
        boolean shouldBeSame = game.isTheSame(original,settings);
        boolean shouldntBeSame = game.isTheSame(anotherGame,settings);
        assertTrue(shouldBeSame);
        assertFalse(shouldntBeSame);
    }

}