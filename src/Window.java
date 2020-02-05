import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Window extends Application {

    Stage window;
    BorderPane layout;

    private StackPane pane = new StackPane();

    private static final double SCREEN_WIDTH = Screen.getPrimary().getBounds().getWidth();
    private static final double SCREEN_HEIGHT = Screen.getPrimary().getBounds().getHeight();



    private void addButtons() throws FileNotFoundException {
        //Button newGButton = new Button("New Game");
        //Button optButton = new Button("Options");
        //Button quit = new Button("Quit");
        //newGButton.setLayoutX(250);
        //newGButton.setLayoutY(220);
        //newGButton.setCenterShape(true);
        //pane.getChildren().addAll(newGButton, optButton, quit);

        //Adds an image to a button
        FileInputStream input = new FileInputStream("res/button_image.png");
        Image image = new Image(input);
        ImageView imageView = new ImageView(image);

        Button button = new Button("New Game", imageView);
        //

        //ImageView imageView = new ImageView(new Image(getClass().getResource("/res/button_image.png").toExternalForm()));
        //Button testButton = new Button("", imageView);

        pane.getChildren().add(button);
    }
    private void addBackground() throws FileNotFoundException {
        /*ImageView imageView = new ImageView(new Image(getClass().getResource("res/markus-henze-4vVZ8N88Ygs-unsplash.jpg").toExternalForm()));
        imageView.setFitWidth(1280);
        imageView.setFitHeight(720);

        pane.getChildren().add(imageView);*/

        FileInputStream inputStream = new FileInputStream("res/markus-henze-4vVZ8N88Ygs-unsplash.jpg");
        Image image = new Image(inputStream);

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(SCREEN_WIDTH);
        imageView.setFitHeight(SCREEN_HEIGHT);

        pane.getChildren().add(imageView);
    }

    private StackPane createContent() throws FileNotFoundException {
        addBackground();
        addButtons();

        return pane;
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            primaryStage.setTitle("Team HalfLife");
            addBackground();
            addButtons();
            Scene scene = new Scene(pane, SCREEN_WIDTH, SCREEN_HEIGHT, Color.WHITE);
            //primaryStage.setScene(new Scene(pane, 500, 500, Color.BLACK));
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public static void main (String[] args) {
        launch(args);
    }

    /*public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Team HalfLife");
        newGButton = new Button("New Game");
        optButton = new Button("Options");
        quit = new Button("Quit");

        StackPane stack = new StackPane();
        stack.getChildren().addAll(newGButton);

        //Group root = new Group();
        Scene s = new Scene(stack, 300, 300);

        primaryStage.setScene(s);
        primaryStage.show();
        /*Rectangle r = new Rectangle(25,25,250,250);
        r.setFill(Color.BLUE);

        root.getChildren().add(r);*/

        /*StackPane stack = new StackPane();
        stack.getChildren().addAll(new Rectangle(100,100,Color.BLUE), newGButton, optButton, quit);
    }*/
}
