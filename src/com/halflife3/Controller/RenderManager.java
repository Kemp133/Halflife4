package com.halflife3.Controller;

import com.halflife3.GameUI.WindowAttributes;
import com.halflife3.View.MapRender;
import javafx.scene.canvas.*;

public final class RenderManager {
	private static RenderManager instance;

	public void render(GraphicsContext canvas) {
		canvas.clearRect(0, 0, WindowAttributes.GAME_WINDOW_WIDTH, WindowAttributes.GAME_WINDOW_HEIGHT);
		MapRender.Render(canvas);
//		renderObjects.forEach((obj) -> obj.render(canvas));
		ObjectManager.getGameObjects().forEach(obj -> obj.render(canvas));
	}

	public static RenderManager getInstance() {
		if(instance == null)
			instance = new RenderManager();
		return instance;
	}
}