package tests;

import kit.Config;
import kit.State;
import kit.models.Game;
import kit.tasks.impl.CreateVdfFile;
import kit.tasks.impl.ShortcutParser;
import kit.utils.BinaryOperations;
import kit.vdf.VdfKey;
import kit.vdf.VdfReader;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import tests.utils.TestLogger;
import tests.utils.TestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;

public class CreateVdfFileTest {

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
    public void createsBackup() throws IOException {
        cleanup();
        State state = TestUtils.createState();
        Path backupPath = Paths.get(TestUtils.getOutPath().toUri()).resolve("backups");
        assertTrue(state.getVdfFilePath().toFile().exists());
        assertFalse(backupPath.toFile().exists());


        CreateVdfFile task = new CreateVdfFile( new TestLogger(),state.getJson());
        task.start(state,TestUtils.getEmptyTaskCallback());

        assertTrue(backupPath.toFile().exists());
        assertEquals(backupPath.toFile().listFiles().length,1);
        assertThat(backupPath.toFile().listFiles()[0].getName(),containsString(".vdf"));
        assertEquals(readFileToString(backupPath.toFile().listFiles()[0],StandardCharsets.UTF_8),
                FileUtils.readFileToString(state.getVdfFilePath().toFile(), StandardCharsets.UTF_8),
                "Backup contents is wrong");
    }
    @Test
    public void createsVdfFile() throws IOException {
        State state = TestUtils.createState();
        Game game = new Game("game1",state.getPrimaryGamesDirectoryPath());
        game.getExecs().add("E:\\Games\\game1\\folder1\\exe1.exe");
        ArrayList<Game> list = state.getGames();
        list.add(game);
        state.setGames(list);
        state.setGamesDirectory("E:\\Games");

        CreateVdfFile task = new CreateVdfFile( new TestLogger(),state.getJson());
        task.start(state,TestUtils.getEmptyTaskCallback());

        VdfReader reader = new VdfReader();
        File vdf = new File(state.getVdfFilePath().toString());
        JSONArray lines = reader.parse(vdf);
        assertEquals(1,lines.length());
        JSONObject line1 = lines.getJSONObject(0);
        JSONObject appid = line1.optJSONObject(VdfKey.APP_ID.getKey());
        assertEquals(game.getAppId(),appid.optLong("value"),"The appId is not the same");
        assertEquals("\"E:\\Games\\game1\\folder1\\exe1.exe\"", line1.getString(VdfKey.EXE_PATH.getKey()));
    }

    @Test
    public void vdfFileCreatedByAppIdenticalToWhatSteamCreates() throws IOException{
        State state = TestUtils.createState();
        state.setGamesDirectory("E:\\Games");

        assertEquals(0,state.getGames().size());
        ShortcutParser task = new ShortcutParser(new TestLogger(),state.getJson());
        task.start(state,TestUtils.getEmptyTaskCallback());
        CreateVdfFile task2 = new CreateVdfFile(new TestLogger(),state.getJson());
        task2.start(state,TestUtils.getEmptyTaskCallback());


        assertEquals(3,state.getGames().size());
        byte[] oldBytes = Files.readAllBytes(TestUtils.getTestDataPath("/steam_shortcuts.vdf"));
        String oldContent = BinaryOperations.convertBytesToString(oldBytes);
        byte[] newBytes = Files.readAllBytes(state.getVdfFilePath());
        String newContent = BinaryOperations.convertBytesToString(newBytes);

        // this check is redundant, but I want to keep the content variables in case the test fails
        String diff = TestUtils.getStringDiff(oldContent,newContent);
        assertEquals("",diff);
        assertEquals(oldContent,newContent);
        assertArrayEquals(oldBytes,newBytes);
    }
}