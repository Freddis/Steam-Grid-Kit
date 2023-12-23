package tests;

import kit.Config;
import kit.models.Game;
import kit.tasks.impl.CreateVdfFile;
import kit.utils.BinaryOperations;
import kit.utils.Logger;
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
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BinaryOperationsTest {

    @Test
    public void longToStringIrreversible(){
        BinaryOperations utils = new BinaryOperations();
        for(int i =0; i < 1000; i++) {
            Long number = utils.generateLong();
            String string = utils.longToString(number);
            Long number2 = utils.stringToLong(string);
            String string2 = utils.longToString(number2);
            assertEquals(number, number2);
            assertEquals(string, string2);
        }
    }

}