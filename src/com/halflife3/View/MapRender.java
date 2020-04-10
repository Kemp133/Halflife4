package com.halflife3.View;

import com.halflife3.GameObjects.Brick;
import com.halflife3.GameObjects.Goal;
import com.halflife3.GameUI.Maps;
import com.halflife3.Mechanics.PowerUps.FastReload;
import com.halflife3.Mechanics.PowerUps.Speedup;
import com.halflife3.Mechanics.Vector2;
import javafx.scene.canvas.*;
import javafx.scene.image.*;
import javafx.scene.paint.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;

public class MapRender {
	private static      Deque<Brick>        Bricks_list;
	private static      HashSet<Speedup>    Speedup_list;
	private static      HashSet<FastReload> FastReload_list;
	private static      Deque<Goal>         goalZone;
	public static final int                 BLOCK_SIZE = 40;
	private static      Vector2             ballSpawnPos;
	private static      ArrayList<Vector2>  startPositions;
	public static       int                 mapWidth   = 0;
	public static       int                 mapHeight  = 0;

	public static Deque<Brick> GetList() {
		return Bricks_list;
	}

	public static void Render(GraphicsContext gc) {
		for (Brick brick : Bricks_list)
			brick.render(gc);
	}

	public synchronized static void LoadLevel() {
		if (mapWidth != 0 && mapHeight != 0) { return; } //To stop loading the map twice when hosting and playing
		try {
			Bricks_list     = new ArrayDeque<>();
			goalZone        = new ArrayDeque<>();
			startPositions  = new ArrayList<>();
			Speedup_list    = new HashSet<>();
			FastReload_list = new HashSet<>();
			Image mapImage = new Image(new FileInputStream(Maps.Map));
			mapWidth  = (int) mapImage.getWidth() * 40;
			mapHeight = (int) mapImage.getHeight() * 40;

			PixelReader pixelReader = mapImage.getPixelReader();

			for (int x = 0; x < mapImage.getWidth(); x++) {
				for (int y = 0; y < mapImage.getHeight(); y++) {
					if (pixelReader.getColor(x, y).equals(Color.BLACK)) {
						Vector2 position  = new Vector2(x * BLOCK_SIZE, y * BLOCK_SIZE);
						Brick   new_Brick = new Brick(position);
						Bricks_list.add(new_Brick);
					} else if (pixelReader.getColor(x, y).equals(Color.RED)) {
						ballSpawnPos = new Vector2((x + 0.5) * BLOCK_SIZE, (y + 0.5) * BLOCK_SIZE);
					} else if (pixelReader.getColor(x, y).equals(Color.rgb(155, 255, 155))) {
						goalZone.add(new Goal(x * BLOCK_SIZE, y * BLOCK_SIZE));
					} else if (pixelReader.getColor(x, y).equals(Color.BLUE)) {
						startPositions.add(new Vector2(x * BLOCK_SIZE, y * BLOCK_SIZE));
					} else if (pixelReader.getColor(x, y).equals(Color.rgb(255, 255, 0))) {
						Speedup_list.add(new Speedup(new Vector2(x * BLOCK_SIZE, y * BLOCK_SIZE),
								"res/boosts/speedBoost.png"));
					} else if (pixelReader.getColor(x, y).equals(Color.rgb(128, 0, 128))) {
						FastReload_list.add(new FastReload(new Vector2(x * BLOCK_SIZE, y * BLOCK_SIZE),
								"res/boosts/damageBoost.png"));
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

	public static Deque<Goal> getGoalZone() {
		return goalZone;
	}

	public static HashSet<Speedup> getSpeedup_list() {
		return Speedup_list;
	}

	public static HashSet<FastReload> getFastReload_list() {
		return FastReload_list;
	}
}