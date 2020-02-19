package GameUI;

import javafx.scene.control.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import javax.swing.*;
import java.io.File;

public class AudioForGame {
    private MenuBar menuBar = new MenuBar();
    private File filename1 = (new File("res/2019-04-18_-_The_Epic_Boss_Fight_-_David_Fesliyan.mp3"));
    private File filename2 = new File("res/9_mm_gunshot-mike-koenig-123.mp3");
    private Media media1 = new Media(filename1.toURI().toString());
    private Media media2 = new Media(filename2.toURI().toString());
    private MediaPlayer battle_music = new MediaPlayer(media1);
    private MediaPlayer bullet_music = new MediaPlayer(media2);
    private Menu menu = new Menu("Audio Settings");
    private MenuItem mute = new MenuItem("on/off");
    private Slider volume = new Slider();
    private CustomMenuItem slider1 = new CustomMenuItem(volume);


    public void volumeControl(Slider volume) {
        battle_music.volumeProperty().bind(volume.valueProperty().divide(100));
    }

    public void swtichMute(){
        if(battle_music.isMute()){
            battle_music.play();
            battle_music.setMute(false);
        }
        else{
            battle_music.stop();
            battle_music.setMute(true);
        }
    }

    public MenuBar getMenuBar() {
        return menuBar;
    }

    public void setMenuBar(MenuBar menuBar) {
        this.menuBar = menuBar;
    }

    public MediaPlayer getBattle_music() {
        return battle_music;
    }

    public void setBattle_music(MediaPlayer battle_music) {
        this.battle_music = battle_music;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    public CustomMenuItem getSlider1() {
        return slider1;
    }

    public void setSlider1(CustomMenuItem slider1) {
        this.slider1 = slider1;
    }

    public Slider getVolume() {
        return volume;
    }

    public void setVolume(Slider volume) {
        this.volume = volume;
    }

    public MenuItem getMute() {
        return mute;
    }

    public void setMute(MenuItem mute) {
        this.mute = mute;
    }

    public MediaPlayer getBullet_music() {
        return bullet_music;
    }

    public void setBullet_music(MediaPlayer bullet_music) {
        this.bullet_music = bullet_music;
    }
}
