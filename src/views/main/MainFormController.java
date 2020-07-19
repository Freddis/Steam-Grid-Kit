package views.main;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import kit.Config;
import kit.Game;
import kit.tasks.impl.ExeFinder;
import kit.tasks.impl.GameFolderFinder;
import kit.interfaces.ITask;
import kit.tasks.impl.SteamGamesLoader;
import kit.utils.*;
import org.json.JSONArray;
import org.json.JSONObject;
import views.options.OptionsController;

import java.io.File;
import java.util.*;


public class MainFormController {
    @FXML
    public ChoiceBox choiceBoxTask;
    @FXML
    Node container;
    @FXML
    private TextArea textAreaLog;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label labelProgress;
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
    public CheckBox checkboxUseCache;

    private Stage optionsWindow;
    private ArrayList<Game> games = new ArrayList<>();
    private JSONObject settings;
    private Logger logger;
    private JsonHelper jsonHelper;
    private Progress progress;

    @FXML
    public void initialize() {
        this.logger = new Logger(this.textAreaLog);
        this.jsonHelper = new JsonHelper(this.logger);
        progress = new Progress(labelProgress, progressBar, new Node[]{
                buttonStart,
                buttonSelectGamesDirectory,
                buttonSelectShortcutsFile,
                checkboxUseCache,
        });
        logger.log("App started");
        settings = jsonHelper.readJsonFromFile(Config.getPropsJsonFilePath());
        this.initControls(settings);
    }

    private void initControls(JSONObject settings) {
        if (settings.has(Config.Keys.VDF_FILE.getKey())) {
            String val = settings.getString(Config.Keys.VDF_FILE.getKey());
            textFieldShortcutsFile.setText(val);
        }
        if (settings.has(Config.Keys.GAMES_DIRECTORY_PATH.getKey())) {
            String val = settings.getString(Config.Keys.GAMES_DIRECTORY_PATH.getKey());
            textFieldGamesDirectory.setText(val);
        }
        if (settings.has(Config.Keys.USE_CACHE.getKey())) {
            boolean val = settings.getBoolean(Config.Keys.USE_CACHE.getKey());
            checkboxUseCache.setSelected(val);
        }

        List<String> tasks = new ArrayList<>();
        for(Config.Task val : Config.Task.values()) {
            tasks.add(val.getTitle());
        }
        choiceBoxTask.setItems(FXCollections.observableList(tasks));
        choiceBoxTask.getSelectionModel().select(0);

        this.initTable();
        this.initGames();
    }

    private void initGames() {
        games.clear();
        games.addAll(jsonHelper.toList(Game::new, settings.optJSONArray(Config.Keys.GAMES.getKey())));
        ObservableList<Game> data = FXCollections.observableList(games);
        tableGames.setItems(data);
        tableGames.refresh();
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


    public void runTasks(ITask[] finders)
    {
        runTasks(finders,true);
    }

    public void runTasks(ITask[] finders,boolean updateTable)
    {
        runTasks(new LinkedList<>(Arrays.asList(finders)),updateTable);
    }
    public void runTasks(Queue<ITask> queue, boolean updateTables)
    {
        ITask task = queue.poll();
        if(task == null)
        {
            return;
        }

        this.progress.startTask(task.getStatusString() + "...");
        task.onFinish((status) -> {
            progress.endTask();
            Platform.runLater(() -> {
                initGames();
                saveConfigJson();
            });
            runTasks(queue,updateTables);
            return null;
        });
        task.start(param -> {
            progress.setTaskProgress(param);
            if(updateTables) {
                Platform.runLater(() -> {
                    initGames();
                    saveConfigJson();
                });
            }
            return null;
        });
    }

    private void saveConfigJson() {
        logger.log("Saving JSON props");
        JSONArray arr = new JSONArray();
        for (Game game : games) {
            arr.put(game.toJson());
        }
        settings.put(Config.Keys.GAMES.getKey(), arr);
        settings.put(Config.Keys.VDF_FILE.getKey(), textFieldShortcutsFile.getText());
        settings.put(Config.Keys.GAMES_DIRECTORY_PATH.getKey(), textFieldGamesDirectory.getText());
        settings.put(Config.Keys.USE_CACHE.getKey(),checkboxUseCache.isSelected());
        this.jsonHelper.writeJsonToFile(Config.getPropsJsonFilePath(), settings);
    }

    public void toggleUseCache(MouseEvent mouseEvent) {
        boolean selected = checkboxUseCache.isSelected();
        checkboxUseCache.setSelected(selected);
        this.saveConfigJson();
    }

    public void start(MouseEvent event) {
        boolean update = true;
        List<ITask> tasks = new ArrayList<>();
        String selectedTaskLabel = choiceBoxTask.getValue().toString();
        Config.Task selectedTask = Arrays.stream(Config.Task.values()).filter(task1 -> selectedTaskLabel.equals(task1.getTitle())).findFirst().get();
        switch (selectedTask)
        {
            case LOAD_STEAM_GAMES :
                tasks.add(new SteamGamesLoader(logger, settings));
                update = false;
                break;
            case FIND_GAME_FOLDERS:
                tasks.add(new GameFolderFinder(logger, settings));
                break;
            case FIND_EXECUTABLES:
                tasks.add(new ExeFinder(logger, settings));
                break;
            default:
                tasks.add(new GameFolderFinder(logger,settings));
                tasks.add(new ExeFinder(logger,settings));
        }
        this.runTasks(tasks.toArray(new ITask[0]),update);

    }
}
