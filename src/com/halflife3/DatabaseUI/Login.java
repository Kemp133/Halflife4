package com.halflife3.DatabaseUI;

import GameUI.Windows;
import javafx.application.Application;
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

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import java.sql.*;

import java.util.Arrays;

public class Login extends Application {
    /*mtest*/

    private static final double SCREEN_WIDTH = Screen.getPrimary().getBounds().getWidth();
    private static final double SCREEN_HEIGHT = Screen.getPrimary().getBounds().getHeight();

    private Stage Pstage = null;
    Button login = new Button();
    Button createNewUser = new Button();
    Button backButton = new Button();
    Button create = new Button();
    Text name = new Text("Name");
    TextField nameField = new TextField();
    Text password = new Text("Password");
    PasswordField passwordField = new PasswordField();
    Text confPassword = new Text("Confirm Password");
    PasswordField passwordFieldConf = new PasswordField();
    Text incorrectFields = new Text();

    public GridPane loginPane() {
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
        GridPane gridPaneCreateUser = new GridPane();

        //Setting size for the pane
        gridPaneCreateUser.setMinSize(800, 600);

        //Setting the padding
        gridPaneCreateUser.setPadding(new Insets(10, 10, 10, 10));

        //Setting the vertical and horizontal gaps between the columns
        gridPaneCreateUser.setVgap(30);
        gridPaneCreateUser.setHgap(30);

        //Setting the Grid alignment
        gridPaneCreateUser.setAlignment(Pos.CENTER);

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


    @Override
    public void start(Stage stage) throws Exception {
        //Setting properties of buttons

        Pstage = stage;
        login.setText("com.halflife3.DatabaseUI.Login");
        login.setMinHeight(30);
        login.setMinWidth(100);

        createNewUser.setText("Create New Account");
        createNewUser.setMinHeight(30);
        createNewUser.setMinWidth(150);

        backButton.setText("Back");
        backButton.setMinHeight(30);
        backButton.setMinWidth(150);

        create.setText("Create com.halflife3.DatabaseUI.User");
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

        stage.setTitle("com.halflife3.DatabaseUI.Login/Create com.halflife3.DatabaseUI.User");
        Scene sceneLogin = new Scene(loginPane(), SCREEN_WIDTH, SCREEN_HEIGHT);
        stage.setScene(sceneLogin);
        stage.show();

        //Setting on click events for login
        login.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                if (nameField.getText() != null && passwordField.getText() != null) {
                    if (confirmUser(getConnection(), nameField.getText(), passwordField.getText()) == true) {
                        //TODO: Assign username to 'player' and change to game screen
                        incorrectFields.setText("Found user - then would log in"); //TODO: delete after above
                        incorrectFields.setVisible(true); //TODO: Delete after above

                       try {
                            new Windows().start(Pstage);
                        } catch (FileNotFoundException ex) {
                            ex.printStackTrace();
                        }

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
                stage.setScene(sceneCreate);
                stage.show();
            }
        });

        //NEED TO ACCOUNT FOR NON MATCHING PASSWORDS
        create.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                if (nameField.getText() != null && passwordField.getText() != null && passwordFieldConf.getText() != null) {
                    if (doesUserExist(getConnection(), nameField.getText()) == true) {
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
                        incorrectFields.setText("com.halflife3.DatabaseUI.User created"); //TODO: delete after above
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
                stage.setScene(sceneLogin);
                stage.show();
            }
        });
    }

    public static Connection getConnection() {
        Connection c = null;
        try {
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://rogue.db.elephantsql.com:5432/nuzmlzpr";
            url = url.trim();
            c = DriverManager.getConnection(url, "nuzmlzpr", "pd7OdC_3BiVrAPNU68CETtFtBaqFxJFB");

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
            String query = "SELECT * FROM userdatascore WHERE name = '" + username + "'";
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
            //Retrieving the salt from the table based on the username given
            byte[] salt = new byte[0];
            //Creating the query
            String querySalt = "SELECT salt FROM userdatascore WHERE \"name\" = '" + username + "'";
            //Creating the statement
            saltStatement = c.prepareStatement(querySalt);
            rs = saltStatement.executeQuery();
            while (rs.next()) {
                salt = rs.getBytes("salt");
            }
            //Hashing the password given based on the salt retrieved
            byte[] hashedPassword = hashPassword(salt, passwordEntered);
            //Creating the query
            String queryDetails = "SELECT * FROM userdatascore WHERE \"name\" = '" + username + "' AND \"password\" = '" + passwordEntered + "'";
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
            byte[] randomSalt = returnSalt();
            byte[] hashedPassword = hashPassword(randomSalt, passwordEntered);
            //Creating the query
            String query = "INSERT INTO userdatascore (name, score, salt, password) VALUES ('" + username + "', " + 0 + ", '" + "123" + "', '" + passwordEntered + "')"; // Added a random salt and plaintext password
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

    //Creates a salt for the user when creating a new account
    public byte[] returnSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    //Hashes password using the created or retrieved salt
    public byte[] hashPassword(byte[] salt, String password) {
        MessageDigest md; //Removed redundant assign to null (as all class references are null on), as per IntelliJ's request
        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null; //MessageDigest can cause a null pointer exception, just return null to stop this from happening (as well as acting as an error state)
        }
        md.update(salt);
        return md.digest(password.getBytes(StandardCharsets.UTF_8));
    }

    //Sets text fields to null
    public void setNullFields() {
        nameField.setText(null);
        passwordField.setText(null);
        passwordFieldConf.setText(null);
    }
}