package com.halflife3;

import com.sun.javafx.application.LauncherImpl;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;

public class MainApplication extends Application {

    private static WindowAttributes windowAttributes;
    private double SCREEN_WIDTH = Screen.getPrimary().getBounds().getWidth() * 0.8;
    private double SCREEN_HEIGHT = Screen.getPrimary().getBounds().getHeight() * 0.8;

    private boolean isMaximised;

    @Override
    public void init() {
        //Do any heavy lifting that's required
    }

    public static void main(String[] args) {
        System.setProperty("javafx.preloader", Login.class.getName());
        loadWindowAttributes();
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        setMainStage(stage);
        VBox vbox = new VBox();
        Scene scene = new Scene(vbox);
        stage.setScene(scene);
        stage.show();
    }

    public static void loadWindowAttributes() {
        try(FileInputStream fi = new FileInputStream(new File("config.conf"))) {
            try(ObjectInputStream oi = new ObjectInputStream(fi)) {
                windowAttributes = (WindowAttributes)oi.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            try(FileOutputStream fo = new FileOutputStream(new File("config.conf"))){
                try(ObjectOutputStream os = new ObjectOutputStream(fo)) {
                    WindowAttributes genericAttributes = new WindowAttributes();
                    genericAttributes.title = "Team HalfLife";
                    genericAttributes.resizeable = false;
                    genericAttributes.maximisedOnLoad = true;
                    genericAttributes.fullScreenOnLoad = true;
                    genericAttributes.isModal = false;
                    genericAttributes.decorated = false;
                    genericAttributes.iconPath = "res/halflife3.bmp";

                    os.writeObject(genericAttributes);
                    windowAttributes = genericAttributes;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setMainStage(Stage s) {
        s.setTitle(windowAttributes.title);
        s.setResizable(windowAttributes.resizeable);
        s.setMaximized(windowAttributes.maximisedOnLoad);
        s.setFullScreen(windowAttributes.fullScreenOnLoad);

        if(!windowAttributes.decorated) s.initStyle(StageStyle.UNIFIED);
        if(windowAttributes.isModal) s.initModality(Modality.APPLICATION_MODAL);

        if(!windowAttributes.iconPath.isEmpty() || !windowAttributes.iconPath.isBlank()) {
            try(FileInputStream fi = new FileInputStream(windowAttributes.iconPath)) {
                s.getIcons().add(new Image(fi));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
