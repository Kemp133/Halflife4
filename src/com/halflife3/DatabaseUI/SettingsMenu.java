package com.halflife3.DatabaseUI;

import com.halflife3.Controller.BaseController;
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

import java.io.*;

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
	public Button getRemoveAcc()        { return removeAcc; }
	public Button getBack()             { return back; }

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

	public Text getUserFeedback() { return userFeedback; }
	public PasswordField getPassword() { return password; }
	public PasswordField getConfPassword() { return confPassword; }
	public Button getConfDeleteAcc() { return confDeleteAcc; }
	public Button getBack2() { return back2; }

	private Scene mainScene;
	private Scene removeAccountScene;
	private Scene accountDeletedScene;

	public SettingsMenu() {
		mainScene = createSceneContext(settingsVBox());
		removeAccountScene = createSceneContext(removeAccVBox());
		accountDeletedScene = createSceneContext(accountDeletedVbox());
	}

	private Background addBackground() {
		return MenuUtilitites.getBackground(getClass(), "res/Leaderboard/LeaderboardBackground.jpg");
	}

	private VBox settingsVBox() {
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

	private VBox accountDeletedVbox() {
		Text headerText = new Text("Your account has been deleted successfully");
		Text bodyText = new Text("The game window will close automatically in 5 seconds");
		headerText.setStyle("-fx-font-size: 25pt;");
		headerText.setFill(Color.GREEN);
		bodyText.setStyle("-fx-font-size: 15pt;");
		bodyText.setFill(Color.GREEN);
		VBox vBox = new VBox(headerText, bodyText);
		vBox.setAlignment(Pos.CENTER);
		vBox.setStyle("-fx-background-color: rgba(176,224,230,0.8)");
		return vBox;
	}

	private VBox removeAccVBox() {
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

	private Scene createSceneContext(VBox content) { return new Scene(borderPane(content), 800,600); }

	public Scene getMainScene() { return mainScene; }
	public Scene getRemoveAccountScene() { return removeAccountScene; }
}
