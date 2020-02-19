package com.halflife3.View;

import com.halflife3.Controller.ObjectManager;
import com.halflife3.Model.Bricks;
import com.halflife3.Model.MeleeEnemy;
import com.halflife3.Model.Vector2;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.Deque;

public class MapRender {

    private Image image;
    private Deque<Bricks> Bricks_list = new ArrayDeque<>();
    private ObjectManager om;


    public MapRender(ObjectManager om) {
        this.om = om;
    }

    public void SetMap(String mapPNG) throws FileNotFoundException {
        image = new Image(new FileInputStream(mapPNG));
    }

    public Deque<Bricks> get_list(){
        return Bricks_list;
    }


    public void render(GraphicsContext gc) {
        for (Bricks bricks : Bricks_list) {
            bricks.render(gc);
        }
    }

    public void loadLevel() throws FileNotFoundException {
        double width = image.getWidth(); //20px
        double height = image.getHeight(); //15px
        PixelReader pixelReader = image.getPixelReader();

        int enemyID = 0;

        for (int xx = 0; xx < width; xx++) {
            for (int yy = 0; yy < height; yy++) {
                javafx.scene.paint.Color color = pixelReader.getColor(xx,yy);
                if (color.equals(Color.BLACK)) { // pixels.getColor(xx, yy) == Color.BLACK
                    Vector2 position = new Vector2((xx) * 40, (yy) * 40);
                    Vector2 velocity = new Vector2(0, 0);
                    Bricks new_Brick = new Bricks(position, velocity, (short) 0, om);
                    new_Brick.setImage("res/block.png");
                    Bricks_list.add(new_Brick);
                }

            }
        }
    }
}
