package com.halflife3.View;

import com.halflife3.Model.Bricks;
import com.halflife3.Model.Vector2;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

import java.io.FileInputStream;
import java.util.ArrayDeque;
import java.util.Deque;

public class MapRender {
    private static Deque<Bricks> Bricks_list = new ArrayDeque<>();
    private static final int MIN_MAP_HEIGHT = 15;
    private static final int MIN_MAP_WIDTH = 20;

    public MapRender() {}

    public static Deque<Bricks> get_list(){
        return Bricks_list;
    }

    public static void render(GraphicsContext gc) {
        for (Bricks bricks : Bricks_list) {
            bricks.render(gc);
        }
    }

    public static void loadLevel(String pathToMapFile) throws Exception {
        Image mapImage;
        try (var fis = new FileInputStream(pathToMapFile)) {
            mapImage = new Image(fis);
        }

        double width = mapImage.getWidth(); //20px
        double height = mapImage.getHeight(); //15px
        
        if(width == 0 || height == 0 || height < MIN_MAP_HEIGHT || width < MIN_MAP_WIDTH)
            throw new Exception("Error! Image file is malformed! Minimum dimensions are 20 pixels wide by 15 pixels tall");
        
        PixelReader pixelReader = mapImage.getPixelReader();

        for (int xx = 0; xx < width; xx++) {
            for (int yy = 0; yy < height; yy++) {
                Color color = pixelReader.getColor(xx,yy);
                if (color.equals(Color.BLACK)) { // pixels.getColor(xx, yy) == Color.BLACK
                    Vector2 position = new Vector2((xx) * 40, (yy) * 40);
                    Vector2 velocity = new Vector2(0, 0);
                    Bricks new_Brick = new Bricks(position.subtract(Camera.GetOffset()), velocity, (short) 0);
                    new_Brick.setSprite("res/block.png");
                    Bricks_list.add(new_Brick);
                }
            }
        }
    }
}