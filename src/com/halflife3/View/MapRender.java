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
import java.util.ArrayList;
import java.util.Deque;

public class MapRender {
	private static      Deque<Bricks>      Bricks_list;
	public static final int                BLOCK_SIZE = 40;
	private static      Vector2            ballSpawnPos;
	private static      ArrayList<Vector2> startPositions;
	public static       int                mapWidth;
	public static       int                mapHeight;

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
            startPositions = new ArrayList<>();
			Image mapImage = new Image(new FileInputStream(Maps.Map));
			mapWidth  = (int) mapImage.getWidth() * 40;
			mapHeight = (int) mapImage.getHeight() * 40;

			PixelReader pixelReader = mapImage.getPixelReader();
			Vector2     zero        = new Vector2();

			for (int x = 0; x < mapImage.getWidth(); x++) {
				for (int y = 0; y < mapImage.getHeight(); y++) {
					if (pixelReader.getColor(x, y).equals(Color.BLACK)) {
						Vector2 position  = new Vector2(x * BLOCK_SIZE, y * BLOCK_SIZE);
						Bricks  new_Brick = new Bricks(position, zero);
						Bricks_list.add(new_Brick);
					} else if (pixelReader.getColor(x, y).equals(Color.RED)) {
						ballSpawnPos = new Vector2((x + 0.5) * BLOCK_SIZE, (y + 0.5) * BLOCK_SIZE);
					} else if (pixelReader.getColor(x, y).equals(Color.BLUE)) {
					    startPositions.add(new Vector2(x * BLOCK_SIZE, y * BLOCK_SIZE));
                    }
				}
			}

			if (ballSpawnPos == null) { ballSpawnPos = new Vector2(mapWidth / 2f, mapHeight / 2f); }
		} catch (FileNotFoundException e) { e.printStackTrace(); }
	}

	public static Vector2 getBallSpawnPos() {
		return new Vector2(ballSpawnPos);
	}

    public static Vector2[] getStartPositions() {
        return startPositions.toArray(new Vector2[0]);
    }
}