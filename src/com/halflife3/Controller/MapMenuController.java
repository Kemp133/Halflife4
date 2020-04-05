package com.halflife3.Controller;

import com.halflife3.Controller.Interfaces.IController;
import com.halflife3.GameUI.MainMenu;
import com.halflife3.GameUI.Maps;
import javafx.scene.Scene;

public class MapMenuController implements IController {
    private Maps menu;
    private MainMenu m_menu;
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
