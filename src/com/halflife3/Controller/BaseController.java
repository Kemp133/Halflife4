package com.halflife3.Controller;

import com.halflife3.Controller.Interfaces.IController;
import com.halflife3.GameUI.ApplicationUser;
import javafx.application.Application;

public abstract class BaseController extends Application implements IController {
	protected static SceneManager manager;
	protected static ApplicationUser user;

	public static ApplicationUser GetApplicationUser() { return user; }
}