package kit.vdf;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class VdfReader {

    final char x00 = 0x00; //separator or list
    final char x01 = 0x01; //string
    final char x02 = 0x02; //bool
    final char x08 = 0x08; //ending
    char[] originalBytes;

    public JSONArray parse(File file) {
        String content;
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            ;
//            content = new String(bytes, StandardCharsets.UTF_8);
            char[] chars = new char[bytes.length];
            originalBytes = chars;
            //byte is signed??? overflows and needs to be corrected
            //it's funny though that readAllBytes can read only half of the bytes out there xD

            //Since the string contains binary data for dates, it's impossible to read it as UTF-8, since it will destroy the dates as they will be treated as multibyte characters
            //We can't read the string as single byte either, since bytes are not signed and get overflow in Java
            //We can't fix the overflow right here, because it will drive dates unreadable
            //So the trick is to convert bytes into chars, which will preserve the date and are compatible with ASCII 2 key names for fields
            //And then convert it to either UTF-8 or UBytes when we find UTF-8 String values or dates.
            for (int i = 0; i < bytes.length; i++) {
                chars[i] = (char) bytes[i];
//                if(bytes[i] < 0)
//                {
//                    chars[i] += 256;
//                }
            }
            content = new String(chars);
        } catch (IOException e) {
            return null;
        }

        String start = x00 + "shortcuts" + x00 + x00;
        String end = "" + x08 + x08 + x08 + x08;
        String lineDelimiter = "" + x08 + x08 + x00;
        content = content.replace(start, "").substring(0, content.length() - start.length() - end.length());
        String[] lines = content.split(lineDelimiter);

        JSONArray arr = new JSONArray();
        for (String line : lines) {
            JSONObject res = this.parseLine(line);
            arr.put(res);
        }
        return arr;
    }

    private JSONObject parseLine(String line) {
        JSONObject result = new JSONObject();
        IndexRef ref = new IndexRef();
        String id = readUntilByte(line, ref, x00);
        result.put("id", id);
        do {
            char type = line.charAt(ref.i++);
            String name = readUntilByte(line, ref, x00);
            String value;
            switch (type) {
                case x01:
                    readStringValue(result, name, line, ref);
                    break;
                case x02:
                    readBoolOrDate(result, name, line, ref);
                    break;
                case x00:
                    readList(result, name, line, ref);
                    break;
            }
        } while (ref.i < line.length());

        return result;
    }

    private void readList(JSONObject result, String name, String str, IndexRef ref) {
        String listString = readUntilByte(str, ref, x08);
        IndexRef index = new IndexRef();
        JSONArray arr = new JSONArray();
        while (index.i < listString.length()) {
            readUntilByte(listString, index, x01);
            String number = readUntilByte(listString, index, x00);
            String value = readUntilByte(listString, index, x00);
            arr.put(value);
        }
        result.put(name, arr);
    }

    private void readStringValue(JSONObject result, String name, String str, IndexRef ref) {
        String value = readUntilByte(str, ref, (char) 0);
        //In order to keep the bytes untouched, we converted every byte into char and since char is twice as big as byte in Java
        //it's now impossible to read multibyte UTF-8 values, they just treated as 2 separate chars.
        //in order to overcome this problem, we need to convert this part of the string back to original signed bytes and read it as UTF-8
        byte[] bytes = new byte[value.length()];
        for (int i = 0; i < value.length(); i++) {
            bytes[i] = (byte) value.charAt(i);
        }
        String val = new String(bytes, StandardCharsets.UTF_8);
        result.put(name, val);
    }

    private void readBoolOrDate(JSONObject result, String name, String str, IndexRef ref) {
        char a = str.charAt(ref.i++);
        char b = str.charAt(ref.i++);
        char c = str.charAt(ref.i++);
        char d = str.charAt(ref.i++);
        if (b == x00 && c == x00 && d == x00) {
            boolean res = a == 1;
            result.put(name, res);
            return;
        }

        //if more than 1 byte is used, then it's a binary date
        //and here comes the problem, you can't read binary as normal chars, since they will be converted to some UTF-8 multibyte crappy chars
        //so, we need to convert this part of a string back to original bytes
        //but it's not enough, because bytes in Java are signed, so we need to create unsigned chars that will reflect exactly what is written in binary.
        byte[] crappySignedBytes = new byte[]{(byte) a, (byte) b, (byte) c, (byte) d};
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
        date.put("type", "date");
        date.put("value", dateStr);
        result.put(name, date);
    }

    private String readUntilByte(String str, IndexRef ref, char search) {
        StringBuilder sb = new StringBuilder();
        if (ref.i >= str.length()) {
            return sb.toString();
        }

        char currentChar = str.charAt(ref.i++);
        while (ref.i < str.length() && currentChar != search) {
            sb.append(currentChar);
            currentChar = str.charAt(ref.i++);
        }

        //end of line exception
        if (ref.i >= str.length() && currentChar != search) {
            sb.append(currentChar);
        }

        return sb.toString();
    }
}
