package com.halflife3.View;

import com.halflife3.GameObjects.Bricks;
import com.halflife3.GameUI.Maps;
import com.halflife3.Mechanics.Vector2;
import javafx.scene.canvas.*;
import javafx.scene.image.*;
import javafx.scene.paint.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.Deque;

public class MapRender {
	private static      Deque<Bricks> Bricks_list;
	public static final int           BLOCK_SIZE = 40;

	public static Deque<Bricks> GetList() {
		return Bricks_list;
	}

	public static void Render(GraphicsContext gc) {
		for (Bricks bricks : Bricks_list)
			bricks.render(gc);
	}

	public static void LoadLevel() {
		try {
			Bricks_list = new ArrayDeque<>();
			Image       mapImage    = new Image(new FileInputStream(Maps.Map));
			PixelReader pixelReader = mapImage.getPixelReader();
			Vector2     zero        = new Vector2();

			for (int x = 0; x < mapImage.getWidth(); x++) {
				for (int y = 0; y < mapImage.getHeight(); y++) {
					if (pixelReader.getColor(x, y).equals(Color.BLACK)) {
						Vector2 position  = new Vector2(x * BLOCK_SIZE, y * BLOCK_SIZE);
						Bricks  new_Brick = new Bricks(position, zero);
						Bricks_list.add(new_Brick);
					}
				}
			}
		} catch (FileNotFoundException e) { e.printStackTrace(); }
	}
}