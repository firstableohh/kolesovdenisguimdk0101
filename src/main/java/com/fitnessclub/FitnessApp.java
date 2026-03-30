package com.fitnessclub;

import com.fitnessclub.db.Database;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.nio.charset.StandardCharsets;

public class FitnessApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Database.getConnection();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        loader.setCharset(StandardCharsets.UTF_8);
        Parent root = loader.load();
        stage.setTitle("Фитнес-клуб — вход");
        Scene scene = new Scene(root, 440, 340);
        var css = FitnessApp.class.getResource("/styles/login.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
        stage.setScene(scene);
        stage.setMinWidth(380);
        stage.setMinHeight(280);
        stage.show();
    }

    @Override
    public void stop() {
        com.fitnessclub.service.Session.clear();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
