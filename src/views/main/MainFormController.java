package views.main;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import kit.Config;
import kit.interfaces.ITask;
import kit.models.Game;
import kit.tasks.impl.*;
import kit.utils.*;
import org.json.JSONArray;
import org.json.JSONObject;
import views.options.OptionsController;

import java.io.File;
import java.util.*;


public class MainFormController {
    @FXML
    public TableColumn<Game, String> tableColumnNumber;
    @FXML
    public TableColumn<Game, String> tableColumnDirectory;
    @FXML
    public TableColumn<Game, String> tableColumnGame;
    @FXML
    public TableColumn<Game, String> tableColumnSteamId;
    @FXML
    public TableColumn<Game, String> tableColumnExecs;
    @FXML
    public TableColumn<Game, VBox> tableColumnImageHeader;
    @FXML
    public TableColumn<Game, ImageView> tableColumnImageCover;
    @FXML
    public TableColumn<Game, ImageView> tableColumnImageBackground;
    @FXML
    public TableColumn<Game, ImageView> tableColumnImageLogo;
    @FXML
    private ChoiceBox<String> choiceBoxTask;
    @FXML
    private Node container;
    @FXML
    private TextArea textAreaLog;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label labelProgress;
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
    @FXML
    private TableView<Game> tableGames;
    @FXML
    public CheckBox checkboxUseCache;

    private Stage optionsWindow;
    private final ArrayList<Game> games = new ArrayList<>();
    private JSONObject settings;
    private Logger logger;
    private JsonHelper jsonHelper;
    private Progress progress;
    private ImageCache images = new ImageCache();

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
        for (Config.Task val : Config.Task.values()) {
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
        tableColumnNumber.setCellValueFactory(param -> {
            int number = tableGames.getItems().indexOf(param.getValue()) + 1;
            return new SimpleObservableValue<>(() -> String.valueOf(number));
        });

//        tableColumnDirectory.setCellValueFactory(param -> new SimpleObservableValue<>(() -> param.getValue().getDirectory() + "\n" + param.getValue().getSteamId() + "\n" + param.getValue().getName()));
//        tableColumnGame.setCellValueFactory(param -> new SimpleObservableValue<>(() -> param.getValue().getName()));
        tableColumnGame.setCellValueFactory(param -> new SimpleObservableValue<>(() ->
                param.getValue().getDirectory() + "\n"
                        + param.getValue().getSteamId() + "\n"
                        + param.getValue().getName() + "\n"
                        + param.getValue().getExecName()
        ));
//        tableColumnSteamId.setCellValueFactory(param -> new SimpleObservableValue<>(() -> param.getValue().getSteamId()));
        tableColumnExecs.setCellValueFactory(param -> new SimpleObservableValue<>(() -> {
            StringBuilder result = new StringBuilder();
            ArrayList<String> files = param.getValue().getExecs();
            for (String file : files) {
                result.append(file).append("\n");
            }
            return result.toString();
        }));
        tableColumnImageHeader.setCellValueFactory(item -> new SimpleObservableValue<>(() -> {
            Node[] nodes = new Node[]{
                    images.getImageView(item.getValue().getHeaderImageFile(), tableColumnImageHeader.widthProperty()),
                    images.getImageView(item.getValue().getBackgroundImageFile(), tableColumnImageHeader.widthProperty()),
                    images.getImageView(item.getValue().getLogoImageFile(), tableColumnImageHeader.widthProperty()),
            };
            Node[] filtered = Arrays.stream(nodes).filter(Objects::nonNull).toArray(Node[]::new);
            return new VBox(filtered);
        }));
        tableColumnImageCover.setCellValueFactory(item -> new SimpleObservableValue<>(() -> images.getImageView(item.getValue().getCoverImageFile(), tableColumnImageCover.widthProperty())));
//        tableColumnImageBackground.setCellValueFactory(item -> new SimpleObservableValue<>(() -> images.getImageView(item.getValue().getBackgroundImageFile(), tableColumnImageBackground.widthProperty())));
//        tableColumnImageLogo.setCellValueFactory(item -> new SimpleObservableValue<>(() -> images.getImageView(item.getValue().getLogoImageFile(), tableColumnImageLogo.widthProperty())));

        tableGames.setRowFactory(new Callback<TableView<Game>, TableRow<Game>>() {
            @Override
            public TableRow<Game> call(TableView param) {
                return new TableRow<Game>() {
                    @Override
                    protected void updateItem(Game item, boolean empty) {
                        super.updateItem(item, empty);
                        if (Objects.nonNull(item) && item.isReadyToExport()) {
                            this.getStyleClass().add("ready-for-export");
                        }
                    }
                };
            }
        });
    }

    public void showOptionsWindow() {
        logger.log("Showing options");
        if (optionsWindow == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/options/options.fxml"));
                Stage stage = loader.load();
                OptionsController ctrl = loader.getController();
                ctrl.initializeSettings(logger, settings);
                optionsWindow = stage;
            } catch (Exception e) {
                logger.log("Couldn't create new window");
            }
            optionsWindow.show();
            optionsWindow.setOnCloseRequest(event -> optionsWindow = null);
        }
        optionsWindow.requestFocus();
    }

    public void selectGamesDirectory() {
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

    public void selectShortcutFile() {
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

    public void runTasks(ITask[] finders, boolean updateTable) {
        runTasks(new LinkedList<>(Arrays.asList(finders)), updateTable);
    }

    public void runTasks(Queue<ITask> queue, boolean updateTables) {
        ITask task = queue.poll();
        if (task == null) {
            return;
        }
        this.progress.startTask(task.getStatusString() + "...");
        task.onFinish((status) -> {
            progress.endTask();
            Platform.runLater(() -> {
                initGames();
                saveConfigJson();
            });
            runTasks(queue, updateTables);
        });
        task.start(param -> {
            progress.setTaskProgress(param);
            if (updateTables) {
                Platform.runLater(() -> {
                    initGames();
                    saveConfigJson();
                });
            }
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
        settings.put(Config.Keys.USE_CACHE.getKey(), checkboxUseCache.isSelected());
        this.jsonHelper.writeJsonToFile(Config.getPropsJsonFilePath(), settings);
    }

    public void toggleUseCache() {
        boolean selected = checkboxUseCache.isSelected();
        checkboxUseCache.setSelected(selected);
        this.saveConfigJson();
    }

    public void start() {
        boolean update = true;
        List<ITask> tasks = new ArrayList<>();
        String selectedTaskLabel = choiceBoxTask.getValue();
        Config.Task selectedTask = Arrays.stream(Config.Task.values()).filter(task1 -> selectedTaskLabel.equals(task1.getTitle())).findFirst().orElse(Config.Task.ALL);
        switch (selectedTask) {
            case LOAD_STEAM_GAMES:
                tasks.add(new SteamGamesLoader(logger));
                update = false;
                break;
            case FIND_GAME_FOLDERS:
                tasks.add(new GameFolderFinder(logger, settings));
                break;
            case FIND_EXECUTABLES:
                tasks.add(new ExeFinder(logger, settings));
                break;
            case FIND_GAME_IDS:
                tasks.add(new SteamIdFinder(logger, settings));
                break;
            case LOAD_STEAM_IMAGES:
                tasks.add(new SteamImageLoader(logger, settings));
                break;
            case ALL:
                tasks.add(new GameFolderFinder(logger, settings));
                tasks.add(new ExeFinder(logger, settings));
                break;
        }
        this.runTasks(tasks.toArray(new ITask[0]), update);
    }
}
