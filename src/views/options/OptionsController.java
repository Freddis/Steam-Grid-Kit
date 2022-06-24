package views.options;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import kit.Config;
import kit.griddb.SteamGridDbClient;
import kit.utils.JsonHelper;
import kit.utils.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class OptionsController {
    public TextArea textAreaIgnoredFolderNames;
    public TextField textFieldLocalGamesPath;
    public TextField textFieldLocalShortcutsPath;
    public TextField textFieldSteamGridDbApiKey;
    public Button buttonSave;
    public Button buttonClear;
    public Label labelLocalGamesPath;
    public Label labelLocalShortcutsPath;

    private JSONObject settings;
    private JsonHelper jsonHelper;
    private Logger logger;
    private Runnable onSave;


    public void initializeSettings(Logger logger, JSONObject settings, Runnable onSave) {
        this.settings = settings;
        this.logger = logger;
        this.jsonHelper = new JsonHelper(logger);
        loadCommaSeparatedValues(settings, Config.Keys.IGNORED_FOLDERS_NAMES.getKey(), textAreaIgnoredFolderNames);
        textFieldLocalGamesPath.setText(settings.optString(Config.Keys.LOCAL_GAMES_DIRECTORY_PATH.getKey(), ""));
        textFieldLocalShortcutsPath.setText(settings.optString(Config.Keys.LOCAL_VDF_PATH.getKey(), ""));
        textFieldSteamGridDbApiKey.setText(settings.optString(Config.Keys.STEAM_GRID_DB_API_KEY.getKey(),""));
        if(!Config.isRemoteProcessingAllowed())
        {
            VBox parent = (VBox) textFieldLocalGamesPath.getParent();
            parent.getChildren().remove(textFieldLocalShortcutsPath);
            parent.getChildren().remove(labelLocalShortcutsPath);
            parent.getChildren().remove(textFieldLocalGamesPath);
            parent.getChildren().remove(labelLocalGamesPath);
        }
        this.onSave = onSave;
    }

    public void saveOptions(MouseEvent mouseEvent) {
        String key = textFieldSteamGridDbApiKey.getText();
        String existingKey = settings.optString(Config.Keys.STEAM_GRID_DB_API_KEY.getKey(),null);
        boolean shouldCheckKey = key != null && !key.trim().isEmpty() && !(existingKey != null && existingKey.equals(key));

        Consumer<Boolean> callback = status -> {
            buttonSave.setDisable(false);
            buttonClear.setDisable(false);
            if (!status) {
                Alert.AlertType type = Alert.AlertType.ERROR;
                Alert alert = new Alert(type);
                alert.setTitle("API status");
                alert.setHeaderText("");
                String message = "Something went wrong, please check the API key";
                alert.setContentText(message);
                alert.show();
                return;
            }
            if(key != null)
            {
                settings.put(Config.Keys.STEAM_GRID_DB_API_KEY.getKey(), key.trim());
            }

            addCommaSeparatedValues(settings, Config.Keys.IGNORED_FOLDERS_NAMES.getKey(), textAreaIgnoredFolderNames);

            if(Config.isRemoteProcessingAllowed()) {
                if (textFieldLocalGamesPath.getText() != null) {
                    String val = textFieldLocalGamesPath.getText().trim();
                    settings.put(Config.Keys.LOCAL_GAMES_DIRECTORY_PATH.getKey(), val.isEmpty() ? null : val);
                }
                if (textFieldLocalShortcutsPath.getText() != null) {
                    String val = textFieldLocalShortcutsPath.getText().trim();
                    settings.put(Config.Keys.LOCAL_VDF_PATH.getKey(), val.isEmpty() ? null : val);
                }
            }
            jsonHelper.writeJsonToFile(Config.getPropsJsonFilePath(), settings);
            Stage stage = (Stage) textAreaIgnoredFolderNames.getScene().getWindow();
            stage.close();
        };

        buttonSave.setDisable(true);
        buttonClear.setDisable(true);



        if (shouldCheckKey) {
            SteamGridDbClient client = new SteamGridDbClient(key.trim(), Config.getUserAgent());
            client.testApi(status -> {
                Platform.runLater(() -> {
                    callback.accept(status);
                });
            });
        } else {
            callback.accept(true);
        }
    }

    private void loadCommaSeparatedValues(JSONObject obj, String key, TextArea textArea) {
        if (obj.has(key)) {
            JSONArray arr = obj.getJSONArray(key);
            String[] strings = new String[arr.length()];
            for (int i = 0; i < arr.length(); i++) {
                strings[i] = arr.getString(i);
            }
            textArea.setText(String.join(",", strings));
        }
    }

    private void addCommaSeparatedValues(JSONObject obj, String key, TextArea textArea) {
        JSONArray arr = new JSONArray();
        String[] parts = textArea.getText().split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            arr.put(trimmed);
        }
        obj.put(key, arr);
    }

    public void clearGames(MouseEvent mouseEvent) {
        logger.log("Clearing games");
        settings.put(Config.Keys.GAMES.getKey(), new JSONArray());
        try {
            File dir = new File(Config.getImageDirectory());
            if(dir.exists()) {
                Files.walk(dir.toPath())
                        .map(Path::toFile)
                        .sorted((o1, o2) -> -o1.compareTo(o2))
                        .forEach(File::delete);
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.log("Couldn't clear images");
        }

        Stage stage = (Stage) textAreaIgnoredFolderNames.getScene().getWindow();
        stage.close();
        onSave.run();
    }

    public void testSteamGridDbApi(MouseEvent mouseEvent) {
        String key = textFieldSteamGridDbApiKey.getText();
        key = key != null ? key.trim() : null;

        SteamGridDbClient client = new SteamGridDbClient(key, Config.getUserAgent());
        client.testApi(status -> {
            Platform.runLater(() -> {
                Alert.AlertType type = status ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR;
                Alert alert = new Alert(type);
                alert.setTitle("API status");
                alert.setHeaderText("");
                String message = status ? "Successfully connected to SteamGridDb!" : "Something went wrong, please check the key";
                alert.setContentText(message);
                alert.show();
            });
        });
    }

    public void gotoSteamGridDbWebsite(MouseEvent mouseEvent) {
        String uri = "https://www.steamgriddb.com/profile/preferences";
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(uri));
            }
            catch (Exception e){

            }
        }
    }
}
