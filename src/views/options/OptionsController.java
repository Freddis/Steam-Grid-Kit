package views.options;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import kit.Config;
import kit.utils.JsonHelper;
import kit.utils.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class OptionsController {
    @FXML
    public TextArea textAreaIgnoredFolderNames;
    @FXML
    public TextField textFieldLocalGamesPath;
    @FXML
    public TextField textFieldLocalVdfPath;

    private JSONObject settings;
    private JsonHelper jsonHelper;
    private Logger logger;
    private Runnable onSave;


    public void initializeSettings(Logger logger, JSONObject settings,Runnable onSave) {
        this.settings = settings;
        this.logger = logger;
        this.jsonHelper = new JsonHelper(logger);
        loadCommaSeparatedValues(settings,Config.Keys.IGNORED_FOLDERS_NAMES.getKey(),textAreaIgnoredFolderNames);
        textFieldLocalGamesPath.setText(settings.optString(Config.Keys.LOCAL_GAMES_DIRECTORY_PATH.getKey(),""));
        textFieldLocalVdfPath.setText(settings.optString(Config.Keys.LOCAL_VDF_PATH.getKey(),""));
        this.onSave = onSave;
    }
    public void saveOptions(MouseEvent mouseEvent) {
        addCommaSeparatedValues(settings,Config.Keys.IGNORED_FOLDERS_NAMES.getKey(),textAreaIgnoredFolderNames);

        if(textFieldLocalGamesPath.getText() != null)
        {
            String val = textFieldLocalGamesPath.getText().trim();
            settings.put(Config.Keys.LOCAL_GAMES_DIRECTORY_PATH.getKey(),val.isEmpty() ? null : val);
        }
        if(textFieldLocalVdfPath.getText() != null)
        {
            String val = textFieldLocalVdfPath.getText().trim();
            settings.put(Config.Keys.LOCAL_VDF_PATH.getKey(),val.isEmpty() ? null : val);
        }
        jsonHelper.writeJsonToFile(Config.getPropsJsonFilePath(),settings);
        Stage stage = (Stage) textAreaIgnoredFolderNames.getScene().getWindow();
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

    public void clearGames(MouseEvent mouseEvent) {
        logger.log("Clearing games");
        settings.put(Config.Keys.GAMES.getKey(),new JSONArray());
        Stage stage = (Stage) textAreaIgnoredFolderNames.getScene().getWindow();
        stage.close();
        onSave.run();
    }
}
