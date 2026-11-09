package com.grupo05.cnn_b;

import javafx.animation.*;
import javafx.concurrent.Task;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.*;
import javafx.util.Duration;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

public class HelloController {
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> { Thread t = new Thread(r,"cnn-b-inference"); t.setDaemon(true); return t; });
    private InferenceBackend backend;
    private Window window;
    @FXML private StackPane rootStack, mainStack, pageHost;
    @FXML private ScrollPane diagnostic;
    @FXML private DiagnosisController diagnosticController;
    @FXML private BorderPane appRoot;
    @FXML private VBox sidebar, splash, backendErrorBox;
    @FXML private Label backendStatus, backendError, loadingMessage, loadingPercent, toastLabel;
    @FXML private ProgressBar loadingProgress;
    @FXML private Canvas networkCanvas;
    @FXML private Button navDiagnosis, navArchitecture, navMetrics, navAbout;
    @FXML private SVGPath ribbonPath;
    private final Map<String,Parent> screens = new HashMap<>();
    private Timeline architectureTimeline, loadingAnimation;
    private AnimationTimer networkAnimation;
    private int architectureStep;
    private Animation toastAnimation;

    public void initialize() { diagnosticController.connect(() -> backend,worker,this::mostrarToast); iniciarBackend(); }
    private void iniciarBackend() {
        long started=System.nanoTime();
        backendStatus.setText("Cargando");backendStatus.getStyleClass().setAll("status-loading");
        loadingProgress.setProgress(0);loadingPercent.setText("0 %");loadingMessage.setText("Preparación del modelo local");
        loadingAnimation=new Timeline(new KeyFrame(Duration.millis(120),e->{double p=Math.min(.88,loadingProgress.getProgress()+Math.max(.002,(.88-loadingProgress.getProgress())*.018));loadingProgress.setProgress(p);loadingPercent.setText(Math.round(p*100)+" %");}));loadingAnimation.setCycleCount(Animation.INDEFINITE);loadingAnimation.play();
        Task<InferenceBackend> init=new Task<>(){protected InferenceBackend call()throws Exception{InferenceBackend service=new InferenceBackend(Path.of(System.getProperty("cnn.root",System.getProperty("user.dir"))));try{service.awaitReady();return service;}catch(Exception e){service.close();throw e;}}};
        init.setOnSucceeded(e->{
            backend=init.getValue();backendStatus.setText("Listo");backendStatus.getStyleClass().setAll("status-ready");diagnosticController.backendChanged();
            PauseTransition wait=new PauseTransition(Duration.millis(Math.max(0,1500-(System.nanoTime()-started)/1_000_000)));
            wait.setOnFinished(done->{loadingAnimation.stop();Timeline finish=new Timeline(new KeyFrame(Duration.millis(360),new KeyValue(loadingProgress.progressProperty(),1,Interpolator.EASE_OUT)));finish.setOnFinished(end->{loadingPercent.setText("100 %");FadeTransition fade=new FadeTransition(Duration.millis(500),splash);fade.setToValue(0);fade.setOnFinished(x->{splash.setVisible(false);splash.setManaged(false);});fade.play();});finish.play();});wait.play();
        });
        init.setOnFailed(e->{loadingAnimation.stop();backendStatus.setText("Error");backendStatus.getStyleClass().setAll("status-error");backendError.setText("No se pudo iniciar el modelo local. "+init.getException().getMessage());backendErrorBox.setVisible(true);backendErrorBox.setManaged(true);loadingMessage.setText("El modelo no está disponible");});worker.execute(init);
    }
    public void setWindow(Window value){window=value;}
    @FXML private void irDiagnostico(){cambiarPantalla("diagnosis");}
    @FXML private void irArquitectura(){cambiarPantalla("architecture");}
    @FXML private void irMetricas(){cambiarPantalla("metrics");}
    @FXML private void irAcerca(){cambiarPantalla("about");}
    private void cambiarPantalla(String id){
        try{
            Parent next=diagnostic;
            if(!id.equals("diagnosis")){next=screens.get(id);if(next==null){FXMLLoader loader=new FXMLLoader(HelloApplication.class.getResource(id+"-view.fxml"));next=loader.load();UiStyles.apply(next);screens.put(id,next);if(id.equals("architecture"))prepararArquitectura(loader);}}
            pageHost.getChildren().setAll(next);diagnosticController.setActive(id.equals("diagnosis"));
            if(architectureTimeline!=null){if(id.equals("architecture"))architectureTimeline.play();else architectureTimeline.pause();}
            Map.of(navDiagnosis,"diagnosis",navArchitecture,"architecture",navMetrics,"metrics",navAbout,"about").forEach((button,key)->button.getStyleClass().setAll(id.equals(key)?"nav-active":"nav-button"));
            if(next instanceof ScrollPane scroll)scroll.setVvalue(0);next.setOpacity(0);next.setTranslateX(18);
            new Timeline(new KeyFrame(Duration.millis(320),new KeyValue(next.opacityProperty(),1,Interpolator.SPLINE(.22,1,.36,1)),new KeyValue(next.translateXProperty(),0,Interpolator.SPLINE(.22,1,.36,1)))).play();
        }catch(Exception e){mostrarToast("No se pudo abrir esta pantalla: "+e.getMessage());}
    }
    private void prepararArquitectura(FXMLLoader loader){
        GridPane blocks=(GridPane)loader.getNamespace().get("architectureBlocks");
        Label stage=(Label)loader.getNamespace().get("architectureStage"),description=(Label)loader.getNamespace().get("architectureDescription");
        String[] names={"ENTRADA · IMAGEN","DETECCIÓN · RASGOS VISUALES","RESUMEN · SEÑALES RELEVANTES","COMBINACIÓN · EVIDENCIA","RESULTADO · PROBABILIDADES"};
        String[] details={"La ecografía se ajusta como imagen de entrada del análisis.","EfficientNetB0 identifica patrones visuales útiles en la imagen.","Las señales detectadas se resumen para conservar la información relevante.","El clasificador combina los rasgos para estimar las tres clases.","Las probabilidades Benigna, Maligna y Normal se comparan para elegir la clase."};
        for(Node node:blocks.getChildren()){GridPane.setHgrow(node,Priority.ALWAYS);GridPane.setFillWidth(node,true);if(node instanceof Region region){region.setMaxWidth(Double.MAX_VALUE);region.setMinHeight(250);}}
        Runnable activate=()->{
            for(int i=0;i<blocks.getChildren().size();i++){
                Node card=blocks.getChildren().get(i);card.getStyleClass().removeAll("active-step","processed-step");
                if(i<architectureStep)card.getStyleClass().add("processed-step");
                if(i==architectureStep){card.getStyleClass().add("active-step");stage.setText("ETAPA ACTIVA · "+names[i]);description.setText(details[i]);Node visual=card.lookup(".arch-visual");if(visual!=null){visual.setScaleX(.96);visual.setScaleY(.96);ScaleTransition pulse=new ScaleTransition(Duration.millis(420),visual);pulse.setToX(1);pulse.setToY(1);pulse.setInterpolator(Interpolator.SPLINE(.22,1,.36,1));pulse.play();}}
            }
        };
        activate.run();
        Button pause=(Button)loader.getNamespace().get("pauseButton"),repeat=(Button)loader.getNamespace().get("repeatButton");
        architectureTimeline=new Timeline(new KeyFrame(Duration.seconds(2),e->{architectureStep=(architectureStep+1)%blocks.getChildren().size();activate.run();}));architectureTimeline.setCycleCount(Animation.INDEFINITE);
        pause.setOnAction(e->{if(architectureTimeline.getStatus()==Animation.Status.RUNNING){architectureTimeline.pause();pause.setText("Reproducir");}else{architectureTimeline.play();pause.setText("Pausar");}});repeat.setOnAction(e->{architectureStep=0;activate.run();architectureTimeline.playFromStart();});
    }
    public void configureResponsive(Scene scene){
        networkCanvas.widthProperty().bind(mainStack.widthProperty());networkCanvas.heightProperty().bind(mainStack.heightProperty());startNetworkAnimation();
        ribbonPath.getStrokeDashArray().setAll(100d);ribbonPath.setStrokeDashOffset(100);new Timeline(new KeyFrame(Duration.millis(1400),new KeyValue(ribbonPath.strokeDashOffsetProperty(),0,Interpolator.SPLINE(0,0,.2,1)))).play();
        Map<Button,String> texts=new LinkedHashMap<>();for(Node child:sidebar.getChildren())if(child instanceof Button button)texts.put(button,button.getText());
        Runnable layout=()->{
            boolean compact=appRoot.getWidth()<1100;sidebar.setMinWidth(compact?68:232);sidebar.setPrefWidth(compact?68:232);sidebar.setMaxWidth(compact?68:232);
            texts.forEach((button,text)->{button.setText(compact?"":text);button.setTooltip(compact?new Tooltip(text):null);});
            for(Node child:sidebar.getChildren())if(child instanceof Label||child.getStyleClass().contains("brand-block")||child.getStyleClass().contains("model-status")){child.setVisible(!compact);child.setManaged(!compact);}
            diagnosticController.resize(scene.getWidth(),window==null?appRoot.getHeight():window.getHeight());
        };
        appRoot.widthProperty().addListener((o,a,b)->layout.run());appRoot.heightProperty().addListener((o,a,b)->layout.run());layout.run();
    }
    private void startNetworkAnimation(){
        var gc=networkCanvas.getGraphicsContext2D();List<double[]> points=new ArrayList<>();
        networkAnimation=new AnimationTimer(){long last;public void handle(long now){
            if(now-last<33_333_333||window instanceof Stage stage&&stage.isIconified())return;last=now;double w=networkCanvas.getWidth(),h=networkCanvas.getHeight();if(w<1||h<1)return;
            if(points.isEmpty())for(int i=0;i<36;i++)points.add(new double[]{Math.random(),Math.random(),(Math.random()-.5)*.0003,(Math.random()-.5)*.0003});gc.clearRect(0,0,w,h);
            for(int i=0;i<points.size();i++){double[] p=points.get(i);p[0]+=p[2];p[1]+=p[3];if(p[0]<0||p[0]>1)p[2]*=-1;if(p[1]<0||p[1]>1)p[3]*=-1;for(int j=i+1;j<points.size();j++){double[] q=points.get(j);double d=Math.hypot((p[0]-q[0])*w,(p[1]-q[1])*h);if(d<170){gc.setStroke(Color.rgb(34,211,238,.07*(1-d/170)));gc.strokeLine(p[0]*w,p[1]*h,q[0]*w,q[1]*h);}}gc.setFill(Color.rgb(94,227,245,.28));gc.fillOval(p[0]*w-1.5,p[1]*h-1.5,3,3);}
        }};networkAnimation.start();
    }
    private void mostrarToast(String message){
        if(toastAnimation!=null)toastAnimation.stop();toastLabel.setText(message);toastLabel.setVisible(true);toastLabel.setManaged(true);toastLabel.setOpacity(0);
        toastAnimation=new Timeline(new KeyFrame(Duration.ZERO,new KeyValue(toastLabel.translateYProperty(),-12)),new KeyFrame(Duration.millis(200),new KeyValue(toastLabel.opacityProperty(),1),new KeyValue(toastLabel.translateYProperty(),-20)),new KeyFrame(Duration.millis(2600),new KeyValue(toastLabel.opacityProperty(),1)),new KeyFrame(Duration.millis(2800),new KeyValue(toastLabel.opacityProperty(),0)));toastAnimation.setOnFinished(e->{toastLabel.setVisible(false);toastLabel.setManaged(false);});toastAnimation.play();
    }
    @FXML private void reintentar(){backendErrorBox.setVisible(false);backendErrorBox.setManaged(false);splash.setOpacity(1);splash.setVisible(true);splash.setManaged(true);iniciarBackend();}
    public void cerrarBackend(){diagnosticController.dispose();if(networkAnimation!=null)networkAnimation.stop();if(loadingAnimation!=null)loadingAnimation.stop();if(architectureTimeline!=null)architectureTimeline.stop();worker.shutdownNow();if(backend!=null)backend.close();}
}
