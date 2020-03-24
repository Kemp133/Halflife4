package com.halflife3.GameUI;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
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
        volume = new Slider();

        Media media = new Media(audio_path.toURI().toString());
        player = new MediaPlayer(media);
        audioItem_on = new MenuItem("audio on");
        audioitem_off = new MenuItem("audio off");
        audio = new CustomMenuItem(volume);
        audio.setHideOnClick(false);

        //Setting the button properties and images for the play button
        FileInputStream inputJoin = new FileInputStream("res/joingamebuttongimp.png");
        Image imageJoin = new Image(inputJoin);
        start_m = new Button();
        start_m.setMinHeight(90);
        start_m.setMinWidth(140);
        BackgroundImage bImageJoin = new BackgroundImage(imageJoin, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                new BackgroundSize(start_m.getWidth(), start_m.getHeight(), true, true, true, false));
        Background newBJoin = new Background(bImageJoin);
        start_m.setBackground(newBJoin);

        //Setting the button properties and images for the audio button
        FileInputStream inputAudio = new FileInputStream("res/audiogamebuttongimp.png");
        Image imageAudio = new Image(inputAudio);
        settings_m = new MenuButton("",new ImageView(),audioItem_on,audioitem_off,audio);
        settings_m.setMinHeight(90);
        settings_m.setMinWidth(140);
        BackgroundImage bImageAudio = new BackgroundImage(imageAudio, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                new BackgroundSize(settings_m.getWidth(), settings_m.getHeight(), true, true, true, false));
        Background newBAudio = new Background(bImageAudio);
        settings_m.setBackground(newBAudio);

        //Setting the button properties and images for the exit button
        FileInputStream inputExit = new FileInputStream("res/exitgamebuttongimp.png");
        Image imageExit = new Image(inputExit);
        exit_m = new Button();
        exit_m.setMinHeight(90);
        exit_m.setMinWidth(140);
        BackgroundImage bImageExit = new BackgroundImage(imageExit, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                new BackgroundSize(exit_m.getWidth(), exit_m.getHeight(), true, true, true, false));
        Background newBExit = new Background(bImageExit);
        exit_m.setBackground(newBExit);

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
        //main_manu.getChildren().addAll(start_m, exit_m);
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
