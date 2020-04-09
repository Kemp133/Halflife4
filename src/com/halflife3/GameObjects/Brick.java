package com.halflife3.GameObjects;

import com.halflife3.Controller.ObjectManager;
import com.halflife3.Mechanics.Vector2;
import com.halflife3.View.Camera;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Rectangle;

import java.io.File;
import java.util.*;

/**
 *
 */
public class Brick extends Sprite {
    public Rectangle rectangle;

    public Brick(Vector2 position) {
        super(position, new Vector2());
        keys.add("Brick");
        //Bricks should only be created in the MapRender class,
        // so remove them from the ObjectManager to stop them polluting the object pool
        ObjectManager.removeObject(this);
        ArrayList<String> blockNames = pathList();
        setSprite(blockNames.get((new Random()).nextInt(blockNames.size())));
        rectangle = new Rectangle(position.getX(), position.getY(), getWidth(), getHeight());
    }

    @Override
    public Rectangle getBounds() {
        return rectangle;
    }

    @Override
    public void setSprite(String file){
        super.setSprite(file);
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.drawImage(sprite, position.getX() - Camera.GetOffset().getX(),
                position.getY() - Camera.GetOffset().getY());
    }

    @Override
    public void update(double time) {}

    private ArrayList<String> pathList() {
        ArrayList<String> paths = new ArrayList<>();

        File directory = new File("res\\Sprites\\Bricks");
        int fileCount = Objects.requireNonNull(directory.list()).length;

        for (int i = 1; i <= fileCount; i++)
            paths.add(String.format("res/Sprites/Bricks/block%d.png", i));

        return paths;
    }
}
