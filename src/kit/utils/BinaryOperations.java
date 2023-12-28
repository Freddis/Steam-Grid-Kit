package kit.utils;
import org.json.JSONObject;
import java.nio.ByteBuffer;
import java.util.Random;

public class BinaryOperations {

    /**
     * This needed when we want to write VDF file. We need to convert unsigned chars(16) back to signed bytes
     * @param str string
     * @return array of proper signed bytes
     */
    public static byte[] convertStringToBytes(String str) {
        char[] chars = str.toCharArray();
        byte[] bytes = new byte[str.length()];
        for(int i =0; i < chars.length; i++){
            bytes[i] = (byte) chars[i];
        }
        return bytes;
    }

    /**
     * This needed when we want to read VDF file. We need to convert signed bytes to unsigned char(16).
     * This will prevent data loss on backward conversion.
     * @param bytes Bytes of the file
     * @return array of proper signed bytes
     */
    public static String convertBytesToString(byte[] bytes){
        char[] chars = new char[bytes.length];
        for(int i =0; i < bytes.length; i++){
            chars[i] = (char) bytes[i];
        }
        String result = new String(chars);
        return result;
    }

    public static Long generateLong() {
        Random rd = new Random();

        long low = 2653195287L;
        long high = 2953195287L;
        long number = (long)(Math.random()*(high-low)) +low;
        return number;
    }

    public static Long stringToLong(String str){
        char a = str.charAt(0);
        char b = str.charAt(1);
        char c = str.charAt(2);
        char d = str.charAt(3);
        byte[] crappySignedBytes = new byte[]{(byte) a, (byte) b, (byte) c, (byte) d};
        byte[] crappySignedBytes2 = str.getBytes();
        char[] unsignedGoodBytes = new char[crappySignedBytes.length];
        for(int i = 0; i < crappySignedBytes.length; i++ )
        {
            unsignedGoodBytes[i] = (char) crappySignedBytes[i];
            if(crappySignedBytes[i] < 0)
            {
                unsignedGoodBytes[i] += 256;
            }
        }
        String dateStr = new String(unsignedGoodBytes);
        JSONObject date = new JSONObject();
        StringBuilder hexStrBuilder = new StringBuilder();
        for(int i = unsignedGoodBytes.length-1; i >= 0; i--){
            hexStrBuilder.append(String.format("%02X",crappySignedBytes[i]));
        }
        String hexValue = hexStrBuilder.toString();
        long number = Long.parseLong(hexValue,16);
        return number;
    }

    public String longToString(Long number){
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(number);
        byte[] bytes = buffer.array();
        byte[] crappySignedBytes = new byte[]{bytes[bytes.length-1],bytes[bytes.length-2],bytes[bytes.length-3],bytes[bytes.length-4]};
        char[] unsignedGoodBytes = new char[crappySignedBytes.length];
        for(int i = 0; i < crappySignedBytes.length; i++ ) {
            unsignedGoodBytes[i] = (char) crappySignedBytes[i];
        }
        String dateStr = new String(unsignedGoodBytes);
        return dateStr;
    }
}
