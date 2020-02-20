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
import javafx.util.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class LinMenu extends ContextMenu {

    //use context menu to implement function with 'start' 'settings' in settings there is audio setting and etc
    VBox main_manu;
    MediaPlayer player;        //only set for backgroud music
    Slider volume;
    MenuItem audioItem_on;
    MenuItem audioitem_off;
    CustomMenuItem audio;
    Button start_m;
    MenuButton settings_m;
    Button exit_m;

    public LinMenu() throws FileNotFoundException {
        File audio_path = new File("res/MenuMusic.mp3");
        FileInputStream input1 = new FileInputStream("res/joingamebuttongimp.png");
        FileInputStream input2 = new FileInputStream("res/Audio.png");
        FileInputStream input3 = new FileInputStream("res/exit.png");
        Image image1 = new Image(input1);
        Image image2 = new Image(input2);
        Image image3 = new Image(input3);
        volume = new Slider();

        Media media = new Media(audio_path.toURI().toString());
        player = new MediaPlayer(media);
        audioItem_on = new MenuItem("audio on");
        audioitem_off = new MenuItem("audio off");
        audio = new CustomMenuItem(volume);
        audio.setHideOnClick(false);
        start_m = new Button("",new ImageView(image1));
        settings_m = new MenuButton("",new ImageView(image2),audioItem_on,audioitem_off,audio);
        exit_m = new Button("",new ImageView(image3));

        player.setAutoPlay(true);
        player.setOnEndOfMedia(() -> {
            player.seek(Duration.ZERO);
            player.play();
        });
        player.setOnError(() -> System.err.println("Error in playing bgm"));
        audioItem_on.setOnAction(actionEvent -> player.setMute(false));
        audioitem_off.setOnAction(actionEvent -> player.setMute(true));
        audio.setOnAction(actionEvent -> volumeControl(volume));
        exit_m.setOnAction(actionEvent -> Platform.exit());
        main_manu = new VBox(30);
        main_manu.getChildren().addAll(start_m, settings_m, exit_m);
    }


    public Button getStartItem() {
        return start_m;
    }

   public void volumeControl(Slider volume) {
       player.volumeProperty().bind(volume.valueProperty().divide(100));
    }

    public VBox getBar() throws FileNotFoundException {
       if(main_manu == null){
           System.err.println("menuBar equals null");
       }
       return main_manu;
    }

}
