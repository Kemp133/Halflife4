package com.halflife3.Controller;

import com.halflife3.Controller.Exceptions.SceneStackEmptyException;
import com.halflife3.DatabaseUI.SettingsMenu;
import com.halflife3.Networking.NetworkingUtilities;
import javafx.application.*;
import javafx.scene.paint.*;
import javafx.stage.*;

import java.io.File;
import java.sql.*;

public class OptionsController extends BaseController {
	SettingsMenu menu;

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
		menu.getDeleteRememberMe().setOnAction(actionEvent -> {
			if (deleteLoginConf()) {
				menu.getUserFeedback().setText("Auto-login removed");
			} else {
				menu.getUserFeedback().setText("Remember me has not been used");
			}
			menu.getUserFeedback().setVisible(true);
		});

		menu.getRemoveAcc().setOnAction(actionEvent -> {
			manager.setScene("Settings Delete Account", menu.getRemoveAccountScene());
		});

		menu.getBack().setOnAction(actionEvent -> {
			try {
				manager.restorePreviousScene();
			} catch (SceneStackEmptyException e) {
				e.printStackTrace();
			}
		});
	}

	public void setRemoveAccountSceneButtons() {
		menu.getConfDeleteAcc().setOnAction(actionEvent -> {
			if (!menu.getPassword().getText().equals(menu.getConfPassword().getText())) {
				menu.getUserFeedback().setText("Passwords do not match");
				setNullFields();
			} else {
				if (DatabaseManager.confirmUser(DatabaseManager.getConnection(),
						BaseController.GetApplicationUser().username, menu.getPassword().getText())) {
					manager.setScene("Account Deleted", menu.getRemoveAccountScene());
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
				manager.restorePreviousScene();
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
				menu.getUserFeedback().setText("Automatic login data deleted!");
				System.out.println(f.getName() + " deleted");
				return true;
			} else {
				menu.getUserFeedback().setText("Failed to delete automatic login data!");
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
