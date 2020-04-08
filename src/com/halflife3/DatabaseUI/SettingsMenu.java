package com.halflife3.DatabaseUI;

import com.halflife3.Controller.BaseController;
import com.halflife3.Controller.DatabaseManager;
import com.halflife3.Controller.Exceptions.SceneStackEmptyException;
import com.halflife3.Controller.SceneManager;
import com.halflife3.GameUI.MenuUtilitites;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.*;

public class SettingsMenu {
	private BorderPane borderPane;
	private VBox       vboxSettings;
	private VBox       vboxDeleteAccount;

	/*
	Set-up for core settings page
	 */
	private Button deleteRememberMe = new Button("Delete auto-login");
	private Button removeAcc        = new Button("Delete Account");
	private Button back             = new Button("Back");

	public Button getDeleteRememberMe() { return deleteRememberMe; }
	public Button getRemoveAcc() { return removeAcc; }
	public Button getBack() { return back; }

	/*
	Set-up for scene to allow user to delete account
	 */
	private Text		  passwordT		  = new Text();
	private PasswordField password        = new PasswordField();
	private Text		  confPasswordT   = new Text();
	private PasswordField confPassword    = new PasswordField();
	private Button        confDeleteAcc   = new Button("Delete Account");
	private Button        back2           = new Button("Back");
	private Text          userFeedback    = new Text();

	private Scene scene;

	private SceneManager manager = SceneManager.getInstance();

	public void start() {
		initialiseMenuScene(settingsVBox());
		manager.setScene("Settings", getScene());
	}

	private Background addBackground() {
		return MenuUtilitites.getBackground(getClass(), "res/Leaderboard/LeaderboardBackground.jpg");
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
		userFeedback.setStyle("-fx-font-size: 15pt;");
		userFeedback.setFill(Color.RED);
		vboxSettings = new VBox(deleteRememberMe, removeAcc, back, userFeedback);
		vboxSettings.setAlignment(Pos.BASELINE_CENTER);
		vboxSettings.setPadding(new Insets(50, 0, 0, 20));
		vboxSettings.setSpacing(60);
		vboxSettings.setStyle("-fx-background-color: rgba(176,224,230,0.8);");

		return vboxSettings;
	}

	private VBox removeAccVBox() {
		confDeleteAcc.setOnAction(actionEvent -> {
			if (!password.getText().equals(confPassword.getText())) {
				userFeedback.setText("Passwords do not match");
				setNullFields();
			} else {
				if (DatabaseManager.confirmUser(DatabaseManager.getConnection(), BaseController.GetApplicationUser().username, password.getText())) {
					deleteUserDetails(DatabaseManager.getConnection(), BaseController.GetApplicationUser().username);
					userFeedback.setFill(Color.GREEN);
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


		passwordT.setText("Password For User: " + BaseController.GetApplicationUser().username);
		passwordT.setStyle("-fx-font-size: 12pt; -fx-font-weight: bold;");
		passwordT.setFill(Color.BLUE);
		password.setMaxWidth(400);
		confPasswordT.setText("Confirm Password");
		confPasswordT.setStyle("-fx-font-size: 12pt; -fx-font-weight: bold;");
		confPasswordT.setFill(Color.BLUE);
		confPassword.setMaxWidth(400);
		userFeedback.setVisible(false);
		userFeedback.setFill(Color.RED);
		vboxDeleteAccount = new VBox(passwordT, password, confPasswordT, confPassword, confDeleteAcc, back2, userFeedback);
		vboxDeleteAccount.setAlignment(Pos.BASELINE_CENTER);
		vboxDeleteAccount.setPadding(new Insets(30, 0, 0, 30));
		vboxDeleteAccount.setSpacing(25);
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

		borderPane.setBackground(addBackground());
		borderPane.setCenter(scene);

		File f = new File("res/MainMenu/MainMenuCSS.css");
		borderPane.getStylesheets().add("file:///" + f.getAbsolutePath().replace("\\", "/"));

		return borderPane;
	}

	private void deleteUserDetails(Connection c, String username) {
		PreparedStatement deleteStatement = null;
		try {
			String deleteQuery = "DELETE FROM userdatascore WHERE \"name\" = '" + username + "'";
			deleteStatement = c.prepareStatement(deleteQuery);
			deleteStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DatabaseManager.closeConnections(c, deleteStatement, null);
		}
	}

	private boolean deleteLoginConf() {
		try {
			File f = new File("AppData/login.conf");
			if (f.delete()) {
				System.out.println(f.getName() + " deleted");
				return true;
			} else {
				System.out.println("failed");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	//Sets text fields to null
	public void setNullFields() {
		password.setText(null);
		confPassword.setText(null);
	}

	private void initialiseMenuScene(VBox sceneName) {
		scene = new Scene(borderPane(sceneName), 800, 600);
	}

	public Scene getScene() { return scene; }
}
