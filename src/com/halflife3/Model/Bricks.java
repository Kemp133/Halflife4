package com.halflife3.Model;

import com.halflife3.Controller.ObjectManager;
import com.halflife3.View.Camera;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Rectangle;

public class Bricks extends Sprite {
    public Rectangle rectangle;

    public Bricks(Vector2 position, Vector2 velocity, short rotation) {
        super(position,velocity,rotation);
        keys.add("Bricks");
        //Bricks should only be created in the MapRender class, so remove them from the ObjectManager to stop them polluting the object pool
        ObjectManager.removeObject(this);
        setSprite("res/block.png");
        rectangle = new Rectangle(position.getX(), position.getY(), getWidth(), getHeight());
    }

    @Override
    public Rectangle getBounds() {
        return rectangle;
    }

    @Override
    public boolean intersects(GameObject s) {
        return false;
    }

    @Override
    public void setSprite(String file){
        super.setSprite(file);
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.drawImage(sprite, position.getX()- Camera.GetOffset().getX() , position.getY() - Camera.GetOffset().getY());
    }

    @Override
    public void update(double time) {
        //TODO: If bullet touches the brick and the brick can be destroyed, remove brick
    }

    //region Get Width and Height
    public double getWidth() { return sprite.getWidth(); }
    public double getHeight() { return sprite.getHeight(); }
    //endregion
}
