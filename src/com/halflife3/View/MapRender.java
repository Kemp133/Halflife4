package com.halflife3.View;

import com.halflife3.Controller.ObjectManager;
import com.halflife3.Model.Bricks;
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

    MapRender(){}

    public void SetMap(String filename) throws FileNotFoundException {
        FileInputStream inputted = new FileInputStream(filename);
        Image image = new Image(inputted);
        this.image = image;
    }

    public Deque<Bricks> get_list(){
        return Bricks_list;
    }

    public void render(GraphicsContext gc){
        Iterator<Bricks> it = Bricks_list.iterator();
        while(it.hasNext()){
            it.next().render(gc);
        }
    }

    public void loadLevel(ObjectManager om) throws FileNotFoundException {
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
                    Bricks new_Brick = new Bricks(new Vector2((xx) * 40, (yy) * 40), new Vector2(0, 0), (short) 0,om);
                    new_Brick.setImage("res/block.png");
                    Bricks_list.add(new_Brick);
                }
            }
        }

    }
}
