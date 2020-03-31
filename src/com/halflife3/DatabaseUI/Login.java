package com.halflife3.DatabaseUI;

import com.halflife3.GameUI.FirstMenu;
import com.halflife3.GameUI.LoginAttributes;
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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;

public class Login extends Preloader {

	Button login = new Button();

	Button createNewUser = new Button();

	Button        backButton        = new Button();
	Button        create            = new Button();
	Text          name              = new Text("Name");
	TextField     nameField         = new TextField();
	Text          password          = new Text("Password");
	PasswordField passwordField     = new PasswordField();
	Text          confPassword      = new Text("Confirm Password");
	PasswordField passwordFieldConf = new PasswordField();
	CheckBox      chbRememberMe     = new CheckBox("Remember me?");
	Text          incorrectFields   = new Text();

	private static Stage preloaderStage = null;

	private boolean         hasLoggedIn = false;
	private ICredentialUser user;
	private LoginAttributes loginAttributes;

	String mySecurePassword;
	String salt;

	/*
		This is used by the stackPanes for the two different scenes, Login and Create Account, to add an image to the background
	*/
	private Background addBackground () {
		try {
			FileInputStream inputStream = new FileInputStream("res/Login/login-background.jpg");
			Image           image       = new Image(inputStream);

			BackgroundSize  backgroundSize  = new BackgroundSize(800, 600, false, false, false, true);
			BackgroundImage backgroundImage = new BackgroundImage(image, BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize);
			Background      background      = new Background(backgroundImage);
			return background;
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		return null;
	}

	private void textProperties () {
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

	public void buttonProperties () throws FileNotFoundException {
		createNewUser.setText("Create New Account");
		login.setText("Login");
		backButton.setText("Back");
		create.setText("Create User");

        /*createNewUser.setMaxHeight(30);
        createNewUser.setMaxWidth(150);

        FileInputStream iSCreateNew = new FileInputStream("res/button_create-new-account (1).png");
        Image imageCreateNew = new Image(iSCreateNew, createNewUser.getWidth(), createNewUser.getHeight(), false, true);
        BackgroundImage cNImage = new BackgroundImage(imageCreateNew, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                new BackgroundSize(createNewUser.getWidth(), createNewUser.getHeight(), true, true, true, false));
        Background cB = new Background(cNImage);
        createNewUser.setBackground(cB);

        login.setMaxHeight(30);
        login.setMaxWidth(150);

        FileInputStream iSLogin = new FileInputStream("res/button_login (1).png");
        Image imageLogin = new Image(iSLogin, login.getWidth(), login.getHeight(), false, true);
        BackgroundImage bImageLogin = new BackgroundImage(imageLogin, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                new BackgroundSize(login.getWidth(), login.getHeight(), true, true, true, false));
        Background newBLogin = new Background(bImageLogin);
        login.setBackground(newBLogin);

        backButton.setMinHeight(30);
        backButton.setMinWidth(150);

        FileInputStream iSBack = new FileInputStream("res/button_back.png");
        Image imageBack = new Image(iSBack, login.getWidth(), login.getHeight(), false, true);
        BackgroundImage bBack = new BackgroundImage(imageBack, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                new BackgroundSize(backButton.getWidth(), backButton.getHeight(), true, true, true, false));
        Background newBBack = new Background(bBack);
        backButton.setBackground(newBBack);

        create.setMinHeight(30);
        create.setMinWidth(150);

        FileInputStream isCreate = new FileInputStream("res/button_create-user.png");
        Image imageCreate = new Image(isCreate, login.getWidth(), login.getHeight(), false, true);
        BackgroundImage bImageCreate = new BackgroundImage(imageCreate, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                new BackgroundSize(create.getWidth(), create.getHeight(), true, true, true, false));
        Background newBCreate = new Background(bImageCreate);
        create.setBackground(newBCreate);*/

	}

	private static GridPane basePane () {
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

	private static BorderPane baseBPane () {
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

	public BorderPane loginPane () {
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
		gridPaneLogin.add(chbRememberMe, 1, 3); //Remember Me?
		gridPaneLogin.add(incorrectFields, 0, 4);

        /*gridPaneLogin.setHalignment(login, HPos.CENTER);
        gridPaneLogin.setHalignment(createNewUser, HPos.CENTER);*/

		//Top, right, bottom, left
		gridPaneLogin.setPadding(new Insets(40, 0, 0, 0));
		gridPaneLogin.setStyle("-fx-background-color: rgba(176,224,230,0.8);");
		//gridPaneLogin.setMinSize(500, 400);

		borderPane.setBackground(addBackground());
		borderPane.setCenter(gridPaneLogin);

		File f = new File("res/Login/LoginStyleSheet.css");
		borderPane.getStylesheets().add("file:///" + f.getAbsolutePath().replace("\\", "/"));
		//incorrectFields.setStyle("-fx-font-family: 'Roboto Regular';");

		return borderPane;
	}

	public BorderPane newUserPane () {
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

		//Sets button alignment to the center
        /*gridPaneCreateUser.setHalignment(create, HPos.CENTER);
        gridPaneCreateUser.setHalignment(backButton, HPos.CENTER);*/

		//Top, right, bottom, left
		gridPaneCreateUser.setPadding(new Insets(40, 0, 0, 0));
		gridPaneCreateUser.setStyle("-fx-background-color: rgba(176,224,230,0.8);");
		gridPaneCreateUser.setMinSize(500, 400);

		newBorderPane.setBackground(addBackground());
		newBorderPane.setCenter(gridPaneCreateUser);

		File f = new File("res/Login/LoginStyleSheet.css");
		newBorderPane.getStylesheets().add("file:///" + f.getAbsolutePath().replace("\\", "/"));

		return newBorderPane;
	}

	private void mayBeHid () {
		if (hasLoggedIn) {
			user.setApplicationUser(nameField.getText());
			Platform.runLater(() -> preloaderStage.hide());
		}
	}

	@Override
	public void handleStateChangeNotification (StateChangeNotification info) {
		if (info.getType() == StateChangeNotification.Type.BEFORE_START) {
			user = (ICredentialUser) info.getApplication();
			if (getUserLoginAttributes()) {
				if(loginAttributes.rememberMe) {
					nameField.setText(loginAttributes.username);
					passwordField.setText(loginAttributes.password);
					hasLoggedIn = confirmUser(getConnection(), loginAttributes.username, loginAttributes.password);
				}
			}
			mayBeHid();
		}
	}

	private void initialiseFields () throws FileNotFoundException {
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

		//Sets text fields to null
		setNullFields();
	}

	@Override
	public void start (Stage stage) throws Exception {
		//Setting properties of buttons
		preloaderStage = stage;
		preloaderStage.setTitle("Login/Create User");

		initialiseFields();
		Scene sceneLogin = new Scene(loginPane(), 800, 600);
        /*File f = new File("res/Login/LoginStyleSheet.css");
        sceneLogin.getStylesheets().add("file:///" + f.getAbsolutePath().replace("\\", "/"));*/
		stage.setScene(sceneLogin);
		stage.show();

		//Setting on click events for login
		login.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle (ActionEvent e) {
				if (nameField.getText() != null && passwordField.getText() != null) {
					if (confirmUser(getConnection(), nameField.getText(), passwordField.getText())) {
						hasLoggedIn = true;
						if(chbRememberMe.isSelected())
						    writeLoginAttributesToFile();
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

		//This will change the scene to show the create new user stackpane
		createNewUser.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle (ActionEvent e) {
				setNullFields();
				incorrectFields.setVisible(false);
				Scene sceneCreate = new Scene(newUserPane(), 800, 600, Color.WHITE);
				stage.setScene(sceneCreate);
				stage.show();
			}
		});

		//NEED TO ACCOUNT FOR NON MATCHING PASSWORDS
		create.setOnAction(e -> {
			if (nameField.getText() != null && passwordField.getText() != null && passwordFieldConf.getText() != null) {
				if (doesUserExist(getConnection(), nameField.getText()) == true) {
					incorrectFields.setText("Username already exists. Choose another");
					incorrectFields.setVisible(true);
				} else if (!passwordField.getText().equals(passwordFieldConf.getText())) {
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
			} else {
				incorrectFields.setText("Please type in a username and password");
				incorrectFields.setVisible(true);
			}
		});

		//This will change the scene to show the login stackpane
		backButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle (ActionEvent e) {
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
						if (confirmUser(getConnection(), nameField.getText(), passwordField.getText())) {
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
					} else {
						incorrectFields.setText("Type in a username and password");
						incorrectFields.setVisible(true);

					}
				} else {
					if (nameField.getText() != null && passwordField.getText() != null && passwordFieldConf.getText() != null) {
						if (doesUserExist(getConnection(), nameField.getText()) == true) {
							incorrectFields.setText("That username already exists. Please choose another one");
							incorrectFields.setVisible(true);
						} else if (!passwordField.getText().equals(passwordFieldConf.getText())) {
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
					} else {
						incorrectFields.setText("Please type in a username and password");
						incorrectFields.setVisible(true);
					}
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

	//Returns true if a user exists in the database table and false if it doesn't
	public boolean doesUserExist (Connection c, String username) {
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
			closeConnections(c, preparedStatement, rs);
		}
		return false;
	}

	/*
	Verifying the user details are correct
	 */
	public boolean confirmUser (Connection c, String username, String passwordEntered) {

		boolean returnValue      = false;
		String  saltCheck        = "";
		String  securedPassword  = "";
		byte[]  securedPassword2 = new byte[0];

		PreparedStatement saltStatement     = null;
		ResultSet         rs                = null;
		PreparedStatement preparedStatement = null;
		ResultSet         rsDetails         = null;
		try {
			//Retrieving the salt from the table based on the username given
			//Creating the query
			String querySalt = "SELECT * FROM userdatascore WHERE \"name\" = '" + username + "'";
			//Creating the statement
			saltStatement = c.prepareStatement(querySalt);
			//Executing the query
			rs = saltStatement.executeQuery();
			while (rs.next()) {
				String testName = rs.getString(2);
				saltCheck        = rs.getString("salt");
				securedPassword2 = rs.getBytes("password");
				System.out.println("Salt retrieved: " + saltCheck + " name: " + testName + " password: " + new String(securedPassword2));
			}
			System.out.println("username passed: " + username + " password passed: " + passwordEntered);
			securedPassword = new String(securedPassword2);

            /*Creating the query
            String queryDetails = "SELECT * FROM userdatascore WHERE \"name\" = '" + username + "'";
            //Creating the statement
            preparedStatement = c.prepareStatement(queryDetails);
            //Executing the query
            rsDetails = preparedStatement.executeQuery();
            while (rsDetails.next()) {
                System.out.println("here");
                securedPassword = rs.getString(5);
                System.out.println("Password retrieved: " + securedPassword);
            }*/

			// Generating new secure password with the salt retrieved from the database associated with the username
			//String newSecurePassword = Password.generateSecurePassword(passwordEntered, saltCheck);

			// Check if two passwords are equal and returns true if they are
			//return returnValue = newSecurePassword.equalsIgnoreCase(securedPassword);

			//Code to check password while hashed password is broken
			System.out.println("Password entered: " + passwordEntered);
			System.out.println("SecuredPassword: " + securedPassword);
			return returnValue = securedPassword.equals(passwordEntered);

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeConnections(c, saltStatement, rs);
			closeConnections(null, preparedStatement, rsDetails);
		}
		return false;
	}

	//Adds new user information to the table
	public void addNewUser (Connection c, String username, String passwordEntered) {
		PreparedStatement preparedStatement = null;
		try {
			//Generating password and salt
			String saltAdd = Password.returnSalt();
			genPassword(passwordEntered, saltAdd);
			//Creating the query
			String query = "INSERT INTO userdatascore (name, score, salt, password) VALUES ('" + username + "', " + 0 + ", '" + "123" + "', '" + passwordEntered + "')"; //saltAdd changed to 123 and mySecurePassword changed to passwordEntered
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

	public void genPassword (String password, String salt) {
		String myPassword = password;

		// Protect user's password. The generated value can be stored in DB.
		mySecurePassword = Password.generateSecurePassword(myPassword, salt);

		// Print out protected password
		System.out.println("My secure password = " + mySecurePassword);
		System.out.println("Salt value = " + salt);
	}

	//Sets text fields to null
	public void setNullFields () {
		nameField.setText(null);
		passwordField.setText(null);
		passwordFieldConf.setText(null);
	}

	public boolean getUserLoginAttributes () {
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

	public void writeLoginAttributesToFile() {
	    try (var fos = new FileOutputStream("AppData/login.conf", false)) {
	        try (var oos = new ObjectOutputStream(fos)) {
                LoginAttributes lab = new LoginAttributes();
                lab.username = nameField.getText();
                lab.password = passwordField.getText();
                lab.rememberMe = true;
                oos.writeObject(lab);
            }
        } catch (IOException e) {
            NetworkingUtilities.CreateErrorMessage(
                    "Cannot save Login Details",
                    "Login credentials could not be saved. Make sure the AppData directory" +
                            "is writeable",
                    e.getMessage()
            );
        }
    }
}