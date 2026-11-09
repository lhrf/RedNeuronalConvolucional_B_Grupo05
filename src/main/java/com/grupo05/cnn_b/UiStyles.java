package com.grupo05.cnn_b;

import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

/** Applies shared styling to both eagerly and lazily loaded FXML trees. */
final class UiStyles {
    static void apply(Node node) {
        var classes = new java.util.ArrayList<String>();
        for (String entry : node.getStyleClass())
            for (String name : entry.split("\\s+")) if (!name.isBlank()) classes.add(name);
        node.getStyleClass().setAll(classes);
        if (node instanceof Button button) {
            FadeTransition hover = new FadeTransition(Duration.millis(160), button);
            button.hoverProperty().addListener((o, old, on) -> {
                hover.stop(); hover.setToValue(on ? .9 : 1); hover.play();
            });
        }
        if (node instanceof Parent parent) parent.getChildrenUnmodifiable().forEach(UiStyles::apply);
        if (node instanceof javafx.scene.control.ScrollPane scroll && scroll.getContent() != null) apply(scroll.getContent());
    }
}
