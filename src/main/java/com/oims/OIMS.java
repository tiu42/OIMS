package com.oims;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class OIMS extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("features/auth/login-view.fxml"));

        Parent root = fxmlLoader.load();

        stage.setScene(new Scene(root));
        stage.setTitle("Glocerimex");
        stage.show();
    }
}
