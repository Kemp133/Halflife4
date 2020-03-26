package com.halflife3.DatabaseUI;

import com.halflife3.GameUI.FirstMenu;
import com.halflife3.GameUI.interfaces.ICredentialUser;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;

public class Login extends Preloader {

    private static final double SCREEN_WIDTH = 800;//Screen.getPrimary().getBounds().getWidth();
    private static final double SCREEN_HEIGHT = 600;//Screen.getPrimary().getBounds().getHeight();

    //region Variables
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

    private static Stage preloaderStage = null;

    private boolean hasLoggedIn = false;
    private ICredentialUser user;
    //endregion

    @Override
    public void handleStateChangeNotification(StateChangeNotification info) {
        if (info.getType() == StateChangeNotification.Type.BEFORE_START) {
            user = (ICredentialUser) info.getApplication();
            mayBeHid();
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        preloaderStage = stage;
        preloaderStage.setTitle("Login/Create User");

        initialiseFields();

        Scene sceneLogin = new Scene(loginPane(), SCREEN_WIDTH, SCREEN_HEIGHT);
        stage.setScene(sceneLogin);
        stage.show();

        //Setting on click events for login
        login.setOnAction(e -> tryToLogin());

        //This will change the scene to show the create new user stackPane
        createNewUser.setOnAction(e -> {
            setTextFieldsToNull();
            incorrectFields.setVisible(false);
            Scene sceneCreate = new Scene(newUserPane(), SCREEN_WIDTH, SCREEN_HEIGHT, Color.WHITE);
            stage.setScene(sceneCreate);
            stage.show();
        });

        //NEED TO ACCOUNT FOR NON MATCHING PASSWORDS
        create.setOnAction(e -> tryToCreateAccount());

        //This will change the scene to show the login stackPane
        backButton.setOnAction(e -> {
            setTextFieldsToNull();
            incorrectFields.setVisible(false);
            Scene sceneLogin1 = new Scene(loginPane(), SCREEN_WIDTH, SCREEN_HEIGHT);
            stage.setScene(sceneLogin1);
            stage.show();
        });

        /*
        Handles enter key press for both the login and create account scene.
        For login it will carry out as if login has been pressed
        For create account it will carry out as if create account has been pressed
        */
        stage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (stage.getScene() == sceneLogin) {
                    tryToLogin();
                } else {
                    tryToCreateAccount();
                }
            }
        });

        /*sceneCreate.addEventHandler(KeyEvent.KEY_PRESSED, event -> {  //TODO: Test doesn't conflict with previous scene on enter button press
            if (event.getCode() == KeyCode.ENTER) {
                if (nameField.getText() != null && passwordField.getText() != null) {
                    if (confirmUser(getConnection(), nameField.getText(), passwordField.getText())) {
                        //TODO: Assign username to 'player' and change to game screen
                        hasLoggedIn = true;
                        mayBeHid();
//                       try {
//                            new Windows().start(preloaderStage);
//                        } catch (FileNotFoundException ex) {
//                            ex.printStackTrace();
//                        }
                    } else {
                        setNullFields();
                        incorrectFields.setText("Incorrect username and/or password.");
                        incorrectFields.setVisible(true);
                    }
                }
                else {
                    incorrectFields.setText("Type in a username and password");
                    incorrectFields.setVisible(true);

                }
            }
        });*/
    }

    private void tryToLogin() {
        if (nameField.getText() != null && passwordField.getText() != null) {
            if (confirmUser(getConnection(), nameField.getText(), passwordField.getText())) {
                //TODO: Assign username to 'player' and change to game screen
                hasLoggedIn = true;
                mayBeHid();
//                       try {
//                            new Windows().start(preloaderStage);
//                        } catch (FileNotFoundException ex) {
//                            ex.printStackTrace();
//                        }
            } else {
                setTextFieldsToNull();
                incorrectFields.setText("Incorrect username and/or password.");
                incorrectFields.setVisible(true);
            }
        } else {
            incorrectFields.setText("Type in a username and password");
            incorrectFields.setVisible(true);
        }
    }

    private void tryToCreateAccount() {
        if (nameField.getText() != null && passwordField.getText() != null && passwordFieldConf.getText() != null) {
            if (doesUserExist(getConnection(), nameField.getText())) {
                incorrectFields.setText("That username already exists. Please choose another one");
                incorrectFields.setVisible(true);
            } else if (!passwordField.getText().equals(passwordFieldConf.getText())) {
                setTextFieldsToNull();
                incorrectFields.setText("Passwords do not match");
                incorrectFields.setVisible(true);
            } else {
                //Insert user and password into table
                addNewUser(getConnection(), nameField.getText(), passwordField.getText());
                //TODO: Assign username to player and Change to game screen
                incorrectFields.setText("User created"); //TODO: delete after above
                incorrectFields.setVisible(true); //TODO: delete after above
            }
        } else {
            incorrectFields.setText("Please type in a username and password");
            incorrectFields.setVisible(true);
        }
    }

    private void initialiseFields() throws FileNotFoundException {
        //Sets the properties of the buttons
        buttonProperties();

        //Setting properties of text/textFields
        name.setFont(Font.font("wide latin", FontWeight.BOLD, FontPosture.REGULAR, 20));
        password.setFont(Font.font("wide latin", FontWeight.BOLD, FontPosture.REGULAR, 20));
        confPassword.setFont(Font.font("wide latin", FontWeight.BOLD, FontPosture.REGULAR, 20));
        nameField.setMinHeight(30);
        nameField.setMinWidth(80);
        passwordField.setMinHeight(30);
        passwordField.setMinWidth(80);
        passwordFieldConf.setMinHeight(30);
        passwordFieldConf.setMinWidth(80);
        incorrectFields.setFill(Color.RED);
        incorrectFields.setVisible(false);

        setTextFieldsToNull();
    }

    public void buttonProperties() throws FileNotFoundException {

        //region Create new account button
        createNewUser.setMaxHeight(30);
        createNewUser.setMaxWidth(150);

        var iSCreateNew = new FileInputStream("res/button_create-new-account (1).png");
        var imageCreateNew = new Image(iSCreateNew, createNewUser.getWidth(), createNewUser.getHeight(), false, true);
        var cNImage = new BackgroundImage(imageCreateNew, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                new BackgroundSize(createNewUser.getWidth(), createNewUser.getHeight(), true, true, true, false));
        var cB = new Background(cNImage);
        createNewUser.setBackground(cB);
        //endregion

        //region Login button
        login.setMaxHeight(30);
        login.setMaxWidth(150);

        var iSLogin = new FileInputStream("res/button_login (1).png");
        var imageLogin = new Image(iSLogin, login.getWidth(), login.getHeight(), false, true);
        var bImageLogin = new BackgroundImage(imageLogin, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                new BackgroundSize(login.getWidth(), login.getHeight(), true, true, true, false));
        var newBLogin = new Background(bImageLogin);
        login.setBackground(newBLogin);
        //endregion

        //region Back button
        backButton.setMinHeight(30);
        backButton.setMinWidth(150);

        var iSBack = new FileInputStream("res/button_back.png");
        var imageBack = new Image(iSBack, login.getWidth(), login.getHeight(), false, true);
        var bBack = new BackgroundImage(imageBack, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                new BackgroundSize(backButton.getWidth(), backButton.getHeight(), true, true, true, false));
        var newBBack = new Background(bBack);
        backButton.setBackground(newBBack);
        //endregion

        //region Create button
        create.setMinHeight(30);
        create.setMinWidth(150);

        var isCreate = new FileInputStream("res/button_create-user.png");
        var imageCreate = new Image(isCreate, login.getWidth(), login.getHeight(), false, true);
        var bImageCreate = new BackgroundImage(imageCreate, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                new BackgroundSize(create.getWidth(), create.getHeight(), true, true, true, false));
        var newBCreate = new Background(bImageCreate);
        create.setBackground(newBCreate);
        //endregion
    }

    public void setTextFieldsToNull() {
        nameField.setText(null);
        passwordField.setText(null);
        passwordFieldConf.setText(null);
    }

    public StackPane loginPane() {
        GridPane gridPaneLogin = basePane();

        //Arranging all the nodes in the grid
        gridPaneLogin.add(name, 0, 0);
        gridPaneLogin.add(nameField, 1, 0);
        gridPaneLogin.add(password, 0, 1);
        gridPaneLogin.add(passwordField, 1, 1);
        gridPaneLogin.add(login, 0, 2);
        gridPaneLogin.add(createNewUser, 1, 2);
        gridPaneLogin.add(incorrectFields, 0, 3);

        StackPane stack = new StackPane();

        //TODO: Below is useless while using solution off adding transparent box to image
        //stack.setStyle("-fx-background-color: transparent;");
        //stack.setStyle("-fx-background-color: rgba(0, 100, 100, 0.5); -fx-background-radius: 10;");

        stack.getChildren().addAll(gridPaneLogin);
        stack.setBackground(addBackground());

        return stack;
    }

    private static GridPane basePane() {
        GridPane gridPaneLogin = new GridPane();
        //Setting size for the pane
        gridPaneLogin.setMinSize(FirstMenu.SCREEN_WIDTH, FirstMenu.SCREEN_HEIGHT);

        //Setting the padding
        gridPaneLogin.setPadding(new Insets(10, 10, 10, 10));

        //Setting the vertical and horizontal gaps between the columns
        gridPaneLogin.setVgap(30);
        gridPaneLogin.setHgap(90);

        //Setting the Grid alignment
        gridPaneLogin.setAlignment(Pos.CENTER);

        return gridPaneLogin;
    }

    private Background addBackground() {
        try {
            var inputStream = new FileInputStream("res/LoginBackground.png");
            var image = new Image(inputStream);
            var backgroundSize = new BackgroundSize(SCREEN_WIDTH, SCREEN_HEIGHT,
                    false, false, false, true);
            var backgroundImage = new BackgroundImage(image, BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER, backgroundSize);
            return new Background(backgroundImage);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return null;
    }

    public static Connection getConnection() {
        Connection c = null;
        try {
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://rogue.db.elephantsql.com:5432/nuzmlzpr";
            url = url.trim();
            c = DriverManager.getConnection(url, "nuzmlzpr", "pd7OdC_3BiVrAPNU68CETtFtBaqFxJFB");
            System.out.println(c != null ? "Connection complete" : "Connection failed");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

        return c;
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
            while (rs.next())
                salt = rs.getBytes("salt");

            //Hashing the password given based on the salt retrieved
            byte[] hashedPassword = hashPassword(salt, passwordEntered);

            //Creating the query
            String queryDetails = "SELECT * FROM userdatascore WHERE \"name\" = '" +
                    username + "' AND \"password\" = '" + passwordEntered + "'";

            //Creating the statement
            preparedStatement = c.prepareStatement(queryDetails);

            //Executing the query
            rsDetails = preparedStatement.executeQuery();

            //Returns true if user details are correct and false if they aren't
            if (rsDetails.next()) return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnections(c, saltStatement, rs);
            closeConnections(null, preparedStatement, rsDetails);
        }

        return false;
    }

    private void mayBeHid() {
        if (hasLoggedIn) {
            user.setApplicationUser(nameField.getText());
            Platform.runLater(() -> preloaderStage.hide());
        }
    }

    public StackPane newUserPane() {
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

        StackPane stack = new StackPane();

        //TODO: Below is useless while using solution off adding transparent box to image
        //stack.setStyle("-fx-background-color: transparent;");
        //stack.setStyle("-fx-background-color: rgba(0, 100, 100, 0.5); -fx-background-radius: 10;");
        stack.getChildren().addAll(gridPaneCreateUser);
        stack.setBackground(addBackground());

        return stack;
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
            if (rs.next()) return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnections(c, preparedStatement, rs);
        }
        return false;
    }

    public void closeConnections(Connection c, PreparedStatement p, ResultSet r) {
        if (c != null) try { c.close(); } catch (SQLException ignored) {}

        if (p != null) try { p.close(); } catch (SQLException ignored) {}

        if (r != null) try { r.close(); } catch (SQLException ignored) {}
    }

    //Adds new user information to the table
    public void addNewUser(Connection c, String username, String passwordEntered) {
        PreparedStatement preparedStatement = null;
        try {
            //Creating a hashed password based on a random salt
//            byte[] randomSalt = returnSalt();
//            byte[] hashedPassword = hashPassword(randomSalt, passwordEntered);
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

    //region Currently unused methods
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

    //endregion
}