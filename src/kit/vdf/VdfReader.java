package kit.vdf;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

public class VdfReader {

    public JSONArray parse(File file) {
        String content;
        try {
            content = new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            return null;
        }
        String x00 = new String(new char[]{0});
        String x08 = new String(new char[]{8});

        String start = x00 + "shortcuts" + x00 + x00;
        String end = x08 + x08 + x08 + x08;
        String lineDelimiter = x08 + x08 + x00;
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
        final char x00 = (char) 0; //separator
        final char x01 = (char) 1; //string
        final char x02 = (char) 2; //bool

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
        String listString = readUntilByte(str, ref, (char) 8);
        IndexRef index = new IndexRef();
        ArrayList<String> list = new ArrayList<>();
        while (index.i < listString.length()) {
            readUntilByte(listString, index, (char) 1);
            String number = readUntilByte(listString, index, (char) 0);
            String value = readUntilByte(listString, index, (char) 0);
            list.add(value);
        }
        JSONArray arr = new JSONArray();
        arr.put(list);
        result.put(name, arr);
    }

    private void readStringValue(JSONObject result, String name, String str, IndexRef ref) {
        String value = readUntilByte(str, ref, (char) 0);
        result.put(name, value);
    }

    private void readBoolOrDate(JSONObject result, String name, String str, IndexRef ref) {
        char a = str.charAt(ref.i++);
        char b = str.charAt(ref.i++);
        char c = str.charAt(ref.i++);
        char d = str.charAt(ref.i++);
        if (b == 0 && c == 0 && d == 0) {
            boolean res = a == 1;
            result.put(name, res);
            return;
        }
        //if more than 1 byte is used, then it's
        String date = new String(new char[]{a, b, c, d});
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
