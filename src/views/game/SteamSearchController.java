package views.game;

import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import kit.Config;
import kit.State;
import kit.models.Game;
import kit.models.SteamGame;
import kit.tasks.impl.SteamIdFinder;
import kit.utils.Logger;
import kit.utils.UrlOpener;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import tests.utils.TestLogger;
import tests.utils.TestUtils;

import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SteamSearchController {

    public TextField searchField;
    public Button searchButton;
    public VBox searchResults;
    protected Logger logger;
    protected Game game;
    protected JSONObject settings;

    public void initialize(JSONObject settings, Game game, Logger logger)
    {
        this.logger = logger;
        this.game = game;
        this.settings = settings;
    }
    public void search(MouseEvent event) throws InterruptedException {
        logger.log("Searching steam games");
        searchField.setDisable(true);
        searchButton.setDisable(true);

        State state = new State(settings,logger);
        Game artificial = new Game("game1",state.getPrimaryGamesDirectoryPath());
        artificial.setAltName(searchField.getText());
        long steamId;
        try {
            steamId = Long.parseLong(searchField.getText());
        }
        catch (NumberFormatException e) {
            steamId = 0;
        }
        artificial.setAltSteamId(steamId);
        state.setGames(Collections.singletonList(artificial));
        SteamIdFinder task = new SteamIdFinder(logger,state.getJson());
        CountDownLatch latch = new CountDownLatch(1);
        task.start(state,result -> {
            latch.countDown();
        });
        latch.await();
        logger.log("Done with search");
        Game processedGame = state.getGames().get(0);
        searchField.setDisable(false);
        searchButton.setDisable(false);

        searchResults.getChildren().clear();
        for(int i =0; i < processedGame.getFoundSteamGames().size(); i++) {
            SteamGame row = processedGame.getFoundSteamGames().get(i);
            HBox hbox = new HBox();
            Label label = new Label();
            label.setText(row.getName() +':'+ row.getAppId());
            label.cursorProperty().set(Cursor.HAND);
            String url = "https://store.steampowered.com/app/"+row.getAppId();
            label.setOnMouseClicked(e -> {
                UrlOpener opener = new UrlOpener(this.logger);
                opener.open(url);
            });
            Button button = new Button();
            button.setText("Set");
            HBox.setMargin(button, new Insets(5, 0, 0, 0));
            button.setOnMouseClicked(e2 -> {
                this.setSteamGame(row);
            });
            hbox.getChildren().add(label);
            hbox.getChildren().add(button);
            hbox.setSpacing(20);
            searchResults.getChildren().add(hbox);
            // 774361
        }
    }

    private void setSteamGame(SteamGame row) {
        this.game.getFoundSteamGames().add(row);
        this.game.setSelectedSteamGame(row);
        this.game.setAltName(row.getName());
        this.close(null);
    }


    public void close(MouseEvent mouseEvent) {
        Stage stage = (Stage)  searchField.getScene().getWindow();
        stage.close();
    }
}
