//import com.halflife3.Controller.*;
//import com.halflife3.Model.*;
//import javafx.animation.AnimationTimer;
//import javafx.application.Application;
//import javafx.scene.*;
//import javafx.scene.Scene;
//import javafx.scene.canvas.Canvas;
//import javafx.scene.canvas.GraphicsContext;
//import javafx.scene.input.KeyEvent;
//import javafx.scene.input.MouseButton;
//import javafx.scene.input.MouseEvent;
//import javafx.scene.layout.StackPane;
//import javafx.stage.Stage;
//
//import static javafx.scene.input.KeyCode.*;
//
//public class JavaFXWindow extends Application {
//
//    static Canvas canvas;
//    static Input input = new Input();
//    static ObjectManager om = new ObjectManager();
//
//    @Override
//    public void start(Stage stage) throws Exception {
//        canvas = new Canvas(800, 800);
//        StackPane root = new StackPane();
//        root.getChildren().add(canvas);
//        Scene scene = new Scene(root, 800, 800);
//        scene.addEventHandler(KeyEvent.ANY, new KeyboardInput(input));
//        scene.addEventHandler(MouseEvent.ANY, new MouseInput(input));
//        scene.setCursor(Cursor.NONE);
//        stage.setTitle("This is a window!");
//        stage.setScene(scene);
//
//        GraphicsContext gc = canvas.getGraphicsContext2D();
//
//        Player Player1 = new Player(new Vector2(100,100), new Vector2(), (short)0, om);
//        //set the image for player, need to change the
//        Player1.setImage("res/Player_pic.png");
//
//        Player Player2 = new Player(new Vector2(200, 200), new Vector2(), (short)0, om);
//        Player2.setImage("res/Player_pic.png");
//
//        Crosshair cursor = new Crosshair(input.mousePosition, new Vector2(0,0), (short)0, om, input);
//
//        //main update.
//        final long[] startNanoTime = {System.nanoTime()};
//
//        new AnimationTimer() {
//            public void handle(long currentNanoTime) {
//                // calculate time since last update.
//                double elapsedTime = (currentNanoTime - startNanoTime[0]) / 1000000000.0;
//                startNanoTime[0] = currentNanoTime;
//
//                // game logic
//                if (input.isKeyPressed(A))
//                    Player1.addVelocity(new Vector2(-1, 0).multiply(Player1.getMoveSpeed()));
//                if (input.isKeyPressed(D))
//                    Player1.addVelocity(new Vector2(1, 0).multiply(Player1.getMoveSpeed()));
//                if (input.isKeyPressed(W))
//                    Player1.addVelocity(new Vector2(0, -1).multiply(Player1.getMoveSpeed()));
//                if (input.isKeyPressed(S))
//                    Player1.addVelocity(new Vector2(0, 1).multiply(Player1.getMoveSpeed()));
//                if(input.isKeyPressed(SPACE)) {
//                    Bullet bullet = new Bullet(Player1.getPosition(), Player1.getVelocity(), (short)0, om);
//                }
//
//                if (input.isKeyPressed(LEFT))
//                    Player2.addVelocity(new Vector2(-150, 0));
//                if (input.isKeyPressed(RIGHT))
//                    Player2.addVelocity(new Vector2(150, 0));
//                if (input.isKeyPressed(UP))
//                    Player2.addVelocity(new Vector2(0, -150));
//                if (input.isKeyPressed(DOWN))
//                    Player2.addVelocity(new Vector2(0, 150));
//
//                if(input.isKeyPressed(C)) {
//                    om.getGameObjects().removeIf(go -> go.containsKey("Bullet"));
//                }
//
//                if(input.mouseButtonPressed.get(MouseButton.PRIMARY)) {
//                    Bullet bullet = new Bullet(new Vector2(Player1.getX(), Player1.getY()), new Vector2(input.mousePosition.getX(), input.mousePosition.getY()).subtract(Player1.getPosition()), (short)0, om);
//                }
//
//                for(IUpdateable go : om.getGameObjects()) {
//                    go.update(elapsedTime);
//                }
//                // TODO: collision detection
//
//                // render
//                gc.clearRect(0, 0, 800, 800);
//
//                for(IRenderable go : om.getGameObjects()) {
//                    go.render(gc);
//                }
//
//                for(GameObject go : om.getGameObjects()) {
//                    go.setVelocity(new Vector2(0,0));
//                }
//                input.resetValues();
//            }
//        }.start();
//
//        root.requestFocus();
//        stage.show();
//    }
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//}
