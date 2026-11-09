package com.grupo05.cnn_b;

import javafx.animation.*;
import javafx.beans.property.*;
import javafx.fxml.*;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import java.io.IOException;
import java.util.Locale;

/** A ten-pixel track with a responsive fill, independent of Modena ProgressBar. */
public final class ProbabilityRow extends VBox {
    @FXML private Circle dot;
    @FXML private Label className, percentage;
    @FXML private StackPane track;
    @FXML private Region fill;
    private final DoubleProperty progress=new SimpleDoubleProperty();
    private Timeline animation;
    public ProbabilityRow(String key,String name,String color,String soft,double probability,boolean winner,int index){
        FXMLLoader loader=new FXMLLoader(getClass().getResource("probability-row.fxml"));loader.setRoot(this);loader.setController(this);
        try{loader.load();}catch(IOException e){throw new IllegalStateException("No se pudo cargar la barra de probabilidad",e);}
        setId("probability-"+key);className.setText(name);percentage.setText(String.format(Locale.ROOT,"%.2f %%",probability*100));dot.setFill(Color.web(color));
        if(winner){className.getStyleClass().add("winning-label");percentage.getStyleClass().add("winning-label");fill.setEffect(new DropShadow(14,Color.web(color)));}
        fill.setStyle("-fx-background-color: linear-gradient(to right, "+soft+", "+color+");");
        fill.maxWidthProperty().bind(track.widthProperty().multiply(progress).map(value->Math.max(4,value.doubleValue())));
        progress.set(0);
        animation=new Timeline(new KeyFrame(Duration.millis(index*120),new KeyValue(progress,0)),new KeyFrame(Duration.millis(index*120+900),new KeyValue(progress,probability,Interpolator.SPLINE(.22,1,.36,1))));animation.play();
        setUserData(probability);
    }
    public void stop(){if(animation!=null)animation.stop();}
}
