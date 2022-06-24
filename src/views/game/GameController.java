package views.game;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import kit.Config;
import kit.griddb.SteamGridDbClient;
import kit.models.Game;
import kit.models.SteamGame;
import kit.utils.JsonHelper;
import kit.utils.Logger;
import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

public class GameController {
    public ChoiceBox<String> choiceBoxExec;
    public ChoiceBox<String> choiceBoxGame;
    public TextField textFieldDirectoryName;
    public TextField textFieldAltName;
    public Hyperlink hyperlinkShowGame;
    public TabPane tabPaneIcons;
    public Button buttonShowExistingData;
    private Game game;
    private Logger logger;
    private JSONObject settings;
    private Runnable onSave;
    private SteamGridDbClient client = null;
    private final BooleanProperty useSteamId = new SimpleBooleanProperty(true);
    private final ObservableList<kit.griddb.Game> games = FXCollections.observableArrayList();


    public void save(MouseEvent mouseEvent) {

        JsonHelper helper = new JsonHelper(logger);
        ArrayList<Game> games = helper.toList(Game::new, settings.getJSONArray(Config.Keys.GAMES.getKey()));

        if (game.getExecs().size() > 0) {
            String newExe = game.getExecs().get(choiceBoxExec.getSelectionModel().getSelectedIndex());
            game.setSelectedExe(newExe);
        }

        if (game.getFoundSteamGames().size() > 0) {
            SteamGame newGame = game.getFoundSteamGames().get(choiceBoxGame.getSelectionModel().getSelectedIndex());
            game.setSelectedSteamGame(newGame);
        }

        if (textFieldAltName.getText() != null) {
            String val = textFieldAltName.getText().trim();
            game.setAltName(val.isEmpty() ? null : val);
        }


        settings.put(Config.Keys.GAMES.getKey(), helper.toJsonArray(games.toArray(new Game[0])));
        helper.writeJsonToFile(Config.getPropsJsonFilePath(), settings);
        Stage stage = (Stage) choiceBoxExec.getScene().getWindow();
        stage.close();
        onSave.run();
    }

    public void initialize(Logger logger, JSONObject settings, Game game, Runnable onSave) {
        this.logger = logger;
        this.game = game;
        this.settings = settings;
        this.onSave = onSave;

        buttonShowExistingData.setDisable(!game.hasVdf());
        textFieldDirectoryName.setText(game.getDirectory());
        textFieldAltName.setText(game.getAltName());
        choiceBoxExec.setItems(new ObservableListWrapper<>(game.getExecs()));
        choiceBoxExec.getSelectionModel().select(game.getSelectedExeIndex());

        ArrayList<SteamGame> steamGames = game.getFoundSteamGames();
        ArrayList<String> steamGamesNames = new ArrayList<>();
        steamGames.forEach(el -> steamGamesNames.add(el.getAppId() + ": " + el.getName()));
        choiceBoxGame.setItems(new ObservableListWrapper<>(steamGamesNames));
        choiceBoxGame.getSelectionModel().select(game.getSelectedSteamGameIndex());
        choiceBoxGame.getSelectionModel().selectedIndexProperty().addListener((a) -> hyperlinkShowGame.setVisited(false));
        if (choiceBoxGame.getItems().size() > 0) {
            hyperlinkShowGame.setOnMouseClicked((e) -> this.openSteamPage());
        } else {
            hyperlinkShowGame.setDisable(true);
        }

        String apiKey = settings.optString(Config.Keys.STEAM_GRID_DB_API_KEY.getKey(),null);
        if(apiKey != null && !apiKey.trim().isEmpty())
        {
            client = new SteamGridDbClient(apiKey.trim(), Config.getUserAgent());
        }

        tabPaneIcons.getTabs().clear();
        String[] images = {"Big Picture Cover", "Cover", "Hero Image", "Logo"};
        for (int i = 0; i < images.length; i++) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/game/icon.fxml"));
            Node tabContent;
            try {
                tabContent = loader.load();
                IconController ctrl = loader.getController();
                Tab tab = new Tab();
                tab.setContent(tabContent);
                ctrl.initialize(game,client, tab, images[i], i, useSteamId, games, this::findGames);
                tabPaneIcons.getTabs().add(tab);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void openSteamPage() {
        int selected = choiceBoxGame.getSelectionModel().getSelectedIndex();
        SteamGame steamGame = game.getFoundSteamGames().get(selected);
        String uri = steamGame.getSteamPageUrl();
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(uri));
            }
            catch (Exception e){

            }
        }
    }

    public void findGames(Runnable runnable) {
        games.clear();
        String name = null;
        SteamGame steamgame = game.getSelectedSteamGame();
        String alt = textFieldAltName.getText();
        if (!this.useSteamId.getValue()) {
            if (alt == null || alt.trim().isEmpty() || alt.trim().length() <= 3) {
                runnable.run();
                return;
            }
            String search = alt.trim();
            client.findGames(search, foundGames -> {
                games.clear();
                games.addAll(foundGames);
                runnable.run();
            });
            return;
        }

        client.findGameBySteamId(game.getSelectedSteamGame().getAppId(), foundGame -> {
            games.clear();
            if (foundGame != null) {
                games.add(foundGame);
            }
            runnable.run();
        });

    }

    public void showExistingSteamData(MouseEvent mouseEvent) {
        logger.log("Showing game info");
        Stage stage;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/game/data.fxml"));
            stage = loader.load();
            GameDataController ctrl = loader.getController();
            ctrl.initialize(game);
        } catch (Exception e) {
            logger.log("Couldn't create new window");
            return;
        }
        stage.setTitle("Edit" + game.getDirectory());
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(buttonShowExistingData.getScene().getWindow());
        stage.show();
    }
}
