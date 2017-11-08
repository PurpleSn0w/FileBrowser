package FileBrowser;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;


/**
 * Created by uteus on 29.10.2017.
 */
public class FBwrapper {
    public FileBrowserTree fb;
    public HBox rootLine=new HBox();
    public VBox fbTree=new VBox();
    public VBox mainRow=new VBox();

    Button btnChooseRoot=new Button();
    public void ini(Stage stage,String path){
        rootLine.setFillHeight(true);
        mainRow.setFillWidth(true);
        fb=new FileBrowserTree(stage);
        fb.ini(path);
        rootLine.getChildren().addAll(fb.rootupBtn,fb.rootTextField);
        fbTree.getChildren().addAll(rootLine,fb.stringTree);
        btnChooseRoot.setText(fb.strings.RUcmChooseRoot);
        btnChooseRoot.setAlignment(Pos.CENTER);
        btnChooseRoot.setOnMouseClicked(event -> {
            fb.setRoot();
            if(fb.rootItem.file!=null){
                mainRow.getChildren().remove(0);
                mainRow.getChildren().add(fbTree);
            }
        });
        mainRow.setAlignment(Pos.CENTER);
        mainRow.getChildren().addAll(fb.rootItem.file.exists()?fbTree:btnChooseRoot);
    }
    public void resize(double w,double h){
        mainRow.setPrefWidth(w);
        mainRow.setPrefHeight(h);
        double temp=fb.rootTextField.getHeight();
        fb.rootupBtn.setPrefHeight(temp);       fb.rootupBtn.setPrefWidth(temp);
        fb.rootTextField.setPrefWidth(w-temp);
        fb.stringTree.setPrefHeight(h-temp);
    }
}
