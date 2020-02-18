package GameUI;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.Timer;

public class Lin_Controller implements EventHandler<KeyEvent> {
    private Timer timer;
    private boolean isPause = false;


    @Override
    public void handle(KeyEvent keyEvent) {
        KeyCode keyCode = keyEvent.getCode();
        if(keyCode == KeyCode.ESCAPE){
            Platform.exit();
        }else{
            System.err.println("Not implemented yet,Please press esc/ to leave");
        }
    }
}
