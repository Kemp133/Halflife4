package com.halflife3.GameUI;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class LinMenu extends ContextMenu {

    //use context menu to implement function with 'start' 'settings' in settings there is audio setting and etc
    private static LinMenu insatnce = null;
    private VBox main_manu = null;
    private File audio_path = new File("res/MenuMusic.mp3");
    private Media media = new Media(audio_path.toURI().toString());
    private MediaPlayer player = new MediaPlayer(media);        //only set for backgroud music
    private Slider volume = new Slider();
    private MenuItem startItem = null;
    private MenuItem loadItem = null;
    private Button login = null;
    private Text text = new Text("Game_Name");

    public LinMenu() throws FileNotFoundException {
        FileInputStream input1 = new FileInputStream("res/join_game.png");
        FileInputStream input2 = new FileInputStream("res/Audio.png");
        FileInputStream input3 = new FileInputStream("res/exit.png");
        Image image1 = new Image(input1);
        Image image2 = new Image(input2);
        Image image3 = new Image(input3);

        startItem = new MenuItem("new game");
        loadItem = new MenuItem("load game");
        MenuItem audioItem_on = new MenuItem("audio on");
        MenuItem audioitem_off = new MenuItem("audio off");
        CustomMenuItem audio = new CustomMenuItem(volume);
        audio.setHideOnClick(false);
        Button start_m = new Button("",new ImageView(image1));
        MenuButton settings_m = new MenuButton("",new ImageView(image2),audioItem_on,audioitem_off,audio);
        Button exit_m = new Button("",new ImageView(image3));
        text.setFont(Font.font("Corbel",100));
        text.setFill(Color.WHITE);

        player.setAutoPlay(true);
        startItem.setOnAction(e->{
            if(player.isMute())
                player.play();
        });

        loadItem.setOnAction(e->{

        });

        audioItem_on.setOnAction(actionEvent -> Is_mute(false));
        audioitem_off.setOnAction(actionEvent -> Is_mute(true));
        audio.setOnAction(actionEvent -> volumeControl(volume));
        exit_m.setOnAction(actionEvent -> Platform.exit());
        main_manu = new VBox(30);
        main_manu.getChildren().addAll(text,start_m, settings_m, exit_m);

    }


    public MenuItem getStartItem() {
        return startItem;
    }

    public MenuItem getLoadItem(){
        return loadItem;
    }

    private void Is_mute(boolean b) {
        if(b){
            player.stop();
        }else{
            player.play();
        }
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

    public Button getLogin() {
        return login;
    }

    public void setLogin(Button login) {
        this.login = login;
    }
}
