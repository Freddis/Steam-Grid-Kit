package tests;

import kit.State;
import kit.models.Game;
import kit.tasks.impl.ExeFinder;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import tests.utils.TestLogger;
import tests.utils.TestUtils;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExeFinderTest {
    @Test
    public void canDetectSameGamesFromShortcuts() throws IOException, InterruptedException {
        State state = TestUtils.createState();
        TestLogger logger = new TestLogger();
        JSONObject vdf = new JSONObject();
        Game original = new Game("game1");
        state.setGames(Collections.singletonList(original));
        ExeFinder task = new ExeFinder(new TestLogger(),state.getJson());

        // this one is threaded
        CountDownLatch latch = new CountDownLatch(1);
        task.start(result -> {
              latch.countDown();
        });
        latch.await();

        Game updated = state.getGames().get(0);
        assertEquals(2,updated.getExecs().size());
        // replacements needed to be agnostic to the operating system
        assertEquals(
                state.getGamesDirectoryPath()
                    .resolve("game1\\folder2\\exe2.exe")
                    .toString()
                    .replace('/','\\'),
                updated
                    .getSelectedExe()
                    .replace('/','\\')
        );

    }
}
