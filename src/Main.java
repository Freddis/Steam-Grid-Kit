import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL res = getClass().getResource("/views/main/main.fxml");
        Stage stage = FXMLLoader.load(res);
        primaryStage.setScene(stage.getScene());
        primaryStage.setTitle(stage.getTitle());
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
