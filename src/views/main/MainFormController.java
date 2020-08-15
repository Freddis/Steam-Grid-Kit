package views.main;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import kit.Config;
import kit.interfaces.ITask;
import kit.models.Game;
import kit.tasks.GameTask;
import kit.tasks.impl.*;
import kit.utils.*;
import org.json.JSONArray;
import org.json.JSONObject;
import views.game.GameController;
import views.options.OptionsController;

import java.io.File;
import java.util.*;


public class MainFormController {
    @FXML
    public TableColumn<Game, String> tableColumnNumber;
    @FXML
    public TableColumn<Game, VBox> tableColumnGame;
    @FXML
    public TableColumn<Game, String> tableColumnExecs;
    @FXML
    public TableColumn<Game, VBox> tableColumnImageHeader;
    @FXML
    public TableColumn<Game, ImageView> tableColumnImageCover;
    @FXML
    public TableColumn<Game, VBox> tableColumnActions;
    @FXML
    public Button buttonShowOptions;
    @FXML
    public Button buttonTransfer;
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

    private final ArrayList<Game> games = new ArrayList<>();
    private JSONObject settings;
    private Logger logger;
    private JsonHelper jsonHelper;
    private Progress progress;
    private final ImageCache images = new ImageCache();
    private Queue<ITask> runningTasks;
    private ITask runningTask;
    private int runningTasksSize;
    private int runningTaskNumber;

    @FXML
    public void initialize() {
        logger = new Logger(this.textAreaLog);
        jsonHelper = new JsonHelper(logger);
        progress = new Progress(logger, labelProgress, progressBar, new Node[]{
                buttonSelectGamesDirectory,
                buttonSelectShortcutsFile,
                buttonShowOptions,
                checkboxUseCache,
                choiceBoxTask,
                buttonTransfer
        }, buttonStart, this::start, this::stop);
        logger.log("App started");
        settings = jsonHelper.readJsonFromFile(Config.getPropsJsonFilePath());
        this.initControls(settings);
    }

    /**
     * Initializing FXML controls, filling inputs, etc.
     * @param settings Settings JSON file
     */
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
        this.reCacheImages();
    }

    /**
     * Caching covers for games in the table. Otherwise it will be slow.
     */
    private void reCacheImages() {
        logger.log("Re-caching thumbnails");
        images.clear();
        for (Game game : games) {
            images.getImageView(game.getHeaderImageFile(), tableColumnImageHeader.widthProperty());
            images.getImageView(game.getBackgroundImageFile(), tableColumnImageHeader.widthProperty());
            images.getImageView(game.getLogoImageFile(), tableColumnImageHeader.widthProperty());
            images.getImageView(game.getCoverImageFile(), tableColumnImageCover.widthProperty());
        }
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

        String gamesDir = settings.optString(Config.Keys.GAMES_DIRECTORY_PATH.getKey(), null);
        String localPath = settings.optString(Config.Keys.LOCAL_GAMES_DIRECTORY_PATH.getKey(), null);
        String finalGamesDir = localPath != null ? localPath : gamesDir;
        tableColumnGame.setCellValueFactory(param -> new SimpleObservableValue<>(() -> {
            ArrayList<Node> nodes = new ArrayList<>();
            Game game = param.getValue();
            String start = game.getDirectory() + "\n";
            Node directory  = new Label(game.getDirectory());
            nodes.add(directory);

            if (game.getAltName() != null) {
                nodes.add(new Label("(" + game.getAltName() + ")"));
            }
            if (game.getSelectedSteamGame() != null) {
                nodes.add(new Label(String.valueOf(game.getSelectedSteamGame().getAppId())));
                nodes.add(new Label(game.getSelectedSteamGame().getName()));
            } else {
                Label id = new Label("(No Steam App ID)");
                id.getStyleClass().add("mark-problem");
                nodes.add(id);
            }
            if (game.hasVdf()) {
                Label label = new Label("Exisitng shortcut");
                label.getStyleClass().add("mark-notice");
                nodes.add(label);
            }
            if (!game.isLocatedIn(finalGamesDir)) {
                Label label = new Label("Not from the game directory");
                label.getStyleClass().add("mark-problem");
                nodes.add(label);
            }
            nodes.add(new Label(game.getExecName()));
            return new VBox(nodes.toArray(new Node[0]));
        }));
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
        tableColumnActions.setCellValueFactory(item -> new SimpleObservableValue<>(() -> {
            String[] names = {"Edit", "Update", "Wipe", "Ignore"};
            Button[] buttons = new Button[names.length];
            for (int i = 0; i < names.length; i++) {
                Button button = new Button(names[i]);
                button.setPrefWidth(100);
                button.disableProperty().bind(progress.getIsRunningProperty());
                buttons[i] = button;
            }
            buttons[0].onMouseClickedProperty().setValue(event -> showInfoWindow(item.getValue()));
            buttons[1].onMouseClickedProperty().setValue(event -> updateGame(item.getValue()));
            buttons[2].onMouseClickedProperty().setValue(event -> wipeGame(item.getValue()));
            buttons[3].onMouseClickedProperty().setValue(event -> ignoreGame(item.getValue()));
            VBox box = new VBox(buttons);
            box.setSpacing(10);
            return box;
        }));

        tableGames.setRowFactory(new Callback<TableView<Game>, TableRow<Game>>() {
            @Override
            public TableRow<Game> call(TableView param) {
                return new TableRow<Game>() {
                    @Override
                    protected void updateItem(Game item, boolean empty) {
                        super.updateItem(item, empty);
                        if (Objects.nonNull(item) && item.isReadyToExport()) {
                            this.getStyleClass().add("ready-for-export");
                        } else {
                            this.getStyleClass().remove("ready-for-export");
                        }
                    }
                };
            }
        });
    }

    private void ignoreGame(Game game) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Ignoring game");
        alert.setHeaderText("");
        alert.setContentText("Are you sure you want to delete this game from the list and add its folder to ignored list?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            logger.log("Deleting game: " + game.getDirectory());
            games.remove(game);
            JSONArray ignoredGames = settings.optJSONArray(Config.Keys.IGNORED_FOLDERS_NAMES.getKey());
            ignoredGames = ignoredGames != null ? ignoredGames : new JSONArray();
            ignoredGames.put(game.getDirectory());

            saveConfigJson();
            initTable();
        }
    }

    private void wipeGame(Game game) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Wiping game");
        alert.setHeaderText("");
        alert.setContentText("Are you sure you want to clear images and steam ids for the game?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            logger.log("Wiping game: " + game.getDirectory());
            ArrayList<File> imageFiles = new ArrayList<>();
            game.wipe();
            File imageFolder = new File(Config.getImageDirectory());
            File gameImageFolder = new File(imageFolder, game.getImageDirectoryName());
            boolean deleteResult = true;
            try {
                if (gameImageFolder.exists() && gameImageFolder.canRead()) {
                    imageFiles.addAll(Arrays.asList(gameImageFolder.listFiles()));
                }
                for (File file : imageFiles) {
                    if (file != null) {
                        logger.log("Deleting " + file.getAbsolutePath());
                        deleteResult &= file.delete();
                    }
                }
                deleteResult &= gameImageFolder.delete();
            } catch (Exception e) {
                deleteResult = false;
                logger.log("Something went wrong: " + e.getMessage());
            }
            logger.log("Delete result: " + (deleteResult ? "true" : "false"));
            saveConfigJson();
            initTable();
            reCacheImages();
        }
    }

    private void updateGame(Game game) {
        GameTask[] tasks = new GameTask[]{
                new ExeFinder(logger, settings),
                new SteamIdFinder(logger, settings),
                new SteamImageLoader(logger, settings),
        };
        Arrays.stream(tasks).forEach(el -> el.setGame(game));
        Arrays.stream(tasks).forEach(el -> el.setUseCache(false));
        runTasks(tasks, true);
    }

    public void showOptionsWindow() {
        logger.log("Showing options");
        Stage stage;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/options/options.fxml"));
            stage = loader.load();
            OptionsController ctrl = loader.getController();
            ctrl.initializeSettings(logger, settings, this::initGames);
        } catch (Exception e) {
            logger.log("Couldn't create new window");
            return;
        }
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(container.getScene().getWindow());
        stage.show();
    }

    private void showInfoWindow(Game game) {
        logger.log("Showing game info");
        Stage stage;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/game/game.fxml"));
            stage = loader.load();
            GameController ctrl = loader.getController();
            ctrl.initialize(logger, settings, game, () -> {
                saveConfigJson();
                initGames();
                initTable();
            });
        } catch (Exception e) {
            logger.log("Couldn't create new window");
            return;
        }
        stage.setTitle("Edit" + game.getDirectory());
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(container.getScene().getWindow());
        stage.show();
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

        if (runningTasks == null) {
            runningTasks = queue;
            runningTasksSize = queue.size();
            runningTaskNumber = 1;
        }
        if (runningTasks != queue) {
            logger.log("Can't run a task if another task is running");
            return;
        }

        ITask task = queue.poll();
        if (task == null) {
            runningTasks = null;
            logger.log("Tasks are done");
            return;
        }

        String cmdStatus = runningTaskNumber + "/" + runningTasksSize + ": " + task.getStatusString() + "...";
        runningTask = task;
        progress.startTask(cmdStatus);
        runningTaskNumber++;

        task.onFinish((status) -> {
            String finalStatus = progress.getStatus().replace("...", status ? ": Done" : ": Failed");
            progress.endTask(finalStatus);
            Platform.runLater(() -> {
                initGames();
                saveConfigJson();
                reCacheImages();
            });
            if (status) {
                runTasks(queue, updateTables);
            } else {
                runningTasks = null;
            }
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

    private void stopTasks() {
        if (runningTasks == null) {
            logger.log("No tasks to stop");
        }
        runningTask.kill();
        progress.endTask(progress.getStatus().replace("...", ": Stopped"));
        this.runningTasks = null;
        this.runningTask = null;
        initGames();
        saveConfigJson();
        reCacheImages();
    }

    private void saveConfigJson() {
        logger.log("Saving JSON props");
        JSONArray arr = jsonHelper.toJsonArray(games.toArray(new Game[0]));
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
            case FIND_EXISTING_SHORTCUTS:
                tasks.add(new ShortcutParser(logger, settings));
                break;
            case FIND_EXISTING_IMAGES:
                tasks.add(new ExistingImagesFinder(logger, settings));
                break;
            case LOAD_STEAM_GAMES:
                tasks.add(new SteamGamesLoader(logger, settings));
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
                tasks.add(new ShortcutParser(logger, settings));
                tasks.add(new ExistingImagesFinder(logger, settings));
                tasks.add(new GameFolderFinder(logger, settings));
                tasks.add(new ExeFinder(logger, settings));
                tasks.add(new SteamGamesLoader(logger, settings));
                tasks.add(new SteamIdFinder(logger, settings));
                tasks.add(new SteamImageLoader(logger, settings));
                break;
        }
        this.runTasks(tasks.toArray(new ITask[0]), update);
    }

    private void stop() {
        this.stopTasks();
    }

    public void transfer(MouseEvent mouseEvent) {
        ITask[] tasks = {
                new CreateVdfFile(logger, settings),
                new TransferImages(logger, settings),
        };
        runTasks(tasks, true);
    }
}
