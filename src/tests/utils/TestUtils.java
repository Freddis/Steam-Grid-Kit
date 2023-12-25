package tests.utils;

import kit.Config;
import kit.State;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class TestUtils {

    static public Path getOutPath() {
        Path outPath = Paths.get("","out/production/SteamGridKit").toAbsolutePath();
        return outPath;
    }

    static public Path getTestDataPath() {
        return getTestDataPath("");
    }
    static public Path getTestDataPath(String extraPath) {
        String strPath = "test-data";
        if(!extraPath.isEmpty()){
            strPath = strPath + extraPath;
        }
        Path path = Paths.get(strPath).toAbsolutePath();
        return path;
    }

    public static JSONObject createSettings() {
        Path vdfPath = TestUtils.getTestDataPath("/test.vdf");
        Path gamesPath = TestUtils.getTestDataPath("/games");
        JSONObject settings = new JSONObject();
        settings.put(Config.Keys.GAMES_DIRECTORY_PATH.getKey(),gamesPath.toString());
        settings.put(Config.Keys.LOCAL_GAMES_DIRECTORY_PATH.getKey(),"");
        settings.put(Config.Keys.VDF_FILE.getKey(),vdfPath.toString());
        return settings;
    }

    public static Consumer<Double> getEmptyTaskCallback() {
        return (param) -> {};
    }

    public static State createState() throws IOException {
        State state = new State(createSettings(),new TestLogger());
        // overriding vdf path since we don't want to corrupt the test vdf file
        Path newVdfFilePath = TestUtils.getOutPath().resolve("newvdf.vdf").toAbsolutePath();
        if(newVdfFilePath.toFile().exists()){
            newVdfFilePath.toFile().delete();
        }
        Files.copy(state.getVdfFilePath(),newVdfFilePath);
        state.setVDFFilePath(newVdfFilePath);

        return state;
    }
}
