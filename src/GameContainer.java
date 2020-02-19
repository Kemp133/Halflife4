/*
import com.halflife3.GameUI.Windows;

import java.io.FileNotFoundException;

public class GameContainer extends Windows implements Runnable{
    private Thread thread;
    //private GameUI.Windows window;
    private boolean running = false;
    private final double UPDATE_CAP = 1.0/60.0;
    private int width = 320, height = 240;
    private float scale = 3f;
    private String title = "Half-Life 3 v1.0";

    public GameContainer() throws FileNotFoundException {
        super();
    }


    public void start() {
        thread = new Thread(this);
       thread.start();
    }

    public void stop() {

    }

    public void run() {
        running = true;

        boolean render = false;
        double firstTime = 0;
        double lastTime = System.nanoTime() / 1e9;
        double passedTime = 0;
        double unprocessedTime = 0;

        double frameTime = 0;
        int frames = 0;
        int fps = 0;

        while (running) {
            render = false;
            firstTime = System.nanoTime() / 1e9;
            passedTime = firstTime - lastTime;
            lastTime = firstTime;
            unprocessedTime += passedTime;

            // Extra stuff for testing
            frameTime += passedTime;

            while (unprocessedTime >= UPDATE_CAP) {
                unprocessedTime -= UPDATE_CAP;
                render = true;

                //TODO: Update game
                if (frameTime >= 1.0) {
                    frameTime = 0;
                    fps = frames;
                    frames = 0;
                    System.out.println("FPS: " + fps);
                }
            }

            if (render) {
                //TODO: Render game
                this.Update();
                frames++;
            } else {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        dispose();
    }

    public void dispose() {

    }

    public static void main(String[] args) throws FileNotFoundException {
        GameContainer gc = new GameContainer();
        gc.start();
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
*/
