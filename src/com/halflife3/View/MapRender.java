package com.halflife3.View;

import com.halflife3.Controller.ObjectManager;
import com.halflife3.Model.Bricks;
import com.halflife3.Model.MeleeEnemy;
import com.halflife3.Model.Vector2;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.Deque;

public class MapRender {

    private Image image;
    private Deque<Bricks> Bricks_list = new ArrayDeque<>();
    private ObjectManager om;
    private Deque<MeleeEnemy> Melee_list = new ArrayDeque<>();

    public MapRender(ObjectManager om) {
        this.om = om;
    }

    public void SetMap(String mapPNG) throws FileNotFoundException {
        image = new Image(new FileInputStream(mapPNG));
    }

    public Deque<Bricks> get_list(){
        return Bricks_list;
    }
    public Deque<MeleeEnemy> getMelee_list(){ return Melee_list; }

    public void render(GraphicsContext gc) {
        for (Bricks bricks : Bricks_list) {
            bricks.render(gc);
        }
        for (MeleeEnemy meleeEnemy : Melee_list){
            meleeEnemy.render(gc);
        }
    }

    public void loadLevel() throws FileNotFoundException {
        double width = image.getWidth(); //20px
        double height = image.getHeight(); //15px
        PixelReader pixelReader = image.getPixelReader();

        int enemyID = 0;

        for (int xx = 0; xx < width; xx++) {
            for (int yy = 0; yy < height; yy++) {
                int pixels = pixelReader.getArgb(xx, yy);
                int red = (pixels >> 16) & 0xff;
                int green = (pixels >> 8) & 0xff;
                int blue = (pixels) & 0xff;

                if (blue == 0 && green == 0 && red == 0) { // pixels.getColor(xx, yy) == Color.BLACK
                    Vector2 position = new Vector2((xx) * 40, (yy) * 40);
                    Vector2 velocity = new Vector2(0, 0);
                    Bricks new_Brick = new Bricks(position, velocity, (short) 0, om);
                    new_Brick.setImage("res/block.png");
                    Bricks_list.add(new_Brick);
                }/*
                if (blue == 0 && green == 0 && red != 0) {
                    Vector2 position = new Vector2((xx) * 32, (yy) * 32);
                    Vector2 velocity = new Vector2(0, 0);
                    MeleeEnemy new_MeleeEnemy = new MeleeEnemy(position, velocity, (short) 0, om, enemyID);
                    enemyID++;
                    Melee_list.add(new_MeleeEnemy);
                }*/
            }
        }
    }
}
