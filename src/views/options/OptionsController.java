package views.options;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import kit.Config;
import kit.utils.JsonHelper;
import kit.utils.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class OptionsController {
    @FXML
    public TextArea textAreaIgnoredExecutableNames;
    @FXML
    public TextArea textAreaIgnoredFolderNames;

    private JSONObject settings;
    private JsonHelper jsonHelper;
    private Logger logger;


    public void initializeSettings(Logger logger, JSONObject settings) {
        this.settings = settings;
        this.logger = logger;
        this.jsonHelper = new JsonHelper(logger);
        loadCommaSeparatedValues(settings,"ignoredExecNames",textAreaIgnoredExecutableNames);
    }
    public void saveOptions(MouseEvent mouseEvent) {
        addCommaSeparatedValues(settings,"ignoredExecNames",textAreaIgnoredExecutableNames);
        jsonHelper.writeJsonToFile(Config.getPropsJsonFilePath(),settings);
        Stage stage = (Stage) textAreaIgnoredExecutableNames.getScene().getWindow();
        stage.close();
    }

    private void loadCommaSeparatedValues(JSONObject obj, String key, TextArea textArea) {
        if(obj.has(key))
        {
            JSONArray arr = obj.getJSONArray(key);
            String[] strings = new String[arr.length()];
            for(int i =0; i < arr.length(); i++){
                strings[i] = arr.getString(i);
            }
            textArea.setText(String.join(",",strings));
        }
    }
    private void addCommaSeparatedValues(JSONObject obj, String key, TextArea textArea) {
        JSONArray arr = new JSONArray();
        String[] parts = textArea.getText().split(",");
        for(String part : parts){
            String trimmed = part.trim();
            if(trimmed.isEmpty())
            {
                continue;
            }
            arr.put(trimmed);
        }
        obj.put(key,arr);
    }
}
