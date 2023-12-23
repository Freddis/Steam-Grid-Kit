package tests;

import kit.Config;
import kit.tasks.impl.CreateVdfFile;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import tests.utils.TestLogger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;

public class CreateVdfFileTest {

    protected Path getOutPath() {
        Path outPath = Paths.get("","out/test/SteamGridKit").toAbsolutePath();
        return outPath;
    }
    protected void cleanup() throws IOException {
        System.out.println("Cleaning up out dir.");
        assertTrue(getOutPath().toFile().exists(),"Out path should be found");
        Path backupPath = Paths.get(getOutPath().toUri()).resolve("backups");
        if(backupPath.toFile().exists()){
            System.out.println("Deleting backup dir");
            FileUtils.deleteDirectory(backupPath.toFile());
        }
    }

    @Test
    public void createsBackup() throws IOException {
        cleanup();
        TestLogger log = new TestLogger();
        JSONObject settings = new JSONObject();
        Path vdfPath = Paths.get("", "test-data/test.vdf").toAbsolutePath();
        Path backupPath = Paths.get(getOutPath().toUri()).resolve("backups");
        assertTrue(vdfPath.toFile().exists());
        assertFalse(backupPath.toFile().exists());
        settings.put(Config.Keys.VDF_FILE.getKey(),vdfPath.toString());
        CreateVdfFile task = new CreateVdfFile(log,settings);

        task.start(param -> {

        });

        assertTrue(backupPath.toFile().exists());
        assertEquals(backupPath.toFile().listFiles().length,1);
        assertThat(backupPath.toFile().listFiles()[0].getName(),containsString(".vdf"));
        assertEquals(readFileToString(backupPath.toFile().listFiles()[0],StandardCharsets.UTF_8),FileUtils.readFileToString(vdfPath.toFile(), StandardCharsets.UTF_8),"Backup contents is wrong");
    }
}