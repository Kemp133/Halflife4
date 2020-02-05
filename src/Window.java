import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.util.Collection;

public class Window extends Application {

    Stage window;
    BorderPane layout;

    private void addButtons() {
        Button newGButton = new Button("New Game");
        Button optButton = new Button("Options");
        Button quit = new Button("Quit");
        newGButton.setLayoutX(250);
        newGButton.setLayoutY(220);
        StackPane root = new StackPane();
        root.getChildren().add(newGButton);
    }

    public static void main (String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
       // Scene scene = new Scene(createContent());
        primaryStage.setTitle("Team HalfLife");
        primaryStage.setScene(new Scene(root, 300, 300));
        primaryStage.show();
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
