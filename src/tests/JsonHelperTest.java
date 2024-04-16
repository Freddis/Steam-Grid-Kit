package tests;

import kit.utils.JsonHelper;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import tests.utils.TestLogger;
import tests.utils.TestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class JsonHelperTest {

    @Test
    void preservesSpecialSymbolsCorrectly() throws IOException {
        JsonHelper helper = new JsonHelper(new TestLogger());
        Path input = TestUtils.getTestDataPath("/json-helper/special-symbols.json");
        Path output = TestUtils.getTestDataPath("/output/special-symbols-output.json");
        assertTrue(input.toFile().exists(),"Input file doesn't exist");
        if(output.toFile().exists()){
            output.toFile().delete();
        }
        assertFalse(output.toFile().exists(),"Output file already exists");

        JSONObject data = helper.readJsonFromFile(input.toString());
        helper.writeJsonToFile(output.toString(),data);

        assertTrue(output.toFile().exists(),"Output file hasn't been created");
        byte[] inputBytes = Files.readAllBytes(input);
        byte[] outputBytes = Files.readAllBytes(output);
        assertArrayEquals(inputBytes,outputBytes,"Bytes have changed after reading");
    }
}
