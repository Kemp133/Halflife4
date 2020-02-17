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
import java.util.*;

public class MapRender {

    Image image;
    private Deque<Bricks> Bricks_list = new ArrayDeque<>();
    private ObjectManager om;
    private Deque<MeleeEnemy> Melee_list = new ArrayDeque<>();

    public MapRender(ObjectManager om) {
        this.om = om;
    }

    public void SetMap(String filename) throws FileNotFoundException {
        FileInputStream inputted = new FileInputStream(filename);
        image = new Image(inputted); //Removed unnecessary Image reference assignment
    }

    public Deque<Bricks> get_list(){
        return Bricks_list;
    }
    public Deque<MeleeEnemy> getMelee_list(){ return Melee_list; }

    public void render(GraphicsContext gc){
        for (Bricks bricks : Bricks_list) { //Replaced with for loop
            bricks.render(gc);
        }
        for (MeleeEnemy meleeEnemy : Melee_list){
            meleeEnemy.render(gc);
        }
    }

    public void loadLevel() throws FileNotFoundException {
        double w = image.getWidth();
        double h = image.getHeight();
        PixelReader pr = image.getPixelReader();
        for (int xx = 0; xx < w; xx++) {
            for (int yy = 0; yy < h; yy++) {
                int pixel = pr.getArgb(xx, yy);
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = (pixel) & 0xff;
                if (blue == 0 && green == 0 && red == 0) {

                    Bricks new_Brick = new Bricks(new Vector2((xx) * 40, (yy) * 40), new Vector2(0, 0), (short) 0, om,0);
                    new_Brick.setImage("res/block.png");
                    Bricks_list.add(new_Brick);
                }
                if (blue == 0 && green == 0 && red != 0) {

                    MeleeEnemy new_MeleeEnemy = new MeleeEnemy(new Vector2 ((xx) * 32, (yy) * 32) , new Vector2(0, 0), (short) 0, om,1 );
                    Melee_list.add(new_MeleeEnemy);
                }
            }
        }

    }
}
