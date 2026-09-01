package tests;

import kit.Config;
import kit.State;
import kit.models.Game;
import kit.tasks.impl.CreateVdfFile;
import kit.tasks.impl.ShortcutParser;
import kit.tasks.impl.SteamGamesLoader;
import kit.utils.BinaryOperations;
import kit.vdf.VdfKey;
import kit.vdf.VdfReader;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import tests.utils.TestLogger;
import tests.utils.TestUtils;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;

public class SteamGamesLoaderTest {

    protected void cleanup() throws IOException {
        System.out.println("Cleaning up out dir.");
        assertTrue(TestUtils.getOutPath().toFile().exists(),"Out path should be found");
        Path backupPath = Paths.get(TestUtils.getOutPath().toUri()).resolve("backups");
        if(backupPath.toFile().exists()){
            System.out.println("Deleting backup dir");
            FileUtils.deleteDirectory(backupPath.toFile());
        }
    }

    @Test
    public void loadsDb() throws IOException, InterruptedException {
        cleanup();
        State state = TestUtils.createState();
        Path steamdbPath = Paths.get(Config.getSteamLibraryJsonFilePath()).toAbsolutePath();
        steamdbPath.toFile().delete();
        assertFalse(steamdbPath.toFile().exists());
        SteamGamesLoader task = new SteamGamesLoader(new TestLogger(),state.getJson());
        CountDownLatch latch = new CountDownLatch(1);
        task.start(state, TestUtils.getEmptyTaskCallback());
        task.onFinish(res -> {
            latch.countDown();
        });
        latch.await();
        assertTrue(steamdbPath.toFile().exists());
    }

}