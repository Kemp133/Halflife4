package com.halflife3.GameUI;

import com.halflife3.Networking.NetworkingUtilities;
import javafx.scene.image.*;
import javafx.scene.layout.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * This class hopes to encapsulate common methods found in a lot of views. This is to reduce the amount of code reuse,
 * and to further tidy up the classes as these methods are declared once in here and then all you ever need in other
 * classes is this method call.
 */
public class MenuUtilitites {

	/**
	 * A method which takes a class reference and a string of the background location and returns a background object.
	 * The class reference is used to show in the popup window exactly which class the error originated in
	 *
	 * @param from The class where this method was called from. Used to make the error more informative
	 * @param backgroundLocation The string with the location of the image to use as the background
	 * @return A background image showing the image pointed to by {@code backgroundLocation}
	 */
	public static Background getBackground(Class<?> from, String backgroundLocation) {
		try (var fis = new FileInputStream(backgroundLocation)) {
			Image img = new Image(fis);
			var   bs  = new BackgroundSize(800, 600, false, false, false, true);
			var   bi  = new BackgroundImage(
					img, BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, bs);
			return new Background(bi);
		} catch (FileNotFoundException e) {
			NetworkingUtilities.CreateErrorMessage(
					"Background image could not be found!",
					"The desired background image could not be loaded! Check your path to make sure it is correct",
					"Origin class: " + from.getName() + "Message: " + e.getMessage()
			);
		} catch (IOException ignored) {}
		return null;
	}

}
