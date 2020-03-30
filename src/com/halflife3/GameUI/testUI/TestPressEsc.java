package com.halflife3.GameUI.testUI;

import javafx.application.Application;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.lang.Thread;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import java.awt.*;

public class TestPressEsc extends Application {
    private boolean state = false;

    @Override
    public void start(Stage stage) throws Exception {
        Rectangle rectangle = new Rectangle(50, 50, 50, 50);
        rectangle.setFill(Color.CORAL);
        Thread escThread = new EscThread();
    }

    public static void main(String[] args) {

    }


    public static class EscThread extends Thread{
        @Override
        public void run(){

        }
    }
}
