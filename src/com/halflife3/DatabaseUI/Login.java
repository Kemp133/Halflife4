package com.halflife3.DatabaseUI;

import com.halflife3.Controller.DatabaseManager;
import com.halflife3.GameUI.LoginAttributes;
import com.halflife3.GameUI.MenuUtilitites;
import com.halflife3.GameUI.interfaces.ICredentialUser;
import com.halflife3.Networking.NetworkingUtilities;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

import java.io.*;
import java.sql.*;

/**
 * This class is the entry point of the program. This is the first screen which the user interacts with, and the user
 * cannot get off this screen unless they either exit the application or log into the system. This uses a class in
 * JavaFX called {@code Preloader}, which is registered when {@code main} is called in MenuController (the entry point
 * of the <i>Application</i> itself).
 * <p>
 * This class works by getting called when a special environment variable is set in the main method of the {@code
 * MenuController}, which then invokes this class via a reference passed to this environmental variable. The preloader
 * then runs, waiting for the application to load in the background so that it can be shown.
 * <p>
 * In the meantime, the preloader shows the log in screen. Here, the user has three main options:
 * <ol>
 *     <li>Log in to an existing account</li>
 *     <li>Create a new account</li>
 *     <li>Exit the application</li>
 * </ol>
 * For the first eventuality, the user puts in their username and password, and can then log into the system.
 * Firstly, the user is checked to make sure they have an account to log into the application to begin with. If they
 * don't, then an error is displayed and they are prompted that their log in attempt was unsuccessful. In the event
 * that the log in was successful however, the username of the user is passed through to the {@code ApplicationUser}
 * reference inside of {@code MenuController} so that the username can be accessed in the game.
 * <p>
 * For the second eventuality, a new pane is created which contains three text boxes for a username, password, and
 * confirm password. The confirm password is there so that the user has chance to enter their password twice, and
 * that in the event they typed it wrong either time, they are alerted that the passwords are different and that they
 * should check them to make sure they're entered correctly. The username doesn't have this kind of verification, but
 * is checked in the database first to make sure it doesn't exist. If it does, then the user is prompted that the
 * username they chose has already been chosen, and to choose another one instead. Once both of these checks are
 * successful, the username and password are sent off to the database and stored, and the user gets a message that
 * their account has been created so that they can go back to the log in page and log in.
 * <p>
 * For the final eventuality, the application just ends, and the JVM instance closes completely.
 */
public class Login extends Preloader {
	Button login         = new Button();
	Button createNewUser = new Button();
	Button backButton    = new Button();
	Button create        = new Button();

	Text          name              = new Text("Name");
	TextField     nameField         = new TextField();
	Text          password          = new Text("Password");
	PasswordField passwordField     = new PasswordField();
	Text          confPassword      = new Text("Confirm Password");
	PasswordField passwordFieldConf = new PasswordField();
	CheckBox      chbRememberMe     = new CheckBox("Remember me?");
	Text          incorrectFields   = new Text();

	private Stage preloaderStage = null;

	private boolean         hasLoggedIn = false;
	private ICredentialUser user;
	private LoginAttributes loginAttributes;

	String mySecurePassword;
	String salt;

	/** A method to set the text properties of the objects in the menu */
	private void textProperties() {
		Font paladinFont = null;
		try {
			paladinFont = Font.loadFont(new FileInputStream(new File("res/Font/PaladinsSemiItalic.otf")), 40);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		name.setFont(paladinFont);
		name.setStyle("-fx-font-size: 20px;");
		name.setFill(Color.BLUE);
		password.setFont(paladinFont);
		password.setStyle("-fx-font-size: 20px;");
		password.setFill(Color.BLUE);
		confPassword.setFont(paladinFont);
		confPassword.setStyle("-fx-font-size: 20px;");
		confPassword.setFill(Color.BLUE);
		incorrectFields.setStyle("-fx-font-family: 'Roboto Regular'; -fx-font-size: 14px;");
		incorrectFields.setFill(Color.RED);
	}

	/** A method which sets the text values of the buttons in the menu */
	public void setButtonText() {
		createNewUser.setText("Create New Account");
		login.setText("Login");
		backButton.setText("Back");
		create.setText("Create User");
	}

	/**
	 * A method to generate a grid pane to build other panes off of
	 *
	 * @return A {@code GridPane} which has all the essential styling done already
	 */
	private GridPane basePane() {

		GridPane gridPaneLogin = new GridPane();
		//Setting size for the pane
		gridPaneLogin.setMinSize(500, 400);

		//Setting the vertical and horizontal gaps between the columns
		gridPaneLogin.setVgap(50);
		gridPaneLogin.setHgap(60);

		//Setting the Grid alignment
		gridPaneLogin.setAlignment(Pos.CENTER);

		return gridPaneLogin;
	}

	/**
	 * A method used to generate a base border pane to build the other panes off of
	 *
	 * @return A {@code BorderPane} which has all the essential styling done already
	 */
	private BorderPane baseBPane() {
		BorderPane borderPane = new BorderPane();

		Font paladinFont = null;
		try {
			paladinFont = Font.loadFont(new FileInputStream(new File("res/Font/PaladinsSemiItalic.otf")), 40);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		Label title = new Label("Login");
		title.setFont(paladinFont);
		title.setStyle("-fx-text-fill: linear-gradient(#0000FF, #FFFFFF 95%);");

		Insets insets  = new Insets(30);
		Pane   topPane = new Pane();
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

		return borderPane;
	}

	/**
	 * A method to generate the pane used for the "Log In" section of the preloader
	 *
	 * @return A {@code BorderPane} which contains all the UI elements for the log in screen
	 */
	private BorderPane loginPane() {
		GridPane gridPaneLogin = basePane();

		BorderPane borderPane = baseBPane();

		textProperties();

		//Arranging all the nodes in the grid
		gridPaneLogin.add(name, 0, 0);
		gridPaneLogin.add(nameField, 1, 0);
		gridPaneLogin.add(password, 0, 1);
		gridPaneLogin.add(passwordField, 1, 1);
		gridPaneLogin.add(login, 0, 2);
		gridPaneLogin.add(createNewUser, 1, 2);
		gridPaneLogin.add(chbRememberMe, 1, 3);
		gridPaneLogin.add(incorrectFields, 0, 4);

		//Setting the padding and background color for the gridpane
		gridPaneLogin.setPadding(new Insets(40, 0, 0, 0));
		gridPaneLogin.setStyle("-fx-background-color: rgba(176,224,230,0.8);");

		borderPane.setBackground(MenuUtilitites.getBackground(getClass(), "res/Login/login-background.jpg"));
		borderPane.setCenter(gridPaneLogin);

		File f = new File("res/Login/LoginStyleSheet.css");
		borderPane.getStylesheets().add("file:///" + f.getAbsolutePath().replace("\\", "/"));

		return borderPane;
	}

	/**
	 * A method to generate the pane used for the "New User" section of the preloader
	 *
	 * @return A {@code BorderPane} which contains all the UI elements for the user pane
	 */
	private BorderPane newUserPane() {
		GridPane gridPaneCreateUser = basePane();

		BorderPane newBorderPane = baseBPane();

		textProperties();

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

		//Setting the padding and background color for the gridpane
		gridPaneCreateUser.setPadding(new Insets(40, 0, 0, 0));
		gridPaneCreateUser.setStyle("-fx-background-color: rgba(176,224,230,0.8);");
		gridPaneCreateUser.setMinSize(500, 400);

		newBorderPane.setBackground(MenuUtilitites.getBackground(getClass(), "res/Login/login-background.jpg"));
		newBorderPane.setCenter(gridPaneCreateUser);

		File f = new File("res/Login/LoginStyleSheet.css");
		newBorderPane.getStylesheets().add("file:///" + f.getAbsolutePath().replace("\\", "/"));

		return newBorderPane;
	}

	//region Preloader Required Methods
	/** A method to check whether the preloader may be hid, or if the user still has not logged in yet */
	private void mayBeHid() {
		if (hasLoggedIn) {
			user.setApplicationUser(nameField.getText());
			Platform.runLater(() -> preloaderStage.hide());
		}
	}

	@Override
	public void handleStateChangeNotification(StateChangeNotification info) {
		if (info.getType() == StateChangeNotification.Type.BEFORE_START) {
			user = (ICredentialUser) info.getApplication();
			if (getUserLoginAttributes()) {
				if (loginAttributes.rememberMe) {
					nameField.setText(loginAttributes.username);
					passwordField.setText(loginAttributes.password);
					hasLoggedIn = DatabaseManager.confirmUser(DatabaseManager.getConnection(),
							loginAttributes.username, loginAttributes.password);
				}
			}
			mayBeHid();
		}
	}
	//endregion

	/** This method initializes all the fields in the views ready to display */
	private void initialiseFields() {
		//Sets the properties of the buttons
		setButtonText();

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

		//Sets text fields to null
		setNullFields();
	}

	@Override
	public void start(Stage stage) {
		//Setting properties of buttons
		preloaderStage = stage;
		preloaderStage.setTitle("Login/Create User");

		initialiseFields();
		Scene sceneLogin = new Scene(loginPane(), 800, 600);
		stage.setScene(sceneLogin);
		stage.show();

		//Setting on click events for login
		login.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				if (nameField.getText() != null && passwordField.getText() != null) {
					if (DatabaseManager.confirmUser(DatabaseManager.getConnection(), nameField.getText(),
							passwordField.getText())) {
						hasLoggedIn = true;
						if (chbRememberMe.isSelected()) writeLoginAttributesToFile();
						mayBeHid();
					} else {
						setNullFields();
						incorrectFields.setText("Incorrect username and/or password.");
						incorrectFields.setVisible(true);
					}
				} else {
					incorrectFields.setText("Type in a username and password");
					incorrectFields.setVisible(true);
				}
			}
		});

		//This will change the scene to show the create new user scene
		createNewUser.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				setNullFields();
				incorrectFields.setVisible(false);
				Scene sceneCreate = new Scene(newUserPane(), 800, 600, Color.WHITE);
				stage.setScene(sceneCreate);
				stage.show();
			}
		});

		//Creates the account in the database if the username isn't in use and if the passwords match
		create.setOnAction(e -> {
			if (nameField.getText() != null && passwordField.getText() != null && passwordFieldConf.getText() != null) {
				if (doesUserExist(DatabaseManager.getConnection(), nameField.getText()) == true) {
					incorrectFields.setText("Username already exists. Choose another");
					incorrectFields.setVisible(true);
				} else if (!passwordField.getText().equals(passwordFieldConf.getText())) {
					setNullFields();
					incorrectFields.setText("Passwords do not match");
					incorrectFields.setVisible(true);
				} else {
					//Insert user and password into table
					addNewUser(DatabaseManager.getConnection(), nameField.getText(), passwordField.getText());
					logInUser(nameField.getText());
				}
			} else {
				incorrectFields.setText("Please type in a username and password");
				incorrectFields.setVisible(true);
			}
		});

		//This will change the scene to show the login scene
		backButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				setNullFields();
				incorrectFields.setVisible(false);
				Scene sceneLogin = new Scene(loginPane(), 800, 600);
				stage.setScene(sceneLogin);
				stage.show();
			}
		});

        /*
        Handles enter key press for both the login and create account scene.
        For login it will carry out as if login has been pressed
        For create account it will carry out as if create account has been pressed
        */
		stage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
			if (event.getCode() == KeyCode.ENTER) {
				if (stage.getScene() == sceneLogin) {
					if (nameField.getText() != null && passwordField.getText() != null) {
						if (DatabaseManager.confirmUser(DatabaseManager.getConnection(), nameField.getText(),
								passwordField.getText())) {
							hasLoggedIn = true;
							mayBeHid();
						} else {
							setNullFields();
							incorrectFields.setText("Incorrect username and/or password.");
							incorrectFields.setVisible(true);
						}
					} else {
						incorrectFields.setText("Type in a username and password");
						incorrectFields.setVisible(true);
					}
				} else {
					if (nameField.getText() != null && passwordField.getText() != null && passwordFieldConf.getText() != null) {
						if (doesUserExist(DatabaseManager.getConnection(), nameField.getText()) == true) {
							incorrectFields.setText("That username already exists. Please choose another one");
							incorrectFields.setVisible(true);
						} else if (!passwordField.getText().equals(passwordFieldConf.getText())) {
							setNullFields();
							incorrectFields.setText("Passwords do not match");
							incorrectFields.setVisible(true);
						} else {
							//Insert user and password into table
							addNewUser(DatabaseManager.getConnection(), nameField.getText(), passwordField.getText());
							incorrectFields.setFill(Color.GREEN);
							incorrectFields.setText("User created");
							incorrectFields.setVisible(true);
						}
					} else {
						incorrectFields.setText("Please type in a username and password");
						incorrectFields.setVisible(true);
					}
				}
			}
		});
	}

	/** Sets text fields to null */
	private void setNullFields() {
		nameField.setText(null);
		passwordField.setText(null);
		passwordFieldConf.setText(null);
	}

	private void logInUser(String username) {
		nameField.setText(username);
		hasLoggedIn = true;
		mayBeHid();
	}

	//region Unique Database Methods
	/**
	 * A method to check if a user with the given username exists in the database already or not
	 *
	 * @param c        The {@code Connection} to use to connect to the database
	 * @param username The username to check against the database to see if it exists already
	 *
	 * @return Returns {@code true} if a user exists in the database table and {@code false} if it doesn't
	 */
	private boolean doesUserExist(Connection c, String username) {
		PreparedStatement preparedStatement = null;
		ResultSet         rs                = null;
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
			DatabaseManager.closeConnections(c, preparedStatement, rs);
		}
		return false;
	}

	/**
	 * Adds new user information to the database
	 *
	 * @param c               The {@code Connection} to use to connect to the database
	 * @param username        The username of the user to submit
	 * @param passwordEntered The password of the user to submit
	 */
	private void addNewUser(Connection c, String username, String passwordEntered) {
		PreparedStatement preparedStatement = null;
		try {
			//Generating password and salt
			String saltAdd = Password.returnSalt();
			genPassword(passwordEntered, saltAdd);
			//Creating the query
			String query =
					"INSERT INTO userdatascore (name, score, salt, password) VALUES ('" + username + "', " + 0 + ", '" + "123" + "', '" + passwordEntered + "')"; //saltAdd changed to 123 and mySecurePassword changed to passwordEntered
			//Creating the statement
			preparedStatement = c.prepareStatement(query);
			//Executing the query
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DatabaseManager.closeConnections(c, preparedStatement, null);
		}
	}

	/**
	 * A method to generate a secure password from a passed plain text password
	 *
	 * @param password The password to encrypt
	 * @param salt     The salt to encrypt the password with
	 */
	private void genPassword(String password, String salt) {
		String myPassword = password;

		// Protect user's password. The generated value can be stored in DB.
		mySecurePassword = Password.generateSecurePassword(myPassword, salt);

		// Print out protected password
		System.out.println("My secure password = " + mySecurePassword);
		System.out.println("Salt value = " + salt);
	}
	//endregion

	//region Log In Attribute Loading and Saving
	/**
	 * Try and load log in values for automatically logging in. In the event the file is not found, a new one is
	 * created
	 * with generic values and saved instead, ready for the next time the person logs in
	 *
	 * @return {@code true} if a configuration file has been found, and {@code false} otherwise
	 */
	private boolean getUserLoginAttributes() {
		try (var fis = new FileInputStream("AppData/login.conf")) {
			try (var ois = new ObjectInputStream(fis)) {
				loginAttributes = (LoginAttributes) ois.readObject();
				return true;
			}
		} catch (FileNotFoundException e) {
			try (var fos = new FileOutputStream("AppData/login.conf", false)) {
				try (var oos = new ObjectOutputStream(fos)) {
					var lab = new LoginAttributes();
					lab.rememberMe = false;
					lab.username   = lab.password = "";
					oos.writeObject(lab);
					loginAttributes = lab;
				}
				return false;
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * A method to write the login values to file so that they can be used to automatically log in for future start ups
	 */
	private void writeLoginAttributesToFile() {
		try (var fos = new FileOutputStream("AppData/login.conf", false)) {
			try (var oos = new ObjectOutputStream(fos)) {
				LoginAttributes lab = new LoginAttributes();
				lab.username   = nameField.getText();
				lab.password   = passwordField.getText();
				lab.rememberMe = true;
				oos.writeObject(lab);
			}
		} catch (IOException e) {
			NetworkingUtilities.CreateErrorMessage("Cannot save Login Details",
					"Login credentials could not be saved" + "." + " Make sure the AppData directory" + "is writeable"
					, e.getMessage());
		}
	}
	//endregion
}