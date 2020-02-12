import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.sql.*;

public class Login extends Application {

    private static final double SCREEN_WIDTH = Screen.getPrimary().getBounds().getWidth();
    private static final double SCREEN_HEIGHT = Screen.getPrimary().getBounds().getHeight();

    Button login = new Button();
    Button newUser = new Button();
    Text name = new Text("Name");
    TextField nameField = new TextField();
    Text password = new Text("Password");
    TextField passwordField = new TextField();
    Text incorrectFields = new Text("Please type in a username and password");

    GridPane gridPane = new GridPane();


    @Override
    public void start(Stage stage) throws Exception {

        //Setting properties of buttons
        login.setText("Login");
        login.setMinHeight(30);
        login.setMinWidth(100);

        newUser.setText("Create Account");
        newUser.setMinHeight(30);
        newUser.setMinWidth(150);

        //Setting properties of text/textFields
        name.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 20));
        password.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 20));
        nameField.setMinHeight(30);
        nameField.setMinWidth(80);
        nameField.setText(null);
        passwordField.setMinHeight(30);
        passwordField.setMinWidth(80);
        passwordField.setText(null);
        incorrectFields.setFill(Color.RED);
        incorrectFields.setVisible(false);

        //Setting size for the pane
        gridPane.setMinSize(800, 600);

        //Setting the padding
        gridPane.setPadding(new Insets(10, 10, 10, 10));

        //Setting the vertical and horizontal gaps between the columns
        gridPane.setVgap(30);
        gridPane.setHgap(30);

        //Setting the Grid alignment
        gridPane.setAlignment(Pos.CENTER);

        //Arranging all the nodes in the grid
        gridPane.add(name, 0, 0);
        gridPane.add(nameField, 1, 0);
        gridPane.add(password, 0, 1);
        gridPane.add(passwordField, 1, 1);
        gridPane.add(login, 0, 2);
        gridPane.add(newUser, 1, 2);
        gridPane.add(incorrectFields, 0, 3);

        stage.setTitle("Login/Create User");
        Scene scene = new Scene(gridPane, SCREEN_WIDTH, SCREEN_HEIGHT, Color.WHITE);
        stage.setScene(scene);
        stage.show();

        //Setting on click events for login
        login.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                if (nameField.getText() != null && passwordField.getText() != null) {
                    try {
                        if (confirmUser(getConnection(), nameField.getText(), passwordField.getText())) {
                            //Assign username and change to game screen
                        } else {
                            nameField.setText(null);
                            passwordField.setText(null);
                            incorrectFields.setText("User not found. Please create a user.");
                            incorrectFields.setVisible(true);
                        }
                    } catch(Exception ex) {
                        System.out.println(ex.toString());
                    }
                }
                else {
                    incorrectFields.setText("Please type in a username and password");
                    incorrectFields.setVisible(true);
                }
            }
        });

        newUser.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                if (nameField.getText() != null && passwordField.getText() != null) {
                    try {
                        if (doesUserExist(getConnection(), nameField.getText(), passwordField.getText())) {
                            incorrectFields.setText("That username already exists. Please choose another one");
                            incorrectFields.setVisible(true);
                        } else {
                            //Insert user and password into table
                            //change to game screen
                        }
                    } catch (Exception ex) {
                        System.out.println(ex.toString());
                    }
                }
                else {
                    incorrectFields.setText("Please type in a username and password");
                    incorrectFields.setVisible(true);
                }
            }
        });
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

    //Returns true if a user exists in the database table and false if it doesn't
    public boolean doesUserExist(Connection c, String username, String password) throws SQLException {
        //Creating the Statement
        Statement stmt = c.createStatement();
        String query = "SELECT * FROM \"TopLeaderboard\" WHERE playername = username";  //Creating a query checking if username is in the table
        //Executing the query
        ResultSet rs = stmt.executeQuery(query);
        //Returns true if a user exists and false if it doesn't
        if (rs.next() != false) {
            return true;
        } else {
            return false;
        }
    }

    //Confirms users name and password exists and is correct. WILL NEED TO CHANGE SCORE -> PASSWORD AND
    public boolean confirmUser(Connection c, String username, String password) throws SQLException {
        //Creating the Statement
        Statement stmt = c.createStatement();
        String query = "SELECT * FROM \"TopLeaderboard\" WHERE playername = username AND score = password"; //this row will be password later on not score
        //Executing the query
        ResultSet rs = stmt.executeQuery(query);
        //Returns true if user details are correct and false if they aren't
        if (rs.next() != false) {
            return true;
        } else {
            return false;
        }
    }

    public void setData(Connection c, String username, String password) throws SQLException {

    }
}