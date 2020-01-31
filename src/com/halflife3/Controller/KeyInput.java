package com.halflife3.Controller;
import com.halflife3.Model.GameObject;


import com.halflife3.Model.player;
import javafx.fxml.FXML;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.application.Platform;
import java.util.Timer;
import java.util.TimerTask;

public class KeyInput implements EventHandler<KeyEvent> {

    private Timer timer;
    private boolean paused;

    public KeyInput() {
        this.paused = false;
    }
    private void startTimer() {
        this.timer = new java.util.Timer();
        TimerTask timerTask = new TimerTask() {
            public void run() {
                Platform.runLater(new Runnable() {
                    public void run() {
                        update(pacManModel.getCurrentDirection());
                    }
                });
            }
        };

        long frameTimeInMilliseconds = (long)(1000.0 / FRAMES_PER_SECOND);
        this.timer.schedule(timerTask, 0, frameTimeInMilliseconds);
    }
    /**
     * Takes in user keyboard input to control the movement of PacMan and start new games
     * @param keyEvent user's key click
     */
    @Override
    public void handle(KeyEvent keyEvent) {
        
    }

    /**
     * Pause the timer
     */
    public void pause() {
        this.timer.cancel();
        this.paused = true;
    }


    public boolean getPaused() {
        return paused;
    }
}

