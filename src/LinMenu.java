import javafx.scene.control.*;
import java.io.FileNotFoundException;

public class LinMenu extends ContextMenu {

    //use context menu to implement function with 'start' 'settings' in settings there is audio setting and etc
    private static LinMenu insatnce = null;
    MenuBar menuBar = new MenuBar();

    private LinMenu() throws FileNotFoundException {
        Menu start_m = new Menu("Start");
        Menu settings_m = new Menu("Settings");
        MenuItem startItem = new MenuItem("new game");
        MenuItem loadItem = new MenuItem("load game");
        MenuItem audioItem = new MenuItem("audio");
        Menu exit_m = new Menu("Exit");
        CustomMenuItem audio = new CustomMenuItem(new Slider());
        audio.setHideOnClick(false);

        start_m.getItems().addAll(startItem,loadItem);
        settings_m.getItems().addAll(audioItem,audio);


        menuBar.getMenus().addAll(start_m,settings_m,exit_m);


    }

    public static LinMenu getInstance() throws FileNotFoundException {
        //build instance
        if(insatnce == null){
            insatnce = new LinMenu();
        }
        return insatnce;
    }

    public MenuBar getBar() throws FileNotFoundException {
       if(menuBar == null){
           System.err.println("menuBar equals null");
       }
       return menuBar;
    }

}
