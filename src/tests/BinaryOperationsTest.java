package tests;

import kit.utils.BinaryOperations;
import org.junit.jupiter.api.Test;
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