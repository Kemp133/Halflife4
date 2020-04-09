package com.halflife3.GameUI;

import java.io.Serializable;

public class WindowAttributes implements Serializable {
	public String  title;
	public boolean resizeable;
	public boolean maximisedOnLoad;
	public boolean fullScreenOnLoad;
	public boolean decorated;
	public boolean isModal;
	public String  iconPath;

	public static int GAME_WINDOW_WIDTH  = 800;
	public static int GAME_WINDOW_HEIGHT = 600;
}