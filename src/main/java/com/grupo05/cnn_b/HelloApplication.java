package com.grupo05.cnn_b;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Parent root = fxmlLoader.load();
        UiStyles.apply(root);
        HelloController controller = fxmlLoader.getController();
        controller.setWindow(stage);
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double width = Math.min(Math.min(bounds.getWidth(), 1920), Math.max(Math.min(1024, bounds.getWidth()), bounds.getWidth() * .85));
        double height = Math.min(Math.min(bounds.getHeight(), 1080), Math.max(Math.min(680, bounds.getHeight()), bounds.getHeight() * .88 - 40));
        Scene scene = new Scene(root, width, height);
        var css = HelloApplication.class.getResource("app.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
        stage.setTitle("CNN_B · Análisis de ecografías mamarias");
        stage.setMinWidth(Math.min(1024, bounds.getWidth()));
        stage.setMinHeight(Math.min(680, bounds.getHeight()));
        stage.setOnCloseRequest(event -> controller.cerrarBackend());
        stage.setScene(scene);
        stage.show();
        controller.configureResponsive(scene);
        stage.setWidth(width);
        stage.setHeight(height);
        stage.setX(bounds.getMinX() + (bounds.getWidth() - stage.getWidth()) / 2);
        stage.setY(bounds.getMinY() + (bounds.getHeight() - stage.getHeight()) / 2);
    }
}
