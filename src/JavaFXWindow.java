import com.halflife3.Controller.*;
import com.halflife3.Model.*;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.Set;

import static javafx.scene.input.KeyCode.*;
import static javafx.scene.input.KeyCode.S;

public class JavaFXWindow extends Application {

    static Canvas canvas;
    static Input input = new Input();
    static ObjectManager om = new ObjectManager();

    @Override
    public void start(Stage stage) throws Exception {
        canvas = new Canvas(800, 800);
        StackPane root = new StackPane();
        root.getChildren().add(canvas);
        Scene scene = new Scene(root, 800, 800);
        scene.addEventHandler(KeyEvent.ANY, new KeyboardInput(input));
        scene.addEventHandler(MouseEvent.ANY, new MouseInput(input));
        stage.setTitle("This is a window!");
        stage.setScene(scene);

        GraphicsContext gc = canvas.getGraphicsContext2D();

        Player Player1 = new Player(new Vector2(100,100), new Vector2(), (short)0, om);
        //set the image for player, need to change the
        Player1.setImage("file:res/Player_pic.png");

        Player Player2 = new Player(new Vector2(200, 200), new Vector2(), (short)0, om);
        Player2.setImage("file:res/Player_pic.png");

        //main update.
        final long[] startNanoTime = {System.nanoTime()};

//        Set<GameObject> gos = new HashSet<>(); //Replaced by the ObjectManager
//        gos.add(Player1);
//        gos.add(Player2);
        new Bullet(new Vector2( 150, 150), new Vector2(0, 0), (short)0, om);

        new AnimationTimer() {
            public void handle(long currentNanoTime) {
                // calculate time since last update.
                double elapsedTime = (currentNanoTime - startNanoTime[0]) / 1000000000.0;
                startNanoTime[0] = currentNanoTime;

                // game logic
                if (input.isKeyPressed(A))
                    Player1.addVelocity(new Vector2(-1, 0).multiply(Player1.getMoveSpeed()));
                if (input.isKeyPressed(D))
                    Player1.addVelocity(new Vector2(1, 0).multiply(Player1.getMoveSpeed()));
                if (input.isKeyPressed(W))
                    Player1.addVelocity(new Vector2(0, -1).multiply(Player1.getMoveSpeed()));
                if (input.isKeyPressed(S))
                    Player1.addVelocity(new Vector2(0, 1).multiply(Player1.getMoveSpeed()));
                if(input.isKeyPressed(SPACE)) {
                    Bullet bullet = new Bullet(Player1.getPosition(), Player1.getVelocity(), (short)0, om);
                }

                if (input.isKeyPressed(LEFT))
                    Player2.addVelocity(new Vector2(-150, 0));
                if (input.isKeyPressed(RIGHT))
                    Player2.addVelocity(new Vector2(150, 0));
                if (input.isKeyPressed(UP))
                    Player2.addVelocity(new Vector2(0, -150));
                if (input.isKeyPressed(DOWN))
                    Player2.addVelocity(new Vector2(0, 150));


//                Player1.update(elapsedTime);
//                Player2.update(elapsedTime);

                for(IUpdateable go : om.getGameObjects()) {
                    go.update(elapsedTime);
                }
                // TODO: collision detection

                // render
                gc.clearRect(0, 0, 800, 800);

                for(IRenderable go : om.getGameObjects()) {
                    go.render(gc);
                }


//                Player1.render(gc);
//                Player2.render(gc);
                input.resetValues();
            }
        }.start();

        root.requestFocus();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
