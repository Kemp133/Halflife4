package com.halflife3.DatabaseUI;

import com.halflife3.Controller.BaseController;
import com.halflife3.Controller.ClientController;
import com.halflife3.Controller.Exceptions.SceneStackEmptyException;
import com.halflife3.Controller.SceneManager;
import com.halflife3.GameUI.Maps;
import com.halflife3.Networking.Server.Server;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.*;

public class SettingsMenu {
    private     BorderPane  borderPane;
    private     VBox        vboxSettings;
    private     VBox        vboxDeleteAccount;


    /*
    Set-up for core settings page
     */
    private     Button          deleteRememberMe    = new Button("Delete auto-login");
    private     Button          removeAcc           = new Button("Delete Account");
    private     Button          back                = new Button("Back");

    /*
    Set-up for scene to allow user to delete account
     */
    private     Text            confirmDeletion;
    private     PasswordField   password;
    private     PasswordField   confPassword;
    private     Button          confDeleteAcc       = new Button("Delete Account");
    private     Button          back2               = new Button("Back");
    private     Text            userFeedback;

    private     Scene           scene;

    private SceneManager manager = SceneManager.getInstance();

    public void start() {
        initialiseMenuScene(settingsVBox());
        manager.setScene("Settings", getScene());
    }

    private Background addBackground() {
        try {

            FileInputStream inputStream = new FileInputStream("res/Leaderboard/LeaderboardBackground.jpg");
            Image image = new Image(inputStream);

            BackgroundSize backgroundSize = new BackgroundSize(800, 600, false, false, false, true);
            BackgroundImage backgroundImage = new BackgroundImage(image, BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize);
            Background background = new Background(backgroundImage);
            return background;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return null;
    }

    private VBox settingsVBox() {

        deleteRememberMe.setOnAction(actionEvent -> {
            if (deleteLoginConf()) {
                userFeedback.setText("Auto-login removed");
            } else {
                userFeedback.setText("Remember me has not been used");
            }
            userFeedback.setVisible(true);
        });

        removeAcc.setOnAction(actionEvent -> {
            //TODO: Handle scene change to delete acc scene
            initialiseMenuScene(removeAccVBox());
            manager.setScene("Settings Delete Account", getScene());
        });

        back.setOnAction(actionEvent -> {
            //TODO: Handle scene change to main menu
            try {
                manager.restorePreviousScene();
            } catch (SceneStackEmptyException e) {
                e.printStackTrace();
            }
        });

        userFeedback.setVisible(false);
        userFeedback.setFill(Color.RED);
        vboxSettings = new VBox(deleteRememberMe, removeAcc, back, userFeedback);
        vboxSettings.setAlignment(Pos.BASELINE_CENTER);
        vboxSettings.setPadding(new Insets(35, 0, 0, 30));
        vboxSettings.setSpacing(30);
        vboxSettings.setStyle("-fx-background-color: rgba(176,224,230,0.8);");

        return vboxSettings;
    }

    private VBox removeAccVBox() {

        confDeleteAcc.setOnAction(actionEvent -> {
            if (!password.equals(confPassword)) {
                userFeedback.setText("Passwords do not match");
                setNullFields();
            } else {
                if (confirmUser(getConnection(), BaseController.GetApplicationUser().username, password.getText())) {
                    deleteUserDetails(getConnection(), BaseController.GetApplicationUser().username);
                    userFeedback.setText("Your account has been deleted");
                    //TODO: Change 'back' on action to go to login page instead of settings
                } else {
                    userFeedback.setText("Password is incorrect");
                    setNullFields();
                }
            }
            userFeedback.setVisible(true);
        });

        back.setOnAction(actionEvent -> {
            //TODO: Add code to go back to settings
            try {
                manager.restorePreviousScene();
            } catch (SceneStackEmptyException e) {
                e.printStackTrace();
            }
        });

        userFeedback.setVisible(false);
        userFeedback.setFill(Color.RED);
        vboxDeleteAccount = new VBox(confirmDeletion, password, confPassword, confDeleteAcc, back2, userFeedback);
        vboxDeleteAccount.setAlignment(Pos.BASELINE_CENTER);
        vboxDeleteAccount.setPadding(new Insets(35, 0, 0, 30));
        vboxDeleteAccount.setSpacing(30);
        vboxDeleteAccount.setStyle("-fx-background-color: rgba(176,224,230,0.8);");

        return vboxDeleteAccount;
    }

    private BorderPane borderPane(VBox scene) {
        borderPane = new BorderPane();

        Font paladinFont = null;
        try {
            paladinFont = Font.loadFont(new FileInputStream(new File("res/Font/PaladinsSemiItalic.otf")), 40);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Label title = new Label("Space Ball");
        title.setFont(paladinFont);
        title.setStyle("-fx-text-fill: linear-gradient(#0000FF, #FFFFFF 95%);");

        Insets insets = new Insets(30);
        Pane topPane = new Pane();
        topPane.setMinSize(500, 100);
        Pane leftPane = new Pane();
        leftPane.setMinSize(100, 400);
        Pane rightPane = new Pane();
        rightPane.setMinSize(100, 400);
        Pane bottomPane = new Pane();
        bottomPane.setMinSize(500, 100);
        borderPane.setTop(title);
        BorderPane.setMargin(title, insets);
        borderPane.setAlignment(title, Pos.TOP_CENTER);

        borderPane.setLeft(leftPane);
        borderPane.setRight(rightPane);
        borderPane.setBottom(bottomPane);

        borderPane.setBackground(addBackground());
        borderPane.setCenter(scene);

        File f = new File("res/MainMenu/MainMenuCSS.css");
        borderPane.getStylesheets().add("file:///" + f.getAbsolutePath().replace("\\", "/"));

        return borderPane;
    }

    public static Connection getConnection () {
        Connection c = null;
        try {
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://rogue.db.elephantsql.com:5432/nuzmlzpr";
            url = url.trim();
            c   = DriverManager.getConnection(url, "nuzmlzpr", "pd7OdC_3BiVrAPNU68CETtFtBaqFxJFB");

            if (c != null) {
                System.out.println("Connection complete");
            } else {
                System.out.println("Connection failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return c;
    }

    public void closeConnections (Connection c, PreparedStatement p, ResultSet r) {
        if (c != null) {
            try {
                c.close();
            } catch (SQLException e) { /* ignored */}
        }
        if (p != null) {
            try {
                p.close();
            } catch (SQLException e) { /* ignored */}
        }
        if (r != null) {
            try {
                r.close();
            } catch (SQLException e) { /* ignored */}
        }
    }

    private boolean confirmUser (Connection c, String username, String passwordEntered) {

        boolean returnValue      = false;
        String  saltCheck        = "";
        String  securedPassword  = "";
        byte[]  securedPassword2 = new byte[0];

        PreparedStatement passwordStatement     = null;
        ResultSet         rs                = null;
        try {
            //Creating the query
            String passwordQuery = "SELECT * FROM userdatascore WHERE \"name\" = '" + username + "'";
            //Creating the statement
            passwordStatement = c.prepareStatement(passwordQuery);
            //Executing the query
            rs = passwordStatement.executeQuery();
            while (rs.next()) {
                securedPassword2 = rs.getBytes("password");
            }
            securedPassword = new String(securedPassword2);

            //Code to check password
            System.out.println("Password entered: " + passwordEntered);
            System.out.println("SecuredPassword: " + securedPassword);
            return returnValue = securedPassword.equals(passwordEntered);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnections(c, passwordStatement, rs);
        }
        return false;
    }

    private void deleteUserDetails (Connection c, String username) {
        PreparedStatement deleteStatement     = null;
        try {
            String deleteQuery = "DELETE FROM userdatascore WHERE \"name\" = '" + username + "'";
            deleteStatement = c.prepareStatement(deleteQuery);
            deleteStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnections(c, deleteStatement, null);
        }
    }

    private boolean deleteLoginConf () {
        try
        {
            File f= new File("AppData/login.conf");
            if(f.delete()) {
                System.out.println(f.getName() + " deleted");
                return true;
            } else {
                System.out.println("failed");
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    //Sets text fields to null
    public void setNullFields () {
        password.setText(null);
        confPassword.setText(null);
    }

    private void initialiseMenuScene(VBox sceneName) {
        scene = new Scene(borderPane(sceneName), 800, 600);
    }

    public Scene getScene()        { return scene; }
}