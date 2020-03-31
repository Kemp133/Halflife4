package com.halflife3.View;

import com.halflife3.Mechanics.GameObjects.Bricks;
import com.halflife3.Mechanics.Vector2;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.Deque;

public class MapRender {
    private static Deque<Bricks> Bricks_list = new ArrayDeque<>();

    public static Deque<Bricks> GetList() {
        return Bricks_list;
    }

    public static void Render(GraphicsContext gc) {
        for (Bricks bricks : Bricks_list)
            bricks.render(gc);
    }

    public static void LoadLevel() {
        try {
            Image mapImage = new Image(new FileInputStream("res/map.png"));
            PixelReader pixelReader = mapImage.getPixelReader();
            Vector2 zero = new Vector2(0, 0);

            for (int x = 0; x < mapImage.getWidth(); x++) {
                for (int y = 0; y < mapImage.getHeight(); y++) {
                    if (pixelReader.getColor(x, y).equals(Color.BLACK)) {
                        Vector2 position = new Vector2(x * 40, y * 40);
                        Bricks new_Brick = new Bricks(position, zero);
                        Bricks_list.add(new_Brick);
                    }
                }
            }
        } catch (FileNotFoundException e) { System.out.println("Could not find file 'res/map.png'"); }
    }
}