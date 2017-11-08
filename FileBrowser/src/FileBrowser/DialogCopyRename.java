package FileBrowser;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.File;
import java.nio.file.Path;

/**
 * Created by uteus on 06.11.2017.
 */
public class DialogCopyRename extends Dialog<String> {
    Strings strings=new Strings();
    ButtonType buttonTypeChange = new ButtonType(strings.RUdiChange);
    VBox vbox=new VBox();
    Text header=new Text(strings.RUdiAlreadyExists);
    HBox hbox=new HBox();
    Text copyAs=new Text(strings.RUdiCopyAs);
    TextField textField=new TextField();
    //Button btnChange=new Button(Strings.RUdiChange);
    DialogCopyRename(String s){
        final DialogPane dialogPane = getDialogPane();
        dialogPane.getButtonTypes().addAll(buttonTypeChange,ButtonType.CANCEL,ButtonType.OK);
        //-----------------------------------------------
        int slashPos=0;String ext="";
        String oldName=s;
        for(int i=0;i<s.length();i++){
            if(i<s.length()-1 && s.substring(i,i+1).compareTo(".")==0){
                slashPos=i;
                ext=s.substring(slashPos);
                oldName=s.substring(0,slashPos);
            }
        }
        //textField.setText(s+Strings.RUdiCopy);
        textField.setText(oldName+strings.RUdiCopy+ext);
        vbox.setSpacing(10);
        vbox.setMaxWidth(Double.MAX_VALUE);
        vbox.setAlignment(Pos.CENTER);
        hbox.setAlignment(Pos.CENTER);
        hbox.getChildren().addAll(copyAs,textField);
        vbox.getChildren().addAll(header,hbox);
        getDialogPane().setContent(vbox);

        setResultConverter((dialogButton) -> {
            ButtonBar.ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();
            if(data==ButtonBar.ButtonData.OTHER)return s;
            else if(data==ButtonBar.ButtonData.OK_DONE)return textField.getText();
            else {
                return null;
            }
        });
    }
}
