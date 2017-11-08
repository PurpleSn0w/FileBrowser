package FileBrowser;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.scene.control.TextField;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardCopyOption.*;
//import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Created by uteus on 21.09.2017.
 */
public class FileBrowserTree {
    Strings strings=new Strings();
    boolean readyToRefresh=false;
    //general-------------------------------------------------------------
    File rootDir;                                                   //используется для выбора корня через диалоговое окно
    Stage stage;                                                    //используется там же
    int itemToReturn=0;                                             //сюда сохраняется номер выделенного элемента дерева, чтобы после обновления
                                                                    //и повторного развёртывания доскролить дерево до этого же элемента
    int selectedItemInt=0;
    public TreeView<String> stringTree =new TreeView<>();           //собственно дерево
    public TextField rootTextField=new TextField("qwe");            //хранит, отображает и управляет корнем
    public Button rootupBtn=new Button();
    DirectoryChooser dirChooser = new DirectoryChooser();           //используется вместе с rootDir для выбора корня через диалоговое окно
    TreeView<String> expandedSet =new TreeView<>();                 //дерево для хранения развёрнутых элементов
    //StringTreeItem rootItem;                                        //корень
    StringTreeItem rootItem;                                        //корень
    public TreeItem selectedItem=new TreeItem();                                          //сюда сохраняется выдеенный элемент после отпускания кнопки мыши
    File copy;
    boolean readyToCopy=false;
    Path pathSource, pathTarget;
    //settings-------------------------------------------------------------
    public boolean sortDir=true;                                    //сортировать ли по признаку файл-папка (приоритет 1 - наивысший)
    public boolean sortType=true;                                   //сортировать ли по типу файлов (приоритет 2)
    public boolean sortReg=false;                                   //сортировать ли с учётом регистра (приоритет 3)
    public boolean setImgDir=true;                                  //устанавливать ли картинку для папок в дереве
    public boolean saveLastRootDir=true;                            //сохранять ли последнее расположение в диалоге выбора корня
    public int timeOfDoubleClick=400;                               //максимальное время между отпусканиями мыши для двойного клика
    //public EventType<MouseEvent> boubleClickEvent=MouseEvent.MOUSE_RELEASED;
    public EventType<MouseEvent> boubleClickEvent=MouseEvent.MOUSE_RELEASED;
    //context menu & items-------------------------------------------------
    ContextMenu contMenu =new ContextMenu();
    MenuItem ImakeRoot   =new MenuItem(strings.RUcmFileBrowser);//сделать выделенный элемент корнем
    MenuItem IсhooseRoot =new MenuItem(strings.RUcmChooseRoot);//выбрать новый корень через диалог
    MenuItem IrefreshTree=new MenuItem(strings.RUcmRefresh); //обновить дерево с развёртыванием и прокруткой до выделенного элемента
    MenuItem IrenameItem=new MenuItem(strings.RUcmRename);   //переименовать элемент дерева и соотв. файл/папку в файловой системе
    MenuItem IdeleteItem=new MenuItem(strings.RUcmDelete);   //удалить элемент дерева и соотв. файл/папку в файловой системе
    MenuItem IcopyItem   =new MenuItem(strings.RUcmCopy);
    MenuItem IpasteItem  =new MenuItem(strings.RUcmPaste);
    //statics--------------------------------------------------------------
    static double baseTextWidth;                                    //используется дя расчёта базовой величины
    static double baseItemHeight;                                   //базовая величина, от которой отталкиваются определения размеров
    static void baseIni(){                                          //расчёт базовой величины
        Text t=new Text("123");
        baseTextWidth=t.getStrokeWidth();
        baseItemHeight =baseTextWidth*16;
    }                                      //расчёт базовой величины
    void staticIniContMenu(){                                //наполнение контекстного меню элементами и картинками
        if(!(contMenu.getItems().isEmpty()))contMenu.getItems().remove(0,contMenu.getItems().size());
        contMenu.getItems().addAll( IcopyItem,IpasteItem,IdeleteItem,IrenameItem,
                                    new SeparatorMenuItem(),
                                    ImakeRoot,IсhooseRoot,IrefreshTree);
        ImakeRoot.setText(strings.RUcmFileBrowser);
        IсhooseRoot.setText(strings.RUcmChooseRoot);
        IrefreshTree.setText(strings.RUcmRefresh);
        IrenameItem.setText(strings.RUcmRename);
        IdeleteItem.setText(strings.RUcmDelete);
        IcopyItem.setText(strings.RUcmCopy);
        IpasteItem.setText(strings.RUcmPaste);
        ImageView imgSetRoot=new ImageView("img/makeRoot.png");
        ImageView imgChooseRoot=new ImageView("img/chooseRoot.png");
        ImageView imgRefresh=new ImageView("img/refresh2.png");
        ImageView imgRename=new ImageView("img/rename.png");
        ImageView imgDelete=new ImageView("img/delete.png");
        imgSetRoot.setFitWidth(baseItemHeight);imgSetRoot.setFitHeight(baseItemHeight);
        imgChooseRoot.setFitWidth(baseItemHeight);imgChooseRoot.setFitHeight(baseItemHeight);
        imgRefresh.setFitWidth(baseItemHeight);imgRefresh.setFitHeight(baseItemHeight);
        imgRename.setFitWidth(baseItemHeight);imgRename.setFitHeight(baseItemHeight);
        imgDelete.setFitWidth(baseItemHeight);imgDelete.setFitHeight(baseItemHeight);
        ImakeRoot.setGraphic(imgSetRoot);
        IсhooseRoot.setGraphic(imgChooseRoot);
        IrefreshTree.setGraphic(imgRefresh);
        IrenameItem.setGraphic(imgRename);
        IdeleteItem.setGraphic(imgDelete);
    }                            //наполнение контекстного меню элементами и картинками
    //double click catching------------------------------------------------
    private boolean doubleClicked=false;                            //хранит инфу о том, было ли последнее отпускание мыши
                                                                    //вторым в двойном щелчке
    private Long firstClick=System.currentTimeMillis();             //используется для замера времени между отпусканиями
    //---------------------------------------------------------------------
    public FileBrowserTree(Stage stage){
        this.stage=stage;
        baseIni();
        //staticIniContMenu();
    }
    //---------------------------------------------------------------------
    //сохраняет развёрнутые дочерние элементы tws в дочерние элементы set
    void saveExpandedSetRecursive(TreeItem<String> set, StringTreeItem tws){
        int twsSize=tws.getChildren().size();
        for(int i=0;i<twsSize;i++){
            if(tws.getChildren().get(i).isExpanded()){
                set.getChildren().add(new TreeItem<>(tws.getChildren().get(i).getValue()));
                saveExpandedSetRecursive(set.getChildren().get(set.getChildren().size()-1), //item, только что добавленный в twn
                        (StringTreeItem)tws.getChildren().get(i));                      //соотв. item в tws
            }
        }
    }
    //сохраняет развёрнутые элементы stringTree в отдельное дерево expandedSet
    void saveExpandedSet(){
        expandedSet.setRoot(new TreeItem<>(rootItem.getValue()));
        saveExpandedSetRecursive(expandedSet.getRoot(),rootItem);
    }
    //разворачивает дерево stringTree в соответствии с expandedSet
    void loadExpandedSetRecursive(TreeItem<String> newItem,TreeItem<String> expItem){
        int newSize=newItem.getChildren().size();
        int expSize=expItem.getChildren().size();
        if(expSize>0) {
            for (int i = 0; i < newSize; i++) {
                for (int j = 0; j < expSize; j++) {
                    if (newItem.getChildren().get(i).getValue().compareTo(expItem.getChildren().get(j).getValue())==0) {
                        newItem.getChildren().get(i).setExpanded(true);
                        loadExpandedSetRecursive(newItem.getChildren().get(i), expItem.getChildren().get(j));
                    }
                }
            }
        }
    }
    //обновляет дерево, возвращает его в развёрнутый вид, скроллит до выделенного элемента
    void fullRefreshTree(){
        itemToReturn=selectedItem!=null?stringTree.getRow(selectedItem):0;
        saveExpandedSet();
        refreshTree();
        loadExpandedSetRecursive(rootItem,expandedSet.getRoot());
        if(stringTree.getTreeItem(itemToReturn)!=null){
            stringTree.scrollTo(Math.max(0,itemToReturn));
            stringTree.getSelectionModel().select(itemToReturn);
        }
        else stringTree.scrollTo(Math.max(0,itemToReturn-1));
    }
    void softRefreshTree(StringTreeItem item){
        saveExpandedSet();
        if(item.file!=null){
            if(item.file.isDirectory())item.reBuildChildren();
            else ((StringTreeItem)item.getParent()).reBuildChildren();
        }
        loadExpandedSetRecursive(rootItem,expandedSet.getRoot());
    }
    //---------------------------------------------------------------------
    void iniContMenu(){
        ImakeRoot.setOnAction(event -> {
            if(((StringTreeItem)stringTree.getSelectionModel().getSelectedItem()).file.isDirectory()) {
                ini(((StringTreeItem) (stringTree.getSelectionModel().getSelectedItem())).file.getPath());
            }
        });
        IсhooseRoot.setOnAction(event -> setRoot());
        IrefreshTree.setOnAction(event -> fullRefreshTree());
        IrenameItem.setOnAction(event -> renameItem());
        IdeleteItem.setOnAction(event -> deleteItem());
        IcopyItem.setOnAction(event -> {
            copy=((StringTreeItem)selectedItem).file;
            //pathSource= getPathFromSourceFile(copy);
            pathSource=FileSystems.getDefault().getPath(((StringTreeItem)selectedItem).file.getAbsolutePath());
            if(copy!=null && copy.exists())readyToCopy=true;
        });
        IpasteItem.setOnAction(event -> {
            if(readyToCopy){
                if(recursiveCopy(copy,((StringTreeItem)selectedItem).file)){
                    softRefreshTree((StringTreeItem)selectedItem);
                };
            }
        });
        stringTree.setContextMenu(contMenu);
    }
    //copy-paste-----------------------------------------------------------
    Path getPathFromSourceFile(File file){
        return FileSystems.getDefault().getPath(file.getAbsolutePath());
    }
    Path getPathFromTargetFile(File source,File target){
        if(target.isDirectory())return FileSystems.getDefault().getPath(target.getAbsolutePath()+"/"+source.getName());
        else return FileSystems.getDefault().getPath(target.getParent()+"/"+source.getName());
    }
    Path getPathFromRenamedTargetFile(File target,String name){
        if(target.isDirectory())return FileSystems.getDefault().getPath(target.getAbsolutePath()+"/"+name);
        else return FileSystems.getDefault().getPath(target.getParent()+"/"+name);
    }
    void alertCouldnotCopy(File source,File target){
        Alert couldNot=new Alert(Alert.AlertType.ERROR);
        couldNot.setHeaderText(source.isDirectory()?strings.RUerCopyDir:strings.RUerCopyFile);
        couldNot.setContentText(source.getName()+"\n"+strings.RUerFrom+source.getParent()+"\n"+
                strings.RUerTo+(target.isDirectory()?target.getAbsolutePath():target.getParent()));
        couldNot.show();
    }
    boolean ifAlreadyExists(File source,File target){
        return new File(getPathFromTargetFile(source,target).toString()).exists();
    }
    String primitiveCopy(File source,File target,CopyOption... opts){
        try {
            Files.copy(getPathFromSourceFile(source), getPathFromTargetFile(source, target), opts);
            return source.getName();
        } catch (IOException e) {
            alertCouldnotCopy(source, target);
            return null;
        }
    }
    boolean primitiveCopy(File source,File target,String name,CopyOption... opts){
        try {
            Files.copy(getPathFromSourceFile(source), getPathFromRenamedTargetFile(target,name), opts);
            return true;
        } catch (IOException e) {
            alertCouldnotCopy(source, target);
            return false;
        }
    }
    String simpleCopy(File source,File target,CopyOption... opts){
        if(ifAlreadyExists(source,target)){
            DialogCopyRename d=new DialogCopyRename(source.getName());
            Optional<String> result=d.showAndWait();
            result.ifPresent(name -> {
                if(name!=null){
                    primitiveCopy(source,target,name,REPLACE_EXISTING,NOFOLLOW_LINKS);
                }
            });
            if(result.isPresent())return result.get();
            else return null;
        }
        else if(target.equals(source)){
            Alert copyError0=new Alert(Alert.AlertType.ERROR);
            copyError0.setContentText(strings.RUdiEr0);
            copyError0.show();
            return null;
        }
        //else return primitiveCopy(source,target,opts);
        else return primitiveCopy(source,target,opts);
    }
    boolean recursiveCopy(File source,File Target,CopyOption... opts){
        File target;
        if(Target.isFile())target=Target.getParentFile();
        else target=Target;
        String newName=simpleCopy(source,target,opts);
        if(newName!=null){
            if(source.isDirectory()){
                //File newTarget=new File(target.getAbsolutePath()+"/"+source.getName());
                File newTarget=new File(target.getAbsolutePath()+"/"+newName);
                if(newTarget.exists()){
                    File F[]=source.listFiles();
                    for(File f:F){
                        recursiveCopy(f,newTarget,opts);
                    }
                }
            }
            return true;
        }
        return false;
        //-------------------------------
        /*if(simpleCopy(source,target,opts)){
        }
        else{
            return false;
        }
        return true;*/
    }
    //---------------------------------------------------------------------
    void iniTree(String path){
        //rootItem=new StringTreeItem(path,sortDir,sortType,sortReg,setImgDir);
        rootItem=new StringTreeItem(path,sortDir,sortType,sortReg,setImgDir);
    }
    void iniTree(String path,boolean SORT_DIR,boolean SORT_TYPE,boolean SORT_REG,boolean SET_IMG_DIR){
        rootItem=new StringTreeItem(path,SORT_DIR,SORT_TYPE,SORT_REG,SET_IMG_DIR);
    }
    void upRootDir(){
        if(rootItem.file.getParent()!=null){
            ini(rootItem.file.getParent());
        }
    }
    void refreshTree(){//обновляет дерево, но не возвращает его в развёрнутый вид
        MultipleSelectionModel<TreeItem<String>> selectionModel=stringTree.getSelectionModel();
        FocusModel<TreeItem<String>> focusModel=stringTree.getFocusModel();
        ini(stringTree.getRoot().getValue());
        stringTree.setSelectionModel(selectionModel);
        stringTree.setFocusModel(focusModel);
    }
    void setRoot(){
        rootDir=dirChooser.showDialog(stage);
        if(rootDir!=null){
            if(saveLastRootDir)dirChooser.setInitialDirectory(rootDir);
            ini(rootDir.getAbsolutePath());
        }
    }
    void renameItem(){
        TextInputDialog dialog = new TextInputDialog (((StringTreeItem)selectedItem).getValue());
        dialog.setTitle(strings.RUcmRename);
        //dialog.setHeaderText(Strings.RUcmDialogRename);
        dialog.setHeaderText(((StringTreeItem) selectedItem).file.getAbsolutePath());
        dialog.setContentText(strings.RUcmDialogRename);
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            String newName=name;
            String p=((StringTreeItem) selectedItem).file.getAbsolutePath();
            String oldName=((StringTreeItem) selectedItem).file.getName();
            p=p.substring(0,p.length()-oldName.length());
            boolean renamed=((StringTreeItem) selectedItem).file.renameTo(
                    new File(p+newName)
            );
            ((StringTreeItem) selectedItem).file=new File(p+newName);
            if(renamed)(selectedItem).setValue(name);
        });
    }
    public int language=0;
    //---------------------------------------------------------------------
    void deleteItemRecursive(StringTreeItem item){
        boolean done=item.file.delete();
        if(!done){
            int size=item.getChildren().size();
            for(int i=0;i<size;i++){
                deleteItemRecursive((StringTreeItem)item.getChildren().get(i));
            }
            item.file.delete();
        }
    }
    void deleteItem(){
        Alert alert=new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(strings.RUcmDelete);
        alert.setHeaderText("");
        alert.setContentText(strings.RUcmDialogDelete+"\n"+((StringTreeItem)selectedItem).file.getAbsolutePath());
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            StringTreeItem parent=(StringTreeItem)(selectedItem.getParent());
            String path=parent.file.getAbsolutePath();
            deleteItemRecursive((StringTreeItem)selectedItem);
            int cnt=selectedItem.getParent().getChildren().size();
            for(int i=0;i<cnt;i++){
                if(selectedItem.getParent().getChildren().get(i).equals(selectedItem)){
                    selectedItem.getParent().getChildren().remove(i);
                    i=cnt;
                }
            }
        }
    }
    public void ini(String path,boolean ...params){
        readyToRefresh=false;
        chooseLanguage(language);
        File temp=new File(path);
        if(rootItem!=null && !temp.exists())return;
        staticIniContMenu();
        iniContMenu();
        //---------------------------------
        rootTextField.setEditable(true);
        rootTextField.setText(path);
        rootTextField.setOnKeyPressed(event -> {
            if(event.getCode()== KeyCode.ENTER){
                ini(rootTextField.getText());
            }
        });
        rootupBtn.setOnMouseClicked(event -> upRootDir());
        rootupBtn.setPrefHeight(baseItemHeight);rootupBtn.setPrefWidth(baseItemHeight);
        ImageView imgUproot=new ImageView("img/uproot.png");
        imgUproot.setFitWidth(baseItemHeight);imgUproot.setFitHeight(baseItemHeight);
        rootupBtn.setGraphic(imgUproot);
        //---------------------------------
        firstClick=System.currentTimeMillis();
        doubleClicked=false;
        if(params.length==0)iniTree(path);
        else iniTree(path,params[0],params[1],params[2],params[3]);
        stringTree.setShowRoot(false);
        rootItem.setExpanded(true);
        stringTree.setRoot(rootItem);
        ///rootItem.getChildren().add(0,new TreeItem<>("..."));
        stringTree.addEventHandler(boubleClickEvent, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Node node=event.getPickResult().getIntersectedNode();
                if((node instanceof Text || (node instanceof TreeCell && ((TreeCell) node).getText() != null))
                //if(stringTree.getSelectionModel().getSelectedItem()!=null
                        && stringTree.contains(event.getX(),event.getY())
                        ){
                    Long temp=System.currentTimeMillis();
                    long diff=temp-firstClick;
                    if(diff<timeOfDoubleClick && diff!=0)doubleClicked=true;
                    firstClick=temp;
                    selectedItem=stringTree.getSelectionModel().getSelectedItem();
                    selectedItemInt=stringTree.getRow(selectedItem);
                    if(doubleClicked){
                        doubleClicked=false;
                        if(selectedItem!=null) {
                            //if (selectedItem.getValue() == "...") upRootDir();
                            /*if (selectedItem.getValue() == "...") {
                                upRootDir();
                            }
                            else {
                                //action for douuble click
                            }*/
                            doubleClickProc();
                        }
                    }
                }
            }
        });
        readyToRefresh=true;
    }
    void doubleClickProc(){}
    void chooseLanguage(int lang){
        language=lang;
        if(language==0){
            LangRU ru=new LangRU();
            strings.RUcmFileBrowser=ru.RUcmFileBrowser;
            strings.RUcmChooseRoot=ru.RUcmChooseRoot;
            strings.RUcmRefresh=ru.RUcmRefresh;
            strings.RUcmRename=ru.RUcmRename;
            strings.RUcmDialogRename=ru.RUcmDialogRename;
            strings.RUcmDelete=ru.RUcmDelete;
            strings.RUcmDialogDelete=ru.RUcmDialogDelete;
            strings.RUcmCopy=ru.RUcmCopy;
            strings.RUcmPaste=ru.RUcmPaste;
            strings.RUerCopyFile=ru.RUerCopyFile;
            strings.RUerCopyDir=ru.RUerCopyDir;
            strings.RUerFrom=ru.RUerFrom;
            strings.RUerTo=ru.RUerTo;
            strings.RUdiChange=ru.RUdiChange;
            strings.RUdiAlreadyExists=ru.RUdiAlreadyExists;
            strings.RUdiCopyAs=ru.RUdiCopyAs;
            strings.RUdiCopy=ru.RUdiCopy;
            strings.RUdiEr0=ru.RUdiEr0;
        }
        else{
            LangEN ru=new LangEN();
            strings.RUcmFileBrowser=ru.RUcmFileBrowser;
            strings.RUcmChooseRoot=ru.RUcmChooseRoot;
            strings.RUcmRefresh=ru.RUcmRefresh;
            strings.RUcmRename=ru.RUcmRename;
            strings.RUcmDialogRename=ru.RUcmDialogRename;
            strings.RUcmDelete=ru.RUcmDelete;
            strings.RUcmDialogDelete=ru.RUcmDialogDelete;
            strings.RUcmCopy=ru.RUcmCopy;
            strings.RUcmPaste=ru.RUcmPaste;
            strings.RUerCopyFile=ru.RUerCopyFile;
            strings.RUerCopyDir=ru.RUerCopyDir;
            strings.RUerFrom=ru.RUerFrom;
            strings.RUerTo=ru.RUerTo;
            strings.RUdiChange=ru.RUdiChange;
            strings.RUdiAlreadyExists=ru.RUdiAlreadyExists;
            strings.RUdiCopyAs=ru.RUdiCopyAs;
            strings.RUdiCopy=ru.RUdiCopy;
            strings.RUdiEr0=ru.RUdiEr0;
        }
        if(readyToRefresh)fullRefreshTree();
    }
    //=========================================================================
    //unuused? but checked funcs
    void checkDeletedItems(StringTreeItem item){
        if(item.file!=null & item.file.isDirectory()){
            int cnt=item.getChildren().size();
            for(int i=0;i<cnt;i++){
                if(!(i==0 & item.getChildren().get(i).getValue().toString().compareTo("...")==0)) {
                    if (!((StringTreeItem) item.getChildren().get(i)).file.exists()){
                        item.getChildren().remove(i);cnt--;
                    }
                    else if (((StringTreeItem) item.getChildren().get(i)).file.isDirectory() &&
                            !((StringTreeItem)(item.getChildren().get(i))).isFirstTimeChildren)
                        checkDeletedItems((StringTreeItem) item.getChildren().get(i));
                }
            }
        }
    }
}
