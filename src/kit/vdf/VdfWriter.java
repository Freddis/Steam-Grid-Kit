package kit.vdf;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class VdfWriter {

    char x00 = 0x00;
    char x01 = 0x01;
    char x08 = 0x08;
    ArrayList<JSONObject> lines = new ArrayList<>();

    public void addLine(String name, String exePath, String imagePath, JSONObject originalVdf) {
        String[] parts = exePath.split("\\\\");
        String exeDir = String.join("\\", Arrays.copyOf(parts, parts.length - 1)) + "\\";
        String quotedExePath = '"' + exePath + '"';

        JSONObject line = this.createVdfLine(originalVdf);
        line.put(VdfKey.APP_NAME.getKey(), name);
        line.put(VdfKey.EXE_PATH.getKey(), '"' + exePath + '"');
        line.put(VdfKey.START_DIR.getKey(), '"' + exeDir + '"');
        line.put(VdfKey.ICON.getKey(), imagePath);

        lines.add(line);
    }

    private JSONObject createVdfLine(JSONObject originalVdf) {
        JSONObject obj = new JSONObject();
        obj.put(VdfKey.SHORTCUT_PATH.getKey(), "");
        obj.put(VdfKey.LAUNCH_OPTIONS.getKey(), "");
        obj.put(VdfKey.IS_HIDDEN.getKey(), false);
        obj.put(VdfKey.ALLOW_DESKTOP_CONFIG.getKey(), true);
        obj.put(VdfKey.ALLOW_OVERLAY.getKey(), true);
        obj.put(VdfKey.OPEN_VR.getKey(), false);
        obj.put(VdfKey.DEVKIT.getKey(), false);
        obj.put(VdfKey.DEVKIT_GAME_ID.getKey(), "");
        obj.put(VdfKey.LAST_PLAY_TIME.getKey(), false);
        obj.put(VdfKey.TAGS.getKey(), new JSONArray());

        if (originalVdf == null) {
            return obj;
        }

        Iterator<String> keys = obj.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (originalVdf.has(key)) {
                Object val = originalVdf.get(key);
                obj.put(key, val);
            }
        }
        return obj;
    }

    public String getVdfContent() {
        String start = x00 + "shortcuts" + x00;
        String end = "" + x08 + x08;

        StringBuilder sb = new StringBuilder();
        sb.append(start);
        String[] strings = new String[lines.size()];
        int i = 0;
        for (JSONObject line : lines) {
            this.convertLineToString(sb, i++, line);
        }
        sb.append(end);
        return sb.toString();
    }

    private void convertLineToString(StringBuilder sb, int id, JSONObject line) {
        String lineDelimiter = "" + x08 + x08;
        String start = x00 + String.valueOf(id) + x00;
        sb.append(start);
        VdfKey[] orderedKeys = VdfKey.values();
        for (VdfKey key : orderedKeys) {
            Object val = line.get(key.getKey());
            String strVal;
            char type;
            if (val instanceof String) {
                type = 0x01;
                strVal = (String) val + x00;
            } else if (val instanceof Boolean) {
                type = 0x02;
                strVal = "" + ((boolean) val ? x01 : x00) + x00 + x00 + x00;
            } else if (val instanceof JSONObject && ((JSONObject) val).optString("type").equals("date")) {
                type = 0x02;
                strVal = ((JSONObject) val).getString("value");
            } else if (val instanceof JSONArray) {
                type = 0x00;
                strVal = convertListToString((JSONArray) val);
            } else {
                continue;
            }
            String keyVal = type + key.getKey() + x00 + strVal;
            sb.append(keyVal);
        }
        sb.append(lineDelimiter);
    }

    private String convertListToString(JSONArray arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length(); i++) {
            String val = arr.getString(i);
            String line = "" + x01 + i + x00 + val + x00;
            sb.append(line);
        }
        return sb.toString();
    }
}
