package com.grupo05.cnn_b;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.animation.PauseTransition;
import javafx.application.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import javax.imageio.ImageIO;
import java.nio.file.*;
import java.util.*;
import java.util.function.BooleanSupplier;

/** Standalone verification, compiled only by tools/capture-step1.ps1. Never packaged. */
public final class CaptureLauncher {
    public static void main(String[] args){Application.launch(CaptureApplication.class,args);}
    public static final class CaptureApplication extends Application {
        private Stage stage;
        private Scene scene;
        private Parent root;
        private HelloController shell;
        private DiagnosisController diagnosis;
        private Map<String,Object> fields;
        private final Path out=Path.of("diseno","verificacion");
        private long deadline;
        @Override public void start(Stage value)throws Exception{
            stage=value;Files.createDirectories(out);
            FXMLLoader loader=new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));root=loader.load();UiStyles.apply(root);shell=loader.getController();
            diagnosis=(DiagnosisController)loader.getNamespace().get("diagnosticController");
            shell.setWindow(stage);scene=new Scene(root,1024,640);scene.getStylesheets().add(HelloApplication.class.getResource("app.css").toExternalForm());
            stage.setScene(scene);stage.setTitle("CNN_B · Verificación del paso 1");stage.setWidth(1024);stage.setHeight(680);stage.show();shell.configureResponsive(scene);
            Thread.setDefaultUncaughtExceptionHandler((thread,error)->{error.printStackTrace();Platform.runLater(()->finish(false));});
            deadline=System.nanoTime()+120_000_000_000L;
            until(()->!root.lookup("#splash").isVisible(),()->{
                setCaptureSize(1366,768);later(200,()->{save("paso1-vacio-1366x768.png");loadExample();});
            });
        }
        private void loadExample(){
            try(var files=Files.list(Path.of("modelo","ecografias_prueba"))){
                Path malignant=files.filter(p->p.getFileName().toString().startsWith("malignant")).findFirst().orElseThrow();
                diagnosis.loadImage(malignant);
                until(()->root.lookup("#readyResult").isVisible(),()->later(200,()->{save("paso1-imagen-lista-1366x768.png");diagnosis.diagnose();awaitResult();}));
            }catch(Exception e){throw new RuntimeException(e);}
        }
        private void awaitResult(){until(()->root.lookup("#finalResult").isVisible(),()->later(1400,()->{
            String label=((Label)root.lookup("#winnerName")).getText();double probability=Double.parseDouble(((Label)root.lookup("#winningProbability")).getText());
            if(!label.equals("Maligna")||probability<60)throw new IllegalStateException("La imagen real no produjo Maligna con alta confianza: "+label+" "+probability);
            save("paso1-maligna-real-1366x768.png");System.out.println("Resultado REAL: "+label+" "+probability+" %, "+((Label)root.lookup("#inferenceTime")).getText());
            JsonObject fixture=JsonParser.parseString("{\"ok\":true,\"clase\":\"benign\",\"probabilidades\":{\"benign\":0.48,\"malignant\":0.37,\"normal\":0.15},\"ms\":123}").getAsJsonObject();
            diagnosis.showResult(fixture);System.out.println("Fixture SOLO DE CAPTURA: baja confianza 48 / 37 / 15 %, ms=123 simulado; backend sin cambios.");
            later(1400,()->{save("paso1-baja-confianza-simulada-1366x768.png");setCaptureSize(1024,680);later(300,()->{
                ScrollPane scroll=(ScrollPane)root.lookup("#diagnosticScreen");scroll.setVvalue(1);later(200,()->{
                    save("paso1-scroll-1024x680.png");verifyScroll(scroll);verifyRows();verifyReset();
                });
            });});
        }));}
        private void verifyRows(){
            VBox rows=(VBox)root.lookup("#probabilityRows");double previous=1;
            for(Node node:rows.getChildren()){double value=(double)node.getUserData();if(value>previous)throw new IllegalStateException("Barras fuera de orden");previous=value;}
            if(!root.lookup("#lowConfidence").isVisible())throw new IllegalStateException("No aparece el aviso de confianza baja");
            if(((VBox)root.lookup("#finalResult")).lookupAll(".progress-bar").size()!=0)throw new IllegalStateException("Todavía hay ProgressBar nativos");
            System.out.println("Comprobaciones: argmax, orden descendente, aviso de confianza y barras propias: OK.");
        }
        private void verifyScroll(ScrollPane scroll){
            ScrollBar bar=scroll.lookupAll(".scroll-bar").stream().filter(n->n instanceof ScrollBar s&&s.getOrientation()==javafx.geometry.Orientation.VERTICAL).map(n->(ScrollBar)n).findFirst().orElseThrow();
            if(!bar.isVisible())throw new IllegalStateException("No hay scroll vertical en la ventana compacta");
            for(Node arrow:bar.lookupAll(".increment-arrow"))if(arrow.getBoundsInParent().getWidth()>0)throw new IllegalStateException("Flecha visible");
            System.out.println("Scrollbar compacta: visible, oscura y sin flechas; ancho="+bar.getWidth());
        }
        private void verifyReset(){
            diagnosis.reset();if(!root.lookup("#emptyResult").isVisible()||root.lookup("#finalResult").isVisible())throw new IllegalStateException("No se limpió el resultado");
            System.out.println("Nuevo análisis: vuelve a vacío, borra resultado y deshabilita diagnóstico: OK.");finish(true);
        }
        private void setCaptureSize(double w,double h){
            double decorationWidth=stage.getWidth()-scene.getWidth(),decorationHeight=stage.getHeight()-scene.getHeight();
            stage.setWidth(w+decorationWidth);stage.setHeight(h+decorationHeight);
            diagnosis.resize(w,h);
        }
        private void save(String name){try{
            root.applyCss();root.layout();
            var shot=scene.snapshot(null);ImageIO.write(SwingFXUtils.fromFXImage(shot,null),"png",out.resolve(name).toFile());System.out.println("Scene.snapshot "+(int)shot.getWidth()+"x"+(int)shot.getHeight()+": "+out.resolve(name).toAbsolutePath());
        }catch(Exception e){throw new RuntimeException(e);}}
        private void until(BooleanSupplier condition,Runnable next){if(condition.getAsBoolean()){next.run();return;}if(System.nanoTime()>deadline)throw new IllegalStateException("Tiempo de espera agotado");later(100,()->until(condition,next));}
        private void later(double ms,Runnable action){PauseTransition wait=new PauseTransition(Duration.millis(ms));wait.setOnFinished(e->{try{action.run();}catch(Throwable error){error.printStackTrace();finish(false);}});wait.play();}
        private void finish(boolean success){shell.cerrarBackend();stage.close();Platform.exit();if(!success)System.exit(1);}
    }
}
