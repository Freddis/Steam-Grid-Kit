package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setMinHeight(600);
        primaryStage.setMinWidth(550);
        primaryStage.setTitle("Steam Grid Kit");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
        Parent root = loader.load();
        MainFormController mainFormController = loader.getController();


        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
//        scene.getStylesheets().add("css/style.css");
        primaryStage.show();
        mainFormController.init(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
