import com.halflife3.Controller.ObjectManager;
import com.halflife3.Model.MeleeEnemy;
import com.halflife3.Model.Player;
import com.halflife3.Model.Vector2;
import com.halflife3.View.MapRender;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.FileInputStream;

public class test extends Application {
    private Pane root = new Pane();
    private ObjectManager objectManager = new ObjectManager();
    private Player player = new Player(new Vector2(100, 100), new Vector2(0, 0), (short) 0, objectManager);

    private MeleeEnemy melee = new MeleeEnemy(new Vector2(150,150), new Vector2(0, 0),(short) 0 ,objectManager);


    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("HalfLife 3");
        Canvas canvas = new Canvas(800, 600);
        root.getChildren().add(canvas);
        Scene scene = new Scene(createContent(), 800, 600);
        primaryStage.setScene(scene);

        /*
          Set the background
         */
        FileInputStream inputted = new FileInputStream("res/background_image.png");
        Image image = new Image(inputted, 40, 40, true, true);
        BackgroundImage myBI = new BackgroundImage(image,
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);
        root.setBackground(new Background(myBI));

        //set the image for player, need to change the
        player.setImage("res/Player_pic.png");

        //set the image for meleeEnemy
        melee.setImage("res/pixil-frame-0.png");

        //Set the graphic tool for canvas
        GraphicsContext gc = canvas.getGraphicsContext2D();


        MapRender map = new MapRender(objectManager);
        map.SetMap("res/mapAndEnemy.png");
        map.loadLevel();

        map.render(gc);
        root.requestFocus();
        primaryStage.show();

    }

    private Parent createContent() {
        return root;
    }
    }
