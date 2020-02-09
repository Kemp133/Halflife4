import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collection;

public class LinMenu extends ContextMenu {

    //use context menu to implement function with 'start' 'settings' in settings there is audio setting and etc
    private static LinMenu insatnce = null;
    private VBox main_manu = null;
    private File audio_path = new File("res/bensound-summer.mp3");
    private Media media = new Media(audio_path.toURI().toString());
    private MediaPlayer player = new MediaPlayer(media);
    private Slider volume = new Slider();

    private LinMenu() throws FileNotFoundException {
        FileInputStream input = new FileInputStream("res/button_image.png");
        Image image = new Image(input);
        ImageView imageview = new ImageView(image);

        MenuItem startItem = new MenuItem("new game");
        MenuItem loadItem = new MenuItem("load game");
        MenuItem audioItem = new MenuItem("audio");
        CustomMenuItem audio = new CustomMenuItem(volume);
        audio.setHideOnClick(false);
        MenuButton start_m = new MenuButton("Start",imageview,startItem,loadItem);
        MenuButton settings_m = new MenuButton("Settings",imageview,audioItem,audio);
        Button exit_m = new Button("Exit",imageview);

        //adding mouse input
        startItem.setOnAction(e->{
            player.play();
            audio.setOnAction(actionEvent -> volumeControl(volume));
        });
        exit_m.setOnAction(actionEvent -> Platform.exit());


        main_manu = new VBox(100);
        main_manu.getChildren().addAll(start_m, settings_m, exit_m);

    }

    private void volumeControl(Slider volume) {
       player.volumeProperty().bind(volume.valueProperty().divide(100));
    }

    public static LinMenu getInstance() throws FileNotFoundException {
        //build instance
        if(insatnce == null){
            insatnce = new LinMenu();
        }
        return insatnce;
    }

    public VBox getBar() throws FileNotFoundException {
       if(main_manu == null){
           System.err.println("menuBar equals null");
       }
       return main_manu;
    }


}
