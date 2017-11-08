package sample;

import FileBrowser.FBwrapper;
import FileBrowser.FileBrowserTree;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Timer;
import java.util.TimerTask;

public class Main extends Application {
    Group root = new Group();
    Scene scene=new Scene(root,600,600);
    int time=0;
    FBwrapper fbWrapper;
    @Override
    public void start(Stage stage) throws Exception{
        stage.setX(70);stage.setY(50);
        fbWrapper=new FBwrapper();

        stage.setTitle(Strings.RU_title);
        stage.setScene(scene);
        stage.show();
        //-------------------------------------
        //fbWrapper.ini(stage,"C:/JAVA/");
        fbWrapper.ini(stage,"C:/JAVA/root/test — копия (3)");
        //fbWrapper.ini(stage,"");
        fbWrapper.resize(scene.getWidth(),scene.getHeight());
        //-------------------------------------
        root.getChildren().add(fbWrapper.mainRow);
        //-------------------------------------
        stage.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                fbWrapper.resize(scene.getWidth(),scene.getHeight());
            }
        });
        stage.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                fbWrapper.resize(scene.getWidth(),scene.getHeight());
            }
        });
        //-------------------------------------
        Timer timer = new Timer(true);
        TimerTask timerTask = new MyTimerTask();
        fbWrapper.resize(scene.getWidth(),scene.getHeight());
        //timer.scheduleAtFixedRate(timerTask, 0, 100);
    }
    class MyTimerTask extends TimerTask{
        @Override
        public void run(){
            System.err.println(
                    time+"      "
                    );
            time++;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
