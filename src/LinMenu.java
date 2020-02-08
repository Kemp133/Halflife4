import javafx.scene.control.*;

public class LinMenu extends ContextMenu {

    //use context menu to implement function with 'start' 'settings' in settings there is audio setting and etc
    private static LinMenu insatnce = null;

    private LinMenu(){
        MenuItem startItem = new MenuItem("start");
        MenuItem audioItem = new MenuItem("audio");
        MenuItem exitItem = new MenuItem("exit game");
        CustomMenuItem audio = new CustomMenuItem(new Slider());
        audio.setHideOnClick(false);




        getItems().add(startItem);
        getItems().add(audioItem);
        //getItems().add(new SeparatorMenuItem());
        getItems().add(audio);
        //getItems().add(new SeparatorMenuItem());
        getItems().add(exitItem);

    }

    public static LinMenu getInstance(){
        //build instance
        if(insatnce == null){
            insatnce = new LinMenu();
        }
        return insatnce;
    }
}
