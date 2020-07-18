package views.main;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.Pair;
import kit.Config;
import kit.Game;
import kit.ProgressedTask;
import kit.finders.ExeFinder;
import kit.utils.FileLoader;
import kit.utils.JsonHelper;
import kit.utils.Logger;
import kit.utils.Progress;
import org.json.JSONArray;
import org.json.JSONObject;
import views.options.OptionsController;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;


public class MainFormController {
    @FXML
    Node container;
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
    private Button buttonShowOptions;
    @FXML
    private Button buttonSelectShortcutsFile;
    @FXML
    private Button buttonSelectGamesDirectory;
    @FXML
    private TextField textFieldShortcutsFile;
    @FXML
    private TextField textFieldGamesDirectory;
    @FXML
    private TableView tableGames;
    @FXML
    private CheckBox useCachedData;

    private Stage optionsWindow;
    private ArrayList<Game> games = new ArrayList<>();
    private JSONObject settings;
    private Logger logger;
    private JsonHelper jsonHelper;
    private Progress progress;

    @FXML
    public void initialize() {
//        primaryStage.setTitle("Steam Grid Kit");
//        this.stage = stage;
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
        if (settings.has("games")) {
            JSONArray data = settings.getJSONArray("games");
            for (int i = 0; i < data.length(); i++) {
                Game game = new Game(data.getJSONObject(i));
                games.add(game);
            }
        }
        this.initTable();
        ObservableList<Game> data = FXCollections.observableList(games);
        tableGames.setItems(data);
    }

    private void initTable() {
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
    }

    public void showOptionsWindow()
    {
        logger.log("Showing options");
        if(optionsWindow == null)
        {
            try
            {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/options/options.fxml"));
                Stage stage = loader.load();
                OptionsController ctrl = loader.getController();
                ctrl.initializeSettings(logger,settings);
                optionsWindow = stage;
            } catch (Exception e)
            {
                logger.log("Couldn't create new window");
            }
            optionsWindow.show();
            optionsWindow.setOnCloseRequest(event -> {
                optionsWindow = null;
            });
        }
        optionsWindow.requestFocus();
    }

    public void selectGamesDirectory(MouseEvent event) {
        System.out.println("Setting games directory");
        DirectoryChooser fileChooser = new DirectoryChooser();
        fileChooser.setTitle("Open Resource File");
        File file = fileChooser.showDialog(this.container.getScene().getWindow());
        if (file == null) {
            logger.log("Cancelled");
            return;
        }
        textFieldGamesDirectory.setText(file.getAbsolutePath());
        logger.log("Dir: " + file.getAbsolutePath());
        this.saveConfigJson();
    }

    public void selectShortcutFile(MouseEvent event) {
        System.out.println("Setting vdf file");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        File file = fileChooser.showOpenDialog(this.container.getScene().getWindow());
        if (file == null) {
            logger.log("Cancelled");
            return;
        }
        textFieldShortcutsFile.setText(file.getAbsolutePath());
        logger.log("File: " + file.getAbsolutePath());
        this.saveConfigJson();
    }

    public void start(MouseEvent event) {
        readGamesFromFolders();

        ExeFinder finder = new ExeFinder(logger, games, new File(settings.getString("gamesDirectory")));
        runFinders(new String[] {"Loading executables"},new ProgressedTask[]{finder});

    }

    public void runFinder( Queue<Pair<String,ProgressedTask>> queue)
    {
        Pair<String, ProgressedTask> pair = queue.poll();
        if(pair == null)
        {
            return;
        }

        this.progress.startTask(pair.getKey() + "...");
        ProgressedTask task = pair.getValue();
        task.onFinish((status) -> {
            progress.endTask();
            runFinder(queue);
            return null;
        });
        task.start(new Callback<Double, Void>() {
            @Override
            public Void call(Double param) {
                progress.setTaskProgress(param);
                Platform.runLater(() -> {
                    tableGames.refresh();
                });
                saveConfigJson();
                return null;
            }
        });
    }
    public void runFinders(String[] statuses, ProgressedTask[] tasks)
    {
        Queue<Pair<String,ProgressedTask>> queue = new LinkedList<>();
        for(int i = 0; i < tasks.length; i++)
        {
            queue.add(new Pair<>(statuses[i],tasks[i]));
        }
        this.runFinder(queue);
    }

    private void readGamesFromFolders() {
        System.out.println("Reading directories");
        File dir = new File(textFieldGamesDirectory.getText());
        if (!dir.isDirectory()) {
            logger.log("Path is not a directory");
        }

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
        this.games.clear();
        this.games.addAll(list);
        this.saveConfigJson();
    }

    private void saveConfigJson() {
        logger.log("Saving JSON props");
        JSONArray arr = new JSONArray();
        for (Game game : games) {
            arr.put(game.toJson());
        }
        settings.put("games", arr);

        settings.put("vdfFile", textFieldShortcutsFile.getText());
        settings.put("gamesDirectory", textFieldGamesDirectory.getText());
        this.jsonHelper.writeJsonToFile(Config.getPropsJsonFilePath(), settings);
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
