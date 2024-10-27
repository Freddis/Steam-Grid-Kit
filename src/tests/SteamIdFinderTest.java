package tests;

import kit.Config;
import kit.State;
import kit.models.Game;
import kit.tasks.impl.SteamIdFinder;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import tests.utils.TestLogger;
import tests.utils.TestUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SteamIdFinderTest {
    @Test
    public void canFindSteamId() throws IOException, InterruptedException {
        // preparing data
        State state = TestUtils.createState();
        TestLogger logger = new TestLogger();
        JSONObject vdf = new JSONObject();
        Game original = new Game("game1",state.getPrimaryGamesDirectoryPath());
        state.setGames(Collections.singletonList(original));
        // copying steam library
        SteamIdFinder task = new SteamIdFinder(new TestLogger(),state.getJson());
        Path testSteamFilePath = TestUtils.getTestDataPath("/steam.json");
        FileUtils.copyFile(FileUtils.getFile(testSteamFilePath.toString()), FileUtils.getFile(Config.getSteamLibraryJsonFilePath()));

        // precheck
        Game unprocessedGame = state.getGames().get(0);
        assertEquals(0,unprocessedGame.getFoundSteamGames().size(),"Game already has steam ids attached");
        assertEquals(null,unprocessedGame.getSelectedSteamGame(),"Steam game already selected");
        // this one is threaded
        CountDownLatch latch = new CountDownLatch(1);
        task.start(state,result -> {
              latch.countDown();
        });
        latch.await();
        Game processedGame = state.getGames().get(0);
        assertEquals(15,processedGame.getFoundSteamGames().size(),"No steam ids were found");
        assertEquals(844020,processedGame.getSelectedSteamGame().getAppId(),"No steam id was set");
    }

    @Test
    public void gettingSteamGameByAltName() throws IOException, InterruptedException {
        // preparing data
        State state = TestUtils.createState();
        Game original = new Game("game1",state.getPrimaryGamesDirectoryPath());
        original.setAltName("Blasphemous 2");
        state.setGames(Collections.singletonList(original));
        // copying steam library
        SteamIdFinder task = new SteamIdFinder(new TestLogger(),state.getJson());
        Path testSteamFilePath = TestUtils.getTestDataPath("/steam.json");
        FileUtils.copyFile(FileUtils.getFile(testSteamFilePath.toString()), FileUtils.getFile(Config.getSteamLibraryJsonFilePath()));

        // precheck
        Game unprocessedGame = state.getGames().get(0);
        assertEquals(0,unprocessedGame.getFoundSteamGames().size(),"Game already has steam ids attached");
        assertEquals(null,unprocessedGame.getSelectedSteamGame(),"Steam game already selected");
        // this one is threaded
        CountDownLatch latch = new CountDownLatch(1);
        task.start(state,result -> {
            latch.countDown();
        });
        latch.await();
        Game processedGame = state.getGames().get(0);
        assertEquals(15,processedGame.getFoundSteamGames().size(),"No steam ids were found");
        assertEquals(2114740,processedGame.getSelectedSteamGame().getAppId(),"No steam id was set");
        assertEquals("Blasphemous 2",processedGame.getSelectedSteamGame().getName(),"No steam game name");
    }
}
