package tests;

import kit.State;
import kit.models.Game;
import kit.tasks.impl.GameFolderFinder;
import org.junit.jupiter.api.Test;
import tests.utils.TestLogger;
import tests.utils.TestUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class GameFolderFinderTest {
    @Test
    public void longToStringIrreversible() throws IOException {
        State state = TestUtils.createState();
        GameFolderFinder task = new GameFolderFinder(new TestLogger(),state.getJson());
        task.start(state,TestUtils.getEmptyTaskCallback());

        ArrayList<Game> games = state.getGames();
        assertEquals(games.size(),2);
        assertEquals("game1",games.get(0).getDirectory());
        assertNotEquals(0,games.get(0).getAppId());
        assertEquals("game2",games.get(1).getDirectory());
        assertNotEquals(0,games.get(1).getAppId());
    }
    @Test
    public void allowsToSearchInMultipleFolders() throws IOException {
        State state = TestUtils.createState();
        Path gamesPath = TestUtils.getTestDataPath("/games2");
        state.addAdditionalGamesDirectory(gamesPath.toString());
        GameFolderFinder task = new GameFolderFinder(new TestLogger(),state.getJson());
        task.start(state,TestUtils.getEmptyTaskCallback());

        ArrayList<Game> games = state.getGames();
        assertEquals(2,state.getGamesDirectoryPaths().length);
        assertEquals(3,games.size());
        assertEquals("game3",games.get(2).getDirectory());
    }
}
