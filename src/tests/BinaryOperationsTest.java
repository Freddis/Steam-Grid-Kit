package tests;

import kit.utils.BinaryOperations;
import org.junit.jupiter.api.Test;
import tests.utils.TestUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BinaryOperationsTest {

    @Test
    public void steamGeneratedLongsAreReversible(){
        // took this one from steam
        // it is bytes converted to chars(16 bit)
        String fromSteam = "Nﾊ0￁";
        Long number = BinaryOperations.stringToLong(fromSteam);
        assertEquals(3241183822L,number);
        String newString = BinaryOperations.longToString(number);
        assertEquals(fromSteam, newString);
    }
    @Test
    public void longToStringIsReversible(){
        for(int i =0; i < 1000; i++) {
            Long number = BinaryOperations.generateLong();
            String string = BinaryOperations.longToString(number);
            Long number2 = BinaryOperations.stringToLong(string);
            String string2 = BinaryOperations.longToString(number2);
            assertEquals(number, number2);
            assertEquals(string, string2);
        }
    }
    @Test
    public void writeToFilesIsReversable() throws IOException {
        File file = TestUtils.getOutPath().resolve("longtest").toFile();
        for(int i =0; i < 30; i++) {
            Long number = BinaryOperations.generateLong();
            String whatWeWrite = BinaryOperations.longToString(number);
            FileWriter writer = new FileWriter(file);
            writer.write(whatWeWrite);
            writer.close();
            byte[] bytes = Files.readAllBytes(file.toPath());
            String whatWeRead = new String(bytes);
            assertEquals(whatWeWrite,whatWeRead,"What we write must equal to what we then read");
            Long number2 = BinaryOperations.stringToLong(whatWeRead);
            assertEquals(number,number2,"The number we write must equal when we then read");
        }
    }

}