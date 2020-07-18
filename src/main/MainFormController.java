package main;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import kit.Game;
import kit.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


public class MainFormController {
    private Stage stage;
    private TextArea logTextArea;
    private ProgressBar progressBar;
    private Label labelProgress;
    private JSONObject settings;
    private Logger logger;

    void init(Stage stage) {
        this.stage = stage;
        this.logTextArea = (TextArea) stage.getScene().lookup("#textareaLog");
        this.logger = new Logger(this.logTextArea);
        this.log("App started");
        progressBar = (ProgressBar) stage.getScene().lookup("#progressBar");
        labelProgress = (Label) stage.getScene().lookup("#labelProgress");
        settings = this.readJsonFromFile(this.getPropsJsonFilePath());
        this.initSettings(settings);
    }

    private JSONObject readJsonFromFile(String path) {
        this.log("Loading data from file " + path);
        String propsPath = this.getPropsJsonFilePath();
        File json = new File(propsPath);
        String content = null;
        try {
            content = new String(Files.readAllBytes(Paths.get(json.toURI())));
        } catch (IOException e) {
            this.log("Can't read file: " + path);
            e.printStackTrace();
        }
        JSONObject root = content != null ? new JSONObject(content) : new JSONObject();
        return root;

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
        TableColumn<Game, String> directoryCol = (TableColumn<Game, String>) tableGames.getColumns().get(0);
        directoryCol.setCellValueFactory(new PropertyValueFactory<>("directory"));

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

        String output = obj.toString();
        //works bad in IDE, working dir is the project root
//        String currentPath = new File(".").getAbsolutePath();
        File jarDir = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath());
        try {
            PrintWriter writer = new PrintWriter(jarDir + "/SteamGridKit.json", "UTF-8");
            writer.write(output);
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void loadSteamLibrary() {
        this.toggleControls(false);
        ProgressBar progressBar = (ProgressBar) stage.getScene().lookup("#progressBar");
        progressBar.setProgress(0.05);
        labelProgress.setText("Loading steam games...");

        Thread thread = new Thread(() -> {
            try {
                URL url = new URL("http://api.steampowered.com/ISteamApps/GetAppList/v0002/?format=json");
                //bigger file without ddos checks for debugging
//                URL url = new URL("https://raw.githubusercontent.com/lutangar/cities.json/master/cities.json");
                HttpURLConnection httpConnection = (HttpURLConnection) (url.openConnection());

                //No luck with Steam
//                    int completeFileSize = httpConnection.getContentLength();
                float averageFileSizeMb = (float) 5.3;
                int completeFileSize = (int) (averageFileSizeMb * 1024 * 1024);
                BufferedInputStream in = new BufferedInputStream(httpConnection.getInputStream());
                FileOutputStream fos = new FileOutputStream(getSteamLibraryJsonFilePath());
                int bufferSize = 1024 * 100;
                BufferedOutputStream bout = new BufferedOutputStream(fos, bufferSize);
                byte[] data = new byte[bufferSize];
                int downloadedFileSize = 0;
                int x = 0;
                double currentProgress = 0;
                while ((x = in.read(data, 0, bufferSize)) >= 0) {
                    downloadedFileSize += x;
                    // calculate progress
                    currentProgress = (double) downloadedFileSize / (double) completeFileSize;
                    double finalCurrentProgress = currentProgress;
                    Platform.runLater(() -> {
//                        log(finalCurrentProgress + "");
                        progressBar.setProgress(finalCurrentProgress);
                    });
                    bout.write(data, 0, x);
                }
                bout.close();
                in.close();
                Platform.runLater(() -> {
                    progressBar.setProgress(1.0);
                    toggleControls(true);
                    log("Response loaded");
                    this.labelProgress.setText("");
                    this.checkSteamLibraryJson();
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    private void checkSteamLibraryJson() {
        JSONObject json = this.getSteamLibraryJson();
        if(json.has("applist") && json.getJSONObject("applist").has("apps"))
        {
            JSONArray apps = json.getJSONObject("applist").getJSONArray("apps");
            log("Loaded " + apps.length() + " game ids");
        }
        else {
            log("Seems like json is malformed");
        }
    }

    private JSONObject getSteamLibraryJson() {
        String path = this.getSteamLibraryJsonFilePath();
        File json = new File(path);
        try {
            String content = new String(Files.readAllBytes(Paths.get(json.toURI())));
            JSONObject root = new JSONObject(content);
            return root;
        } catch (FileNotFoundException e) {
            this.log("File not found at: " + path);
        } catch (IOException e) {
            this.log("Couldn't read file: " + path);
        }
        return  new JSONObject();
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
