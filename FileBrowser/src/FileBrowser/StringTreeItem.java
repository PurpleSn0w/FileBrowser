package FileBrowser;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

import java.io.File;
import java.util.Comparator;

/**
 * Created by uteus on 20.09.2017.
 */
public class StringTreeItem extends TreeItem<String> {
    //sort vars
    boolean sortDir=true;
    boolean sortType=true;
    boolean sortReg=false;
    boolean setImgDir=true;
    //---------------------------------------------------------------------
    static double baseTextWidth;
    static double baseItemHeight;
    static void calcIni(){
        Text t=new Text("123");
        baseTextWidth=t.getStrokeWidth();
        baseItemHeight =baseTextWidth*16;
    }
    //---------------------------------------------------------------------
    public File file;
    //---------------------------------------------------------------------
    boolean isFirstTimeChildren=true;
    private boolean isFirstTimeLeaf=true;
    private boolean isLeaf;
    void setImgDir(TreeItem<String> item){
        //item.setGraphic(new Text("DIR"));
        ImageView imgDir=new ImageView("img/treeDirCollapsed.png");
        imgDir.setFitHeight(baseItemHeight);
        imgDir.setFitWidth(baseItemHeight);
        item.setGraphic(imgDir);
    }
    File getFile(TreeItem<String> i){
        //System.err.println("getFile: "+i.getValue()+"   "+String.valueOf(i.getParent()!=null));
        String s = i.getValue();                //System.err.print("s="+s+"   ");
        String fullPath=s;
        String slash=s.contains("/")?"/":"\\";  //System.err.println("slash="+slash+"   ");
        while(i.getParent()!=null){
            i=i.getParent();                    //System.err.print("parent: "+i+"   ");
            String temp=slash;if(i.getValue().endsWith(slash) || fullPath.startsWith(slash))temp="";
            fullPath=i.getValue()+temp+fullPath;//System.err.println("fullPath: "+fullPath);
        }                                       //System.err.println("END getFile");
        return new File(fullPath);
    }
    String getExtention(String s){
        int index=0;
        if(s.contains(".") && this.file.isFile())index=s.lastIndexOf(".");
        else return "";
        return s.substring(index);
    }
    Comparator<TreeItem> comparator =(TreeItem i0, TreeItem i1)->{
        String s0=String.valueOf(i0);String s1=String.valueOf(i1);
        if(!sortReg){s0=s0.toLowerCase();s1=s1.toLowerCase();}
        return (s0.compareTo(s1))+
                (sortDir?((i0.isLeaf()?100000000:0)-(i1.isLeaf()?100000000:0)):0)+
                (sortType?((getExtention(s0).compareTo(getExtention(s1)))*1000000):0);
    };
    StringTreeItem(String s, boolean SORT_DIR, boolean SORT_TYPE, boolean SORT_REG, boolean SET_IMG_DIR) {
        super(s);
        calcIni();
        //file=getFile(this);
        //System.err.println("super: "+this.getValue()+"   "+String.valueOf(this.getParent()!=null));
        this.sortDir=SORT_DIR;
        this.sortType=SORT_TYPE;
        this.sortReg=SORT_REG;
        this.setImgDir=SET_IMG_DIR;
    }
    @Override
    public ObservableList<TreeItem<String>> getChildren() {
        if (isFirstTimeChildren) {
            isFirstTimeChildren = false;
            super.getChildren().setAll(buildChildren(this));
            super.getChildren().sort(comparator);
            int cnt=this.getChildren().size();
            if(cnt>0){
                for(int i=0;i<cnt;i++){
                    ((StringTreeItem)(this.getChildren().get(i))).file=getFile(this.getChildren().get(i));
                }
            }
        }
        return super.getChildren();
    }
    public void reBuildChildren(){
        System.err.println(this.getValue()+"    "+this.getChildren().removeAll(this.getChildren()));
        this.getChildren().addAll(buildChildren(this));
        super.getChildren().sort(comparator);
        int cnt=this.getChildren().size();
        if(cnt>0){
            for(int i=0;i<cnt;i++){
                ((StringTreeItem)(this.getChildren().get(i))).file=getFile(this.getChildren().get(i));
            }
        }
    }
    @Override
    public boolean isLeaf() {
        if (isFirstTimeLeaf) {
            isFirstTimeLeaf = false;
            //File f = getFile(this);
            isLeaf = getFile(this).isFile();
        }
        return isLeaf;

    }
    private ObservableList<TreeItem<String>> buildChildren(TreeItem<String> TreeItem) {
        String s = TreeItem.getValue();
        file=getFile(this);
        if (s != null && file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                ObservableList<TreeItem<String>> children = FXCollections.observableArrayList();
                for (File childFile : files) {
                    children.add(new StringTreeItem(childFile.getName(),
                            this.sortDir,this.sortType,this.sortReg,this.setImgDir));
                    if(setImgDir && childFile.isDirectory()) setImgDir(children.get(children.size()-1));
                }
                return children;
            }
        }

        return FXCollections.emptyObservableList();
    }
}
