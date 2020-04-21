package com.halflife3.Controller;

import com.halflife3.Controller.Exceptions.SceneStackEmptyException;
import com.halflife3.DatabaseUI.SettingsMenu;
import com.halflife3.Networking.NetworkingUtilities;
import javafx.application.*;
import javafx.scene.media.*;
import javafx.stage.*;

import java.io.File;
import java.sql.*;

public class OptionsController extends BaseController {
	SettingsMenu menu;
	MediaPlayer  player;

	public OptionsController(MediaPlayer player) {
		this.player = player;
	}

	@Override
	public void initialise() {
		menu = new SettingsMenu();
	}
	@Override
	public void start() {
		initialise();
		setMainSceneButtons();
		setRemoveAccountSceneButtons();
		SceneManager.getInstance().setScene("Settings", menu.getMainScene());
	}

	public void setMainSceneButtons() {
		//region Set Delete Remember Me OnAction Event
		menu.getDeleteRememberMe().setOnAction(actionEvent -> {
			if (deleteLoginConf())
				menu.getDeleteRememberMe().setDisable(true);
			menu.getUserFeedback().setVisible(true);
		});
		//endregion

		//region Set Remove Account OnAction Event
		menu.getRemoveAcc().setOnAction(actionEvent -> {
			SceneManager.getInstance().setScene("Settings Delete Account", menu.getRemoveAccountScene());
		});
		//endregion

		//region Set Back Button OnAction Event
		menu.getBack().setOnAction(actionEvent -> {
			try {
				SceneManager.getInstance().restorePreviousScene();
			} catch (SceneStackEmptyException e) {
				e.printStackTrace();
			}
		});
		//endregion

		//region Add Mute Volume Button OnAction Event
		menu.getMuteVolumeButton().setOnAction(actionEvent -> {
			player.setMute(!player.isMute());
			menu.getMuteVolumeButton().setText(player.isMute() ? "Un-Mute" : "Mute");
		});
		//endregion

		//region Initialize Volume Slider
		menu.getVolumeSlider().setValue(player.getVolume());
		menu.getVolumeSlider().setOnMouseReleased(mouseEvent -> player.setVolume(menu.getVolumeSlider().getValue()));
		//endregion
	}

	public void setRemoveAccountSceneButtons() {
		menu.getConfDeleteAcc().setOnAction(actionEvent -> {
			if (!menu.getPassword().getText().equals(menu.getConfPassword().getText())) {
				menu.getUserFeedback().setText("Passwords do not match");
				setNullFields();
			} else {
				if (DatabaseManager.confirmUser(DatabaseManager.getConnection(), BaseController.GetApplicationUser().username,
						menu.getPassword().getText())) {

					SceneManager.getInstance().setScene("Account Deleted", menu.getAccountDeletedScene());
					deleteUserDetails(DatabaseManager.getConnection(), BaseController.GetApplicationUser().username);
					deleteLoginConf();

					Platform.runLater(() -> {
						NetworkingUtilities.WaitXSeconds(5);
						closeApplication();
					});
				} else {
					menu.getUserFeedback().setText("Password is incorrect");
					setNullFields();
				}
			}
			menu.getUserFeedback().setVisible(true);
		});

		menu.getBack2().setOnAction(actionEvent -> {
			try {
				SceneManager.getInstance().restorePreviousScene();
			} catch (SceneStackEmptyException e) {
				e.printStackTrace();
			}
		});
	}

	//Sets text fields to null
	public void setNullFields() {
		menu.getPassword().setText(null);
		menu.getConfPassword().setText(null);
	}

	public void resetUserFeedback() {
		menu.getUserFeedback().setText(null);
		menu.getUserFeedback().setVisible(false);
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

	//region Not Needed
	@Override
	public void end() {}
	@Override
	public void run() {}
	@Override
	public void start(Stage stage) {} //not needed
	//endregion
}
