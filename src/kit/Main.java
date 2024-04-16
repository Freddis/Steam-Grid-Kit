package kit;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;
import java.net.URL;

public class Main extends Application {

    private static Application current;
    public static Application getCurrent()
    {
        return current;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        current = this;
        //Don't want to use primary stage, since it cannot be controlled with FXML and I want all styles to be in FXML
        URL res = getClass().getResource("/views/main/main.fxml");
        Stage stage = FXMLLoader.load(res);
        stage.getIcons().add(new Image("/views/assets/steam.ico"));
        stage.show();
        stage.setTitle(stage.getTitle()+" v"+Config.getVersion());
        stage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
