package main;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import kit.Config;
import kit.finders.ExeFinder;
import kit.Game;
import kit.utils.FileLoader;
import kit.utils.JsonHelper;
import kit.utils.Logger;
import kit.utils.Progress;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.*;


public class MainFormController {
    @FXML
    private TextArea textAreaLog;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label labelProgress;
    @FXML
    private Button buttonLoadSteamLibrary;
    @FXML
    private Button buttonStart;
    @FXML
    private Button buttonSelectShortcutsFile;
    @FXML
    private Button buttonSelectGamesDirectory;
    @FXML
    private TextField textFieldShortcutsFile;
    @FXML
    private TextField textFieldGamesDirectory;

    private Stage stage;
    private JSONObject settings;
    private Logger logger;
    private JsonHelper jsonHelper;
    private Progress progress;

    void init(Stage stage) {
        this.stage = stage;
        this.logger = new Logger(this.textAreaLog);
        this.jsonHelper = new JsonHelper(this.logger);
        progress = new Progress(labelProgress, progressBar, new Button[]{buttonStart, buttonLoadSteamLibrary, buttonSelectGamesDirectory, buttonSelectShortcutsFile});
        logger.log("App started");
        settings = jsonHelper.readJsonFromFile(Config.getPropsJsonFilePath());
        this.initControls(settings);
    }

    private void initControls(JSONObject settings) {
        if (settings.has("vdfFile")) {
            String val = settings.getString("vdfFile");
            textFieldShortcutsFile.setText(val);
        }
        if (settings.has("gamesDirectory")) {
            String val = settings.getString("gamesDirectory");
            textFieldGamesDirectory.setText(val);
        }
    }


    public void selectGamesDirectory(MouseEvent event) {
        System.out.println("Setting games directory");
        DirectoryChooser fileChooser = new DirectoryChooser();
        fileChooser.setTitle("Open Resource File");
        File file = fileChooser.showDialog(stage);
        if (file == null) {
            logger.log("Cancelled");
            return;
        }
        TextField gamesDirectory = (TextField) stage.getScene().lookup("#textFieldGamesDirectory");
        gamesDirectory.setText(file.getAbsolutePath());
        logger.log("Dir: " + file.getAbsolutePath());
        this.saveConfigJson();
    }

    public void selectShortcutFile(MouseEvent event) {
        System.out.println("Setting vdf file");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        File file = fileChooser.showOpenDialog(stage);
        if (file == null) {
            logger.log("Cancelled");
            return;
        }
        TextField field = (TextField) stage.getScene().lookup("#textFieldShortcutsFile");
        field.setText(file.getAbsolutePath());
        logger.log("File: " + file.getAbsolutePath());
        this.saveConfigJson();
    }

    public void start(MouseEvent event) {
        System.out.println("Reading directories");
        TextField gamesDirectory = (TextField) stage.getScene().lookup("#textFieldGamesDirectory");
        File dir = new File(gamesDirectory.getText());
        if (!dir.isDirectory()) {
            logger.log("Path is not a directory");
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
        TableColumn<Game, String> execsCol = (TableColumn<Game, String>) tableGames.getColumns().get(4);
        execsCol.setCellValueFactory(param -> new ObservableValue<String>() {
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
                String result = "";
                ArrayList<String> files = param.getValue().getExecs();
                for (int i = 0; i < files.size(); i++) {
                    result += files.get(i) + "\n";
                }
                return result;
            }
        });

        File[] files = dir.listFiles();
        ArrayList<Game> list = new ArrayList<>();
        logger.log("Found " + files.length + " files");
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (!file.isDirectory()) {
                continue;
            }
            list.add(new Game(file.getName()));
        }
        ObservableList<Game> data = FXCollections.observableList(list);
        tableGames.setItems(data);

        ExeFinder finder = new ExeFinder(this.logger, list, dir);
        this.progress.startTask("Searching exe files");
        finder.onFinish((status) -> {
            progress.endTask();
            return null;
        });
        finder.start(progress -> {
            this.progress.setTaskProgress(progress);
            tableGames.refresh();
            return null;
        });


    }

    private void saveConfigJson() {
        logger.log("Saving JSON props");
        JSONObject obj = new JSONObject();
        TextField vdfFileField = (TextField) stage.getScene().lookup("#textFieldShortcutsFile");
        obj.put("vdfFile", vdfFileField.getText());
        TextField gamesDirectory = (TextField) stage.getScene().lookup("#textFieldGamesDirectory");
        obj.put("gamesDirectory", gamesDirectory.getText());
        this.jsonHelper.writeJsonToFile(Config.getPropsJsonFilePath(), obj);
    }

    public void loadSteamLibrary() {
        progress.startTask("Loading steam games");
        //bigger file without ddos checks for debugging
//        String url = "https://raw.githubusercontent.com/lutangar/cities.json/master/cities.json";
        String url = "http://api.steampowered.com/ISteamApps/GetAppList/v0002/?format=json";
        File file = new File(Config.getSteamLibraryJsonFilePath());
        FileLoader loader = new FileLoader(url, file);
        loader.onFinish((status) -> {
            progress.endTask();
            checkSteamLibraryJson();
            return null;
        });
        loader.start((currentProgress) -> {
            progress.setTaskProgress(currentProgress);
            return null;
        });
    }

    private void checkSteamLibraryJson() {
        JSONObject json = this.jsonHelper.readJsonFromFile(Config.getSteamLibraryJsonFilePath());
        if (json.has("applist") && json.getJSONObject("applist").has("apps")) {
            JSONArray apps = json.getJSONObject("applist").getJSONArray("apps");
            logger.log("Loaded " + apps.length() + " game ids");
        } else {
            logger.log("Seems like json is malformed");
        }
    }
}
