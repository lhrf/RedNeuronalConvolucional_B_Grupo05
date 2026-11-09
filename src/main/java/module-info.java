module com.grupo05.cnn_b {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires javafx.swing;


    opens com.grupo05.cnn_b to javafx.fxml;
    exports com.grupo05.cnn_b;
}
