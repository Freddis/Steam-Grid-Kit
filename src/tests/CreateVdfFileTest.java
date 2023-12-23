package kit.tests;

import kit.Config;
import kit.tasks.impl.CreateVdfFile;
import kit.tests.utils.TestLogger;
import org.json.JSONObject;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class CreateVdfFileTest {

    @Test
    public void start() {
        TestLogger log = new TestLogger();
        JSONObject settings = new JSONObject();
        Path path = Paths.get("", "test-data/test.vdf").toAbsolutePath();
        assertTrue(path.toFile().exists());
        settings.put(Config.Keys.VDF_FILE.getKey(),path.toString());
        CreateVdfFile task = new CreateVdfFile(log,settings);
        task.start(param -> {

        });

    }
}