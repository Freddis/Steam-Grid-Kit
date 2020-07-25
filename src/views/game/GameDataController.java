package views.game;

import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import kit.models.Game;
import org.json.JSONObject;

public class GameDataController {

    public TextFlow textFlowData;

    public void initialize(Game game)
    {
        JSONObject json = game.getVdf();
        String str = json.toString(2);
        Text text = new Text(str);
        textFlowData.getChildren().add(text);
    }
    public void close(MouseEvent mouseEvent) {
        Stage stage = (Stage) textFlowData.getScene().getWindow();
        stage.close();
    }
}
