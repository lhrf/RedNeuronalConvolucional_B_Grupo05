package com.grupo05.cnn_b;

import com.google.gson.JsonObject;
import javafx.animation.*;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.embed.swing.SwingFXUtils;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javax.imageio.ImageIO;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.*;

/** Presentation and input handling only; preprocessing and inference remain in Python. */
public final class DiagnosisController {
    private static final double LOW_CONFIDENCE=0.60;
    private static final String[] KEYS={"benign","malignant","normal"};
    private static final String[] NAMES={"Benigna","Maligna","Normal"};
    private static final String[] COLORS={"#F4B942","#D63A8A","#2EC4B6"};
    private static final String[] INKS={"#1F1600","#FFFFFF","#032420"};
    @FXML private ScrollPane diagnosticScreen;
    @FXML private GridPane cards;
    @FXML private VBox imageCard,resultCard,emptyDrop,emptyResult,readyResult,finalResult,probabilityRows;
    @FXML private VBox screenContent,screenHeading;
    private final List<Region> measuredRegions=new ArrayList<>();
    @FXML private HBox imageHeading,imageActions;
    @FXML private StackPane imageArea,imageFrame,resultBody;
    @FXML private ImageView imagePreview;
    @FXML private Label fileChip,imageCaption,stateText,readyFile,readyTitle,readyDescription,winnerName,winningProbability,inferenceTime;
    @FXML private HBox stateChip,winnerBadge,lowConfidence;
    @FXML private Circle stateDot,winnerDot;
    @FXML private Region badgeHalo;
    @FXML private Button removeButton,exampleButton,selectButton,diagnoseButton,exportButton;
    private Supplier<InferenceBackend> backend;
    private Executor worker;
    private Consumer<String> notice;
    private Path selectedImage;
    private boolean busy,loadingImage,active=true;
    private int selectionGeneration,sampleIndex;
    private double imageSize=260;
    private Boolean compact;
    private FadeTransition glowPulse;
    private Task<?> inferenceTask;
    private JsonObject lastResult;

    @FXML private void initialize(){
        Rectangle clip=new Rectangle();clip.widthProperty().bind(imageFrame.widthProperty());clip.heightProperty().bind(imageFrame.heightProperty());clip.setArcWidth(24);clip.setArcHeight(24);imageFrame.setClip(clip);
        imageArea.widthProperty().addListener((o,a,b)->sizeImage());
        diagnosticScreen.viewportBoundsProperty().addListener((o,a,b)->resize(diagnosticScreen.getWidth(),b.getHeight()));
        for(Region region:List.of(screenHeading,imageCard,imageHeading,imageActions)){
            region.heightProperty().addListener((o,a,b)->recalculateImageSize());
        }
        StackPane.setMargin(imageCaption,new Insets(0,10,10,10));
        exampleButton.addEventFilter(MouseEvent.MOUSE_CLICKED,e->e.consume());
        setState("Sin resultado",null);
    }
    void connect(Supplier<InferenceBackend> value,Executor executor,Consumer<String> notifier){backend=value;worker=executor;notice=notifier;updateButtons();}
    void backendChanged(){updateButtons();}
    void resize(double width,double height){
        boolean narrow=width<1100;
        if(compact==null||compact!=narrow){
            compact=narrow;cards.getColumnConstraints().clear();
            for(double percent:narrow?new double[]{100}:new double[]{58,42}){ColumnConstraints c=new ColumnConstraints();c.setPercentWidth(percent);c.setHgrow(Priority.ALWAYS);c.setFillWidth(true);cards.getColumnConstraints().add(c);}
            GridPane.setColumnIndex(resultCard,narrow?0:1);GridPane.setRowIndex(resultCard,narrow?1:0);
        }
        recalculateImageSize();
        sizeImage();
    }
    private void recalculateImageSize(){
        if(diagnosticScreen==null||screenContent==null||screenHeading==null||imageCard==null||imageHeading==null||imageActions==null)return;
        Insets viewportInsets=diagnosticScreen.getInsets();
        Insets contentInsets=screenContent.getInsets();
        Insets cardInsets=imageCard.getInsets();
        double viewportHeight=diagnosticScreen.getViewportBounds().getHeight()-viewportInsets.getTop()-viewportInsets.getBottom();
        double contentHeight=screenHeading.getLayoutBounds().getHeight()+screenContent.getSpacing()+contentInsets.getTop()+contentInsets.getBottom();
        double cardHeight=cardInsets.getTop()+cardInsets.getBottom()+imageHeading.getLayoutBounds().getHeight()+imageActions.getLayoutBounds().getHeight()+imageCard.getSpacing()*2;
        imageSize=clamp(viewportHeight-contentHeight-cardHeight-32,260,600);
        sizeImage();
    }    private double clamp(double value,double minimum,double maximum){return Math.max(minimum,Math.min(maximum,value));}
    private void sizeImage(){
        double side=Math.min(imageSize,Math.max(0,imageArea.getWidth()-36));
        imageFrame.setMinSize(side,side);imageFrame.setPrefSize(side,side);imageFrame.setMaxSize(side,side);
        emptyDrop.setMinHeight(imageSize);emptyDrop.setPrefHeight(imageSize);emptyDrop.setMaxHeight(imageSize);
        imageArea.setMinHeight((selectedImage==null?imageSize:side)+32);
        resultCard.setMinHeight((selectedImage==null?imageSize:side)+144);
        imagePreview.setFitWidth(Math.max(0,side-2));imagePreview.setFitHeight(Math.max(0,side-2));
    }
    @FXML private void selectImage(){
        if(busy)return;
        FileChooser chooser=new FileChooser();chooser.setTitle("Seleccionar ecografía");chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Ecografías · PNG, JPG, JPEG, BMP","*.png","*.jpg","*.jpeg","*.bmp"));
        File file=chooser.showOpenDialog(diagnosticScreen.getScene().getWindow());if(file!=null)loadImage(file.toPath());
    }
    @FXML private void selectFromDrop(MouseEvent event){if(event.getButton()==MouseButton.PRIMARY&&!isButton(event.getTarget()))selectImage();}
    private boolean isButton(Object target){for(Node n=target instanceof Node node?node:null;n!=null;n=n.getParent())if(n instanceof Button)return true;return false;}
    void loadImage(Path path){
        if(busy)return;
        if(!path.getFileName().toString().matches("(?i).*\\.(png|jpe?g|bmp)$")){notice.accept("Formato no compatible. Seleccione una imagen PNG, JPG o BMP.");return;}
        int generation=++selectionGeneration;loadingImage=true;updateButtons();
        Task<Image> load=new Task<>(){protected Image call()throws Exception{try(InputStream stream=Files.newInputStream(path)){Image image=new Image(stream);if(image.isError()||image.getWidth()==0)throw new IOException("El archivo no contiene una imagen legible.");return image;}}};
        load.setOnSucceeded(e->{
            if(generation!=selectionGeneration)return;
            loadingImage=false;selectedImage=path.toAbsolutePath().normalize();Image image=load.getValue();imagePreview.setImage(image);
            double crop=Math.min(image.getWidth(),image.getHeight());imagePreview.setViewport(new Rectangle2D((image.getWidth()-crop)/2,(image.getHeight()-crop)/2,crop,crop));
            fileChip.setText(path.getFileName().toString());imageCaption.setText(fileChip.getText());readyFile.setText(fileChip.getText());
            fileChip.setTooltip(new Tooltip(fileChip.getText()));show(fileChip,true);show(removeButton,true);show(emptyDrop,false);show(imageFrame,true);
            clearResult();showReady(false);sizeImage();updateButtons();
        });
        load.setOnFailed(e->{if(generation!=selectionGeneration)return;loadingImage=false;updateButtons();notice.accept("No se pudo leer la imagen. Seleccione otro archivo PNG, JPG o BMP.");});worker.execute(load);
    }
    @FXML private void useExample(){
        try(var files=Files.list(Path.of("modelo","ecografias_prueba"))){
            List<Path> examples=files.filter(p->p.getFileName().toString().matches("(?i).*\\.(png|jpe?g|bmp)$")).sorted().toList();
            if(examples.isEmpty())throw new IOException();loadImage(examples.get(sampleIndex++%examples.size()));
        }catch(IOException e){notice.accept("No se encontraron las ecografías de ejemplo en modelo/ecografias_prueba.");}
    }
    @FXML void reset(){
        if(busy)return;selectionGeneration++;loadingImage=false;selectedImage=null;imagePreview.setImage(null);imagePreview.setViewport(null);clearResult();
        show(fileChip,false);show(removeButton,false);show(imageFrame,false);show(emptyDrop,true);show(emptyResult,true);show(readyResult,false);show(finalResult,false);setState("Sin resultado",null);sizeImage();updateButtons();
    }
    private void showReady(boolean analyzing){
        show(emptyResult,false);show(finalResult,false);show(readyResult,true);
        readyTitle.setText(analyzing?"Análisis en curso":"Imagen lista para el análisis");
        readyDescription.setText(analyzing?"El modelo local calcula las probabilidades de la imagen.":"Presione «Diagnosticar» para ejecutar el Modelo B sobre la imagen cargada.");
        setState(analyzing?"Análisis en curso":"Imagen lista",analyzing?"#22D3EE":null);
    }
    @FXML void diagnose(){
        if(busy||loadingImage||selectedImage==null||backend.get()==null)return;
        busy=true;clearResult();showReady(true);updateButtons();
        Path input=selectedImage;InferenceBackend service=backend.get();
        Task<JsonObject> task=new Task<>(){protected JsonObject call()throws Exception{return service.predict(input);}};inferenceTask=task;
        task.setOnSucceeded(e->{busy=false;try{showResult(task.getValue());}catch(RuntimeException error){showReady(false);notice.accept("El modelo devolvió un resultado no válido. Vuelva a intentar el análisis.");}updateButtons();});
        task.setOnFailed(e->{busy=false;showReady(false);updateButtons();notice.accept("No se pudo completar el análisis. El modelo local no respondió: "+task.getException().getMessage());});
        worker.execute(task);
    }
    void showResult(JsonObject result){
        JsonObject probabilities=result.getAsJsonObject("probabilidades");
        Double[] values=new Double[3];Integer[] order={0,1,2};double sum=0;
        for(int i=0;i<3;i++){values[i]=probabilities.get(KEYS[i]).getAsDouble();if(!Double.isFinite(values[i])||values[i]<0||values[i]>1)throw new IllegalArgumentException("Probabilidad inválida");sum+=values[i];}
        if(Math.abs(sum-1)>.01)throw new IllegalArgumentException("Probabilidades inválidas");
        Arrays.sort(order,Comparator.<Integer>comparingDouble(i->values[i]).reversed());int winner=order[0];
        clearResult();lastResult=result.deepCopy();show(emptyResult,false);show(readyResult,false);show(finalResult,true);
        setState(NAMES[winner],COLORS[winner]);winnerName.setText(NAMES[winner]);winnerName.setStyle("-fx-text-fill: "+INKS[winner]+";");winnerDot.setFill(Color.web(INKS[winner]));
        winnerBadge.setStyle("-fx-background-color: "+COLORS[winner]+";");badgeHalo.setStyle("-fx-background-color: "+COLORS[winner]+";");badgeHalo.setEffect(new DropShadow(28,Color.web(COLORS[winner])));
        glowPulse=new FadeTransition(Duration.millis(1200),badgeHalo);glowPulse.setFromValue(.35);glowPulse.setToValue(.8);glowPulse.setInterpolator(Interpolator.EASE_BOTH);glowPulse.setAutoReverse(true);glowPulse.setCycleCount(Animation.INDEFINITE);if(active)glowPulse.play();
        winningProbability.setText(String.format(Locale.ROOT,"%.2f",values[winner]*100));
        for(int row=0;row<3;row++){int i=order[row];probabilityRows.getChildren().add(new ProbabilityRow(KEYS[i],NAMES[i],COLORS[i],COLORS[i]+"88",values[i],row==0,row));}
        inferenceTime.setText(String.format(Locale.ROOT,"%s ms",result.get("ms").getAsBigDecimal().stripTrailingZeros().toPlainString()));show(lowConfidence,values[winner]<LOW_CONFIDENCE);exportButton.setDisable(false);
    }
    private void clearResult(){lastResult=null;if(glowPulse!=null)glowPulse.stop();probabilityRows.getChildren().forEach(n->{if(n instanceof ProbabilityRow row)row.stop();});probabilityRows.getChildren().clear();exportButton.setDisable(true);}
    private void setState(String label,String color){stateText.setText(label);String foreground=color==null?"#6F84A3":color;stateText.setStyle("-fx-text-fill: "+foreground+";");stateDot.setFill(Color.web(foreground));stateChip.setStyle(color==null?"":"-fx-border-color: "+color+"55; -fx-background-color: "+color+"14;");}
    private void updateButtons(){diagnoseButton.setDisable(busy||loadingImage||selectedImage==null||backend==null||backend.get()==null);diagnoseButton.setText(busy?"Análisis en curso":"Diagnosticar");selectButton.setDisable(busy);exampleButton.setDisable(busy);removeButton.setDisable(busy);}
    private static void show(Node node,boolean visible){node.setVisible(visible);node.setManaged(visible);}
    @FXML private void dragOver(DragEvent e){if(!busy&&e.getDragboard().hasFiles()){e.acceptTransferModes(TransferMode.COPY);emptyDrop.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("drag-over"),true);}e.consume();}
    @FXML private void dragExited(DragEvent e){emptyDrop.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("drag-over"),false);}
    @FXML private void dragDropped(DragEvent e){boolean accepted=!busy&&e.getDragboard().hasFiles();if(accepted)loadImage(e.getDragboard().getFiles().getFirst().toPath());e.setDropCompleted(accepted);dragExited(e);e.consume();}
    @FXML private void exportResult(){
        if(lastResult==null)return;FileChooser chooser=new FileChooser();chooser.setTitle("Exportar resultado");chooser.setInitialFileName("resultado_cnn_b.png");chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagen PNG","*.png"));File file=chooser.showSaveDialog(diagnosticScreen.getScene().getWindow());if(file==null)return;
        try{
            WritableImage card=resultCard.snapshot(new SnapshotParameters(),null);
            javafx.scene.image.ImageView copy=new javafx.scene.image.ImageView(card);Label credit=new Label("Generado con CNN_B · Grupo 05 · UPN");credit.setStyle("-fx-text-fill: #6F84A3; -fx-font-size: 10px;");
            VBox export=new VBox(8,copy,credit);export.setStyle("-fx-background-color: #070F1E; -fx-padding: 12;");new Scene(export);export.applyCss();export.autosize();export.layout();
            ImageIO.write(SwingFXUtils.fromFXImage(export.snapshot(null,null),null),"png",file);notice.accept("Resultado guardado");
        }catch(IOException e){notice.accept("No se pudo guardar el resultado. Elija una carpeta con permiso de escritura.");}
    }
    void setActive(boolean value){active=value;if(glowPulse!=null){if(value)glowPulse.play();else glowPulse.pause();}}
    void dispose(){clearResult();if(inferenceTask!=null)inferenceTask.cancel();}
}
