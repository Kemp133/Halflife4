package GameUI;

import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Slider;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;

public class AudioForGame{
    private MenuBar menuBar = new MenuBar();
    private File filename1 = (new File("res/bensound-summer.mp3"));
    private Media media1 = new Media(filename1.toURI().toString());
    private MediaPlayer battle_music = new MediaPlayer(media1);
    private Menu menu = new Menu("Audio Settings");
    private CustomMenuItem slider1 = new CustomMenuItem(new Slider());

    private void volumeControl(Slider volume) {
        battle_music.volumeProperty().bind(volume.valueProperty().divide(100));
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

}
