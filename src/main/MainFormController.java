package main;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import kit.ExeFinder;
import kit.Game;
import kit.utils.FileLoader;
import kit.utils.JsonHelper;
import kit.utils.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.*;


public class MainFormController {
    private Stage stage;
    private TextArea logTextArea;
    private ProgressBar progressBar;
    private Label labelProgress;
    private JSONObject settings;
    private Logger logger;
    private JsonHelper jsonHelper;

    void init(Stage stage) {
        this.stage = stage;
        this.logTextArea = (TextArea) stage.getScene().lookup("#textareaLog");
        this.logger = new Logger(this.logTextArea);
        this.jsonHelper = new JsonHelper(this.logger);
        this.log("App started");
        progressBar = (ProgressBar) stage.getScene().lookup("#progressBar");
        labelProgress = (Label) stage.getScene().lookup("#labelProgress");
        settings = jsonHelper.readJsonFromFile(this.getPropsJsonFilePath());
        this.initSettings(settings);
    }

    private void initSettings(JSONObject settings) {
        if (settings.has("vdfFile")) {
            TextField field = (TextField) stage.getScene().lookup("#textFieldShortcutsFile");
            String val = settings.getString("vdfFile");
            field.setText(val);
        }
        if (settings.has("gamesDirectory")) {
            TextField field = (TextField) stage.getScene().lookup("#textFieldGamesDirectory");
            String val = settings.getString("gamesDirectory");
            field.setText(val);
        }
    }

    private String getPropsJsonFilePath() {
        return getJarPath() + "/SteamGridKit.json";
    }

    private String getSteamLibraryJsonFilePath() {
        return getJarPath() + "/steam.json";
    }

    private String getJarPath() {
        File jarDir = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath());
        try {
            String path = jarDir.getCanonicalPath();
            return path;
        } catch (IOException e) {
            return "";
        }
    }

    void log(String text) {
        this.logger.log(text);
    }

    public void selectGamesDirectory(MouseEvent event) {
        System.out.println("Setting games directory");
        DirectoryChooser fileChooser = new DirectoryChooser();
        fileChooser.setTitle("Open Resource File");
        File file = fileChooser.showDialog(stage);
        if (file == null) {
            this.log("Cancelled");
            return;
        }
        TextField gamesDirectory = (TextField) stage.getScene().lookup("#textFieldGamesDirectory");
        gamesDirectory.setText(file.getAbsolutePath());
        this.log("Dir: " + file.getAbsolutePath());
        this.saveJson();
    }

    public void selectShortcutFile(MouseEvent event) {
        System.out.println("Setting vdf file");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        File file = fileChooser.showOpenDialog(stage);
        if (file == null) {
            this.log("Cancelled");
            return;
        }
        TextField field = (TextField) stage.getScene().lookup("#textFieldShortcutsFile");
        field.setText(file.getAbsolutePath());
        this.log("File: " + file.getAbsolutePath());
        this.saveJson();
    }

    public void start(MouseEvent event) {
        System.out.println("Reading directories");
        TextField gamesDirectory = (TextField) stage.getScene().lookup("#textFieldGamesDirectory");
        File dir = new File(gamesDirectory.getText());
        if (!dir.isDirectory()) {
            this.log("Path is not a directory");
        }

        TableView tableGames = (TableView) stage.getScene().lookup("#tableGames");
        TableColumn<Game, String> directoryCol = (TableColumn<Game, String>) tableGames.getColumns().get(1);
        directoryCol.setCellValueFactory(new PropertyValueFactory<>("directory"));
        TableColumn<Game, String> numberCol = (TableColumn<Game, String>) tableGames.getColumns().get(0);
        numberCol.setCellValueFactory(param -> {
            int number = tableGames.getItems().indexOf(param.getValue()) + 1;
            return new ObservableValue<String>() {
                @Override
                public void addListener(InvalidationListener listener) {

                }

                @Override
                public void removeListener(InvalidationListener listener) {

                }

                @Override
                public void addListener(ChangeListener<? super String> listener) {

                }

                @Override
                public void removeListener(ChangeListener<? super String> listener) {

                }

                @Override
                public String getValue() {
                    return String.valueOf(number);
                }
            };

        });

        File[] files = dir.listFiles();
        ArrayList<Game> list = new ArrayList<>();
        this.log("Found " + files.length + " files");
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (!file.isDirectory()) {
                continue;
            }
            list.add(new Game(file.getName()));
        }

        ExeFinder finder = new ExeFinder(this.logger, list, dir);
        this.labelProgress.setText("Searching exe files");
        this.progressBar.setProgress(0);
        finder.start(progress -> {
            Platform.runLater(() -> {
                this.progressBar.setProgress(progress);
            });
            return null;
        });

        ObservableList<Game> data = FXCollections.observableList(list);
        tableGames.setItems(data);
    }

    private void saveJson() {
        this.log("Saving JSON props");
        JSONObject obj = new JSONObject();
        TextField vdfFileField = (TextField) stage.getScene().lookup("#textFieldShortcutsFile");
        obj.put("vdfFile", vdfFileField.getText());
        TextField gamesDirectory = (TextField) stage.getScene().lookup("#textFieldGamesDirectory");
        obj.put("gamesDirectory", gamesDirectory.getText());

       this.jsonHelper.writeJsonToFile(getPropsJsonFilePath(),obj);
    }

    public void loadSteamLibrary() {
        this.startTask("Loading steam games");
        //bigger file without ddos checks for debugging
//        String url = "https://raw.githubusercontent.com/lutangar/cities.json/master/cities.json";
        String url = "http://api.steampowered.com/ISteamApps/GetAppList/v0002/?format=json";
        File file = new File(getSteamLibraryJsonFilePath());
        FileLoader loader = new FileLoader(url, file);
        loader.onFinish((status) -> {
            endTask();
            checkSteamLibraryJson();
            return null;
        });
        loader.start((currentProgress) -> {
            setTaskProgress(currentProgress);
            return null;
        });
    }

    private void endTask() {
        Platform.runLater(() -> {
            this.toggleControls(true);
            progressBar.setProgress(0.0);
            labelProgress.setText("Done");
        });
    }

    private void startTask(String taskStatus) {
        Platform.runLater(() -> {
            this.toggleControls(false);
            progressBar.setProgress(0.01);
            labelProgress.setText(taskStatus);
        });
    }

    private void setTaskProgress(double progress) {
        Platform.runLater(() -> {
            progressBar.setProgress(progress);
        });
    }

    private void checkSteamLibraryJson() {
        JSONObject json = this.jsonHelper.readJsonFromFile(getSteamLibraryJsonFilePath());
        if (json.has("applist") && json.getJSONObject("applist").has("apps")) {
            JSONArray apps = json.getJSONObject("applist").getJSONArray("apps");
            log("Loaded " + apps.length() + " game ids");
        } else {
            log("Seems like json is malformed");
        }
    }

    private void toggleControls(boolean state) {
        String[] selectors = this.getControlSelectors();
        Platform.runLater(() -> {
            for (int i = 0; i < selectors.length; i++) {
                Node control = stage.getScene().lookup(selectors[i]);
                control.setDisable(!state);
            }
        });
    }

    private String[] getControlSelectors() {
        return new String[]{"#buttonLoadSteamLibrary"};
    }

}
