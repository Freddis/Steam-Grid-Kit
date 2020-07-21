package views.game;

import com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory;
import com.sun.javafx.application.HostServicesDelegate;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import kit.Config;
import kit.models.Game;
import kit.models.SteamGame;
import kit.utils.JsonHelper;
import kit.utils.Logger;
import org.json.JSONObject;

import java.util.ArrayList;

public class GameController {

    public ChoiceBox<String> choiceBoxExec;
    public ChoiceBox<String> choiceBoxGame;
    public TextField textFieldDirectoryName;
    public TextField textFieldAltName;
    public Hyperlink hyperlinkShowGame;
    private Game game;
    private Logger logger;
    private JSONObject settings;
    private Runnable onSave;

    public void save(MouseEvent mouseEvent) {

        JsonHelper helper = new JsonHelper(logger);
        ArrayList<Game> games = helper.toList(Game::new, settings.getJSONArray(Config.Keys.GAMES.getKey()));

        String newExe = game.getExecs().get(choiceBoxExec.getSelectionModel().getSelectedIndex());
        game.setSelectedExe(newExe);

        SteamGame newGame = game.getFoundSteamGames().get(choiceBoxGame.getSelectionModel().getSelectedIndex());
        game.setSelectedSteamGame(newGame);

        if(textFieldAltName.getText() != null && !textFieldAltName.getText().trim().isEmpty())
        {
            game.setAltName(textFieldAltName.getText().trim());
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

        textFieldDirectoryName.setText(game.getDirectory());
        textFieldAltName.setText(game.getAltName());
        choiceBoxExec.setItems(new ObservableListWrapper<>(game.getExecs()));
        choiceBoxExec.getSelectionModel().select(game.getSelectedExeIndex());

        ArrayList<SteamGame> steamGames = game.getFoundSteamGames();
        ArrayList<String> steamGamesNames = new ArrayList<>();
        steamGames.forEach(el -> steamGamesNames.add(el.getAppId() + ": " + el.getName()));
        choiceBoxGame.setItems(new ObservableListWrapper<>(steamGamesNames));
        choiceBoxGame.getSelectionModel().select(game.getSelectedSteamGameIndex());
        choiceBoxGame.getSelectionModel().selectedIndexProperty().addListener((a) -> {
            hyperlinkShowGame.setVisited(false);
        });
        if (choiceBoxGame.getItems().size() > 0) {
            hyperlinkShowGame.setOnMouseClicked((e) -> this.openSteamPage());
        } else {
            hyperlinkShowGame.setDisable(true);
        }
    }

    private void openSteamPage() {
        int selected = choiceBoxGame.getSelectionModel().getSelectedIndex();
        SteamGame steamGame = game.getFoundSteamGames().get(selected);
        String uri = steamGame.getSteamPageUrl();
        HostServicesDelegate hostServices = HostServicesFactory.getInstance(kit.Main.getCurrent());
        hostServices.showDocument(uri);
    }
}
