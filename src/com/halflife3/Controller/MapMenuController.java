package com.halflife3.Controller;

import com.halflife3.Controller.Interfaces.IController;
import com.halflife3.GameUI.*;

/**
 * This class acts as the controller class for the map selection menu. This allows the user to select which map they want to play when they
 * load the game, and then return to the main menu to host the game
 */
public class MapMenuController implements IController {
	/** A private reference to the {@code Maps} class which is used to show the available maps */
	private Maps         menu;
	/** A private reference to {@code MainMenu} to recreate the main menu */
	private MainMenu     m_menu;
	/** A private reference to {@code SceneManager} so that getInstance() doesn't need to be called every time it's needed*/
	private SceneManager manager = SceneManager.getInstance();
	@Override
	public void initialise() {}

	@Override
	public void start() {
		menu = new Maps();
		manager.setScene("Maps", menu.getScene());
	}

	@Override
	public void end() {
		m_menu = new MainMenu();
		manager.setScene("Main Menu", m_menu.getScene());
		menu = null;
	}

	@Override
	public void run() {
		start();
	}
}
