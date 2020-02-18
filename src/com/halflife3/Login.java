package com.halflife3;

import com.halflife3.Model.ApplicationUser;
import com.halflife3.Model.Interfaces.ICredentialUser;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import java.sql.*;

import java.util.Arrays;

public class Login extends Preloader {

    private static final double SCREEN_WIDTH = Screen.getPrimary().getBounds().getWidth() * 0.5;
    private static final double SCREEN_HEIGHT = Screen.getPrimary().getBounds().getHeight() * 0.5;

    private static Button login = new Button();
    private static Button createNewUser = new Button();
    private static Button backButton = new Button();
    private static Button create = new Button();
    private static Text name = new Text("Name");
    private static TextField nameField = new TextField();
    private static Text password = new Text("Password");
    private static PasswordField passwordField = new PasswordField();
    private static Text confPassword = new Text("Confirm Password");
    private static PasswordField passwordFieldConf = new PasswordField();
    private static Text incorrectFields = new Text();

    private static Stage preloaderStage;

    private boolean hasLoggedIn = false;
    private ICredentialUser user;

    private static GridPane basePane() {
        GridPane gridPaneLogin = new GridPane();
        //Setting size for the pane
        gridPaneLogin.setMinSize(800, 600);

        //Setting the padding
        gridPaneLogin.setPadding(new Insets(10, 10, 10, 10));

        //Setting the vertical and horizontal gaps between the columns
        gridPaneLogin.setVgap(30);
        gridPaneLogin.setHgap(30);

        //Setting the Grid alignment
        gridPaneLogin.setAlignment(Pos.CENTER);

        return gridPaneLogin;
    }

    public static GridPane loginPane() {
        GridPane gridPaneLogin = basePane();

        //Arranging all the nodes in the grid
        gridPaneLogin.add(name, 0, 0);
        gridPaneLogin.add(nameField, 1, 0);
        gridPaneLogin.add(password, 0, 1);
        gridPaneLogin.add(passwordField, 1, 1);
        gridPaneLogin.add(login, 0, 2);
        gridPaneLogin.add(createNewUser, 1, 2);
        gridPaneLogin.add(incorrectFields, 0, 3);

        return gridPaneLogin;
    }

    public GridPane newUserPane() {
        GridPane gridPaneCreateUser = basePane();

        //Arranging all the nodes in the grid
        gridPaneCreateUser.add(name, 0, 0);
        gridPaneCreateUser.add(nameField, 1, 0);
        gridPaneCreateUser.add(password, 0, 1);
        gridPaneCreateUser.add(passwordField, 1, 1);
        gridPaneCreateUser.add(confPassword, 0, 2);
        gridPaneCreateUser.add(passwordFieldConf, 1, 2);
        gridPaneCreateUser.add(create, 0, 3);
        gridPaneCreateUser.add(backButton, 1, 3);
        gridPaneCreateUser.add(incorrectFields, 0, 4);

        return gridPaneCreateUser;
    }

    public void mayBeHid() {
        if(hasLoggedIn) {
            user.setApplicationUser(nameField.getText());
            Platform.runLater(() -> preloaderStage.hide());
        }
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification stateChangeNotification) {
        if(stateChangeNotification.getType() == StateChangeNotification.Type.BEFORE_START) {
            user = (ICredentialUser)stateChangeNotification.getApplication();
            mayBeHid();
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        preloaderStage = stage;

        initaliseFields();

        preloaderStage.setTitle("Log In");
        preloaderStage.initStyle(StageStyle.UTILITY);
        Scene sceneLogin = new Scene(loginPane(), SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2);
        preloaderStage.setScene(sceneLogin);
        preloaderStage.show();

        //Setting on click events for login
        login.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                if (nameField.getText() != null && passwordField.getText() != null) {
                    if (confirmUser(getConnection(), nameField.getText(), passwordField.getText())) {
                        //TODO: Assign username to 'player' and change to game screen
                        hasLoggedIn = true;
                        mayBeHid();
                    } else {
                        setNullFields();
                        incorrectFields.setText("Incorrect username and/or password.");
                        incorrectFields.setVisible(true);
                    }
                }
                else {
                    incorrectFields.setText("Please type in a username and password");
                    incorrectFields.setVisible(true);
                }
            }
        });

        //This will change the scene to show the create new user gridpane
        createNewUser.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                setNullFields();
                Scene sceneCreate = new Scene(newUserPane(), SCREEN_WIDTH, SCREEN_HEIGHT, Color.WHITE);
                preloaderStage.setScene(sceneCreate);
                preloaderStage.show();
            }
        });

        //NEED TO ACCOUNT FOR NON MATCHING PASSWORDS
        create.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                if (nameField.getText() != null && passwordField.getText() != null && passwordFieldConf.getText() != null) {
                    if (doesUserExist(getConnection(), nameField.getText())) {
                        incorrectFields.setText("That username already exists. Please choose another one");
                        incorrectFields.setVisible(true);
                    } else if (!passwordField.getText().equals(passwordFieldConf.getText())){
                        setNullFields();
                        incorrectFields.setText("Passwords do not match");
                        incorrectFields.setVisible(true);
                    } else {
                        //Insert user and password into table
                        addNewUser(getConnection(), nameField.getText(), passwordField.getText());
                        //TODO: Assign username to player and Change to game screen
                        incorrectFields.setText("User created"); //TODO: delete after above
                        incorrectFields.setVisible(true); //TODO: delete after above
                    }
                }
                else {
                    incorrectFields.setText("Please type in a username and password");
                    incorrectFields.setVisible(true);
                }
            }
        });

        //This will change the scene to show the login gridpane
        backButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                setNullFields();
                Scene sceneLogin = new Scene(loginPane(), SCREEN_WIDTH, SCREEN_HEIGHT);
                preloaderStage.setScene(sceneLogin);
                preloaderStage.show();
            }
        });
    }

    //Sets text fields to null
    public void setNullFields() {
        nameField.setText(null);
        passwordField.setText(null);
        passwordFieldConf.setText(null);
    }

    public Connection getConnection() {
        Connection c = null;
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/Leaderboard", "postgres", "123");

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

    public void closeConnections(Connection c, PreparedStatement p, ResultSet r) {
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

    //Returns true if a user exists in the database table and false if it doesn't
    public boolean doesUserExist(Connection c, String username) {
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        try {
            //Creating a query checking if username is in the table
            String query = "SELECT * FROM \"UserDataScore\" WHERE name = '" + username + "'";
            //Creating the Statement
            preparedStatement = c.prepareStatement(query);
            //Executing the query
            rs = preparedStatement.executeQuery();
            //Returns true if a user exists and false if it doesn't
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnections(c, preparedStatement, rs);
        }
        return false;
    }

    //Confirms users name and password exists and is correct.
    public boolean confirmUser(Connection c, String username, String passwordEntered) {
        PreparedStatement saltStatement = null;
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        ResultSet rsDetails = null;
        try {
  /*          //Retrieving the salt from the table based on the username given
            byte[] salt = new byte[0];
            String retVal = "";
            //Creating the query
            String querySalt = "SELECT salt FROM \"UserDataScore\" WHERE name = '" + username + "'";
            //Creating the statement
            saltStatement = c.prepareStatement(querySalt);
            rs = saltStatement.executeQuery();
            while (rs.next()) {
                retVal = rs.getString(1);
            }

            System.out.println(retVal);
            System.out.println(Arrays.toString(retVal.getBytes(StandardCharsets.UTF_8)));
*/
            //Hashing the password given based on the salt retrieved
//            byte[] hashedPassword = hashPassword(salt, passwordEntered);
            //Creating the query
            String queryDetails = "SELECT * FROM \"UserDataScore\" WHERE name = '" + username + "' AND password = '" + /*Arrays.toString(hashedPassword)*/ passwordEntered + "'";  //AND salt = salt?? or not needed? Not needed, checking the username and passwords match is all that's needed, you get the salt from a
            //Creating the statement
            preparedStatement = c.prepareStatement(queryDetails);
            //Executing the query
            rsDetails = preparedStatement.executeQuery();
            //Returns true if user details are correct and false if they aren't
            if (rsDetails.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnections(c, saltStatement, rs);
            closeConnections(null, preparedStatement, rsDetails);
        }
        return false;
    }

    //Adds new user information to the table
    public void addNewUser(Connection c, String username, String passwordEntered) {
        PreparedStatement preparedStatement = null;
        try {
            //Creating a hashed password based on a random salt
//            byte[] randomSalt = returnSalt();
//            byte[] hashedPassword = hashPassword(randomSalt, passwordEntered);
//            System.out.println("salt: " + Arrays.toString(randomSalt) + " hashed password: " + Arrays.toString(hashedPassword));
            //Creating the query
            String query = "INSERT INTO \"UserDataScore\" (name, score, salt, password) VALUES ('" + username + "', " + 0 + ", '" + "nothingtoseehere"/*Arrays.toString(randomSalt)*/ + "', '" + /*Arrays.toString(hashedPassword)*/ passwordEntered + "')";
            System.out.println("query: " + query);
            //Creating the statement
            preparedStatement = c.prepareStatement(query);
            //Executing the query
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnections(c, preparedStatement, null);
        }
    }

//    //Creates a salt for the user when creating a new account
//    public byte[] returnSalt() {
//        SecureRandom random = new SecureRandom();
//        byte[] salt = new byte[16];
//        random.nextBytes(salt);
//        return salt;
//    }

//    //Hashes password using the created or retrieved salt
//    public byte[] hashPassword(byte[] salt, String password) {
//        MessageDigest md; //Removed redundant assign to null (as all class references are null on creation)
//        try {
//            md = MessageDigest.getInstance("SHA-512");
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//            return null; //MessageDigest can cause a null pointer exception, just return null to stop this from happening
//        }
//        md.update(salt);
//        return md.digest(password.getBytes(StandardCharsets.UTF_8));
//    }

    public void initaliseFields() {
        //Setting properties of buttons
        login.setText("Login");
        login.setMinHeight(30);
        login.setMinWidth(100);

        createNewUser.setText("Create New Account");
        createNewUser.setMinHeight(30);
        createNewUser.setMinWidth(150);

        backButton.setText("Back");
        backButton.setMinHeight(30);
        backButton.setMinWidth(150);

        create.setText("Create User");
        create.setMinHeight(30);
        create.setMinWidth(150);

        //Setting properties of text/textFields
        name.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 20));
        password.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 20));
        confPassword.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 20));
        nameField.setMinHeight(30);
        nameField.setMinWidth(80);
        passwordField.setMinHeight(30);
        passwordField.setMinWidth(80);
        passwordFieldConf.setMinHeight(30);
        passwordFieldConf.setMinWidth(80);
        incorrectFields.setFill(Color.RED);
        incorrectFields.setVisible(false);

        //Sets text fields to null
        setNullFields();
    }
}