package com.halflife3.GameUI;

import com.halflife3.GameUI.interfaces.ICredentialUser;
import com.halflife3.DatabaseUI.Login;
import com.halflife3.Model.ApplicationUser;
import com.halflife3.Networking.Server.Server;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;

import static javafx.scene.paint.Color.WHITE;

//The menu set for choose server and client
public class FirstMenu extends Application implements ICredentialUser {
    private Stage pstage = null;
    private static final double SCREEN_WIDTH = Screen.getPrimary().getBounds().getWidth();
    private static final double SCREEN_HEIGHT = Screen.getPrimary().getBounds().getHeight();
    StackPane root = new StackPane();
    private Button client_m;
    private Button server_m;

    private static ApplicationUser user;
    private static WindowAttributes windowAttributes;

    private boolean isMaximised;

    public FirstMenu() throws FileNotFoundException {
        FileInputStream input = new FileInputStream("res/button_image.png");
        javafx.scene.image.Image image = new Image(input);

        client_m = new Button("Client",new ImageView(image));
        server_m = new Button("Server",new ImageView(image));

        server_m.setOnAction(e->{
            //jump to server main class
            try {
                runServer();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        client_m.setOnAction(e->{
            //jump to run client class
            //load the login page
            try {
                new Windows().start(pstage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void runServer() throws Exception {
        new ServerDisplay().start(pstage);
        pstage.centerOnScreen();
        Server myServer = new Server();
        myServer.start();
    }

    public static void main(String[] args) {
        System.setProperty("javafx.preloader", Login.class.getName());
        loadWindowAttributes();
        Application.launch(args);
    }

    private static void loadWindowAttributes() {
        try(FileInputStream fi = new FileInputStream(new File("AppData/config.conf"))) {
            try(ObjectInputStream oi = new ObjectInputStream(fi)) {
                windowAttributes = (WindowAttributes)oi.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            try(FileOutputStream fo = new FileOutputStream(new File("AppData/config.conf"))){
                try(ObjectOutputStream os = new ObjectOutputStream(fo)) {
                    WindowAttributes genericAttributes = new WindowAttributes();
                    genericAttributes.title = "Main Menu";
                    genericAttributes.resizeable = false;
                    genericAttributes.maximisedOnLoad = true;
                    genericAttributes.fullScreenOnLoad = true;
                    genericAttributes.isModal = false;
                    genericAttributes.decorated = false;
//                    genericAttributes.iconPath = "res/halflife3.bmp";

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
        if(windowAttributes.maximisedOnLoad) s.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

//        if(!windowAttributes.iconPath.isEmpty() || !windowAttributes.iconPath.isBlank()) {
//            try(FileInputStream fi = new FileInputStream(windowAttributes.iconPath)) {
//                s.getIcons().add(new Image(fi));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }

    @Override
    public void start(Stage stage) {
        this.setPstage(stage);
        setMainStage(stage);
        stage.setScene(new Scene(createContent(), SCREEN_WIDTH, SCREEN_HEIGHT, WHITE));
        mayBeShown();
    }

    public StackPane createContent() {
        VBox pane = new VBox(100);
        pane.getChildren().addAll(server_m, client_m);
        pane.setAlignment(Pos.CENTER);
        root.getChildren().add(pane);
        return root;
    }

    public Stage getPstage() {
        return pstage;
    }

    public void setPstage(Stage pstage) {
        this.pstage = pstage;
    }

    @Override
    public void setApplicationUser(String username) {
        user = new ApplicationUser();
        user.username = username;
        user.isValidSession = true;
        mayBeShown();
    }

    public void mayBeShown() {
        if (user != null && pstage != null)
            Platform.runLater(() -> pstage.show());
    }
}