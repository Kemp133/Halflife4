import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

import javax.imageio.stream.FileImageInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Windows extends Application {

    /*Stage window;
    BorderPane layout;*/ // Not needed for now
    private String filepng = null;
    private StackPane pane = new StackPane();

    public Windows(){
    }

    private static final double SCREEN_WIDTH = Screen.getPrimary().getBounds().getWidth();
    private static final double SCREEN_HEIGHT = Screen.getPrimary().getBounds().getHeight();


   /* private void addButtons() throws FileNotFoundException {
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
    }*/

    private void addMenu() throws IOException {
        LinMenu main_menu = LinMenu.getInstance();

        FileInputStream input = new FileInputStream("res/button_image.png");
        Image image = new Image(input);
        ImageView imageview = new ImageView(image);
        MenuButton button = new MenuButton("Settings", imageview, main_menu.getItems().get(0),main_menu.getItems().get(1),main_menu.getItems().get(2),main_menu.getItems().get(3));
        button.setLayoutX(250);
        button.setLayoutY(200);
      /*  MenuButton button_stat = new MenuButton("Start",imageview,main_menu.getItems().get(0));
        MenuButton button_set = new MenuButton("Settings",imageview,main_menu.getItems().get(1));
        MenuButton button_exit = new MenuButton("Exit",imageview,main_menu.getItems().get(2));

        I have problems with button lay_out
        */

        /*hbox2.setLayoutY(hbox1.getWidth());
        hbox1.setLayoutX(hbox1.getHeight());*/

        pane.getChildren().add(button);

    }
    private void addBackground() throws FileNotFoundException {
        FileInputStream inputStream = new FileInputStream("res/button_image.png"); //change the backgraoud file plz
        Image image = new Image(inputStream);

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(SCREEN_WIDTH);
        imageView.setFitHeight(SCREEN_HEIGHT);

        pane.getChildren().add(imageView);
    }

    private StackPane createContent() throws FileNotFoundException {
        addBackground();
        try {
            addMenu();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return pane;
    }

    @Override
    public void start(Stage primaryStage) {
        try {

            primaryStage.setTitle("Team HalfLife");
            Scene scene = new Scene(createContent(), SCREEN_WIDTH, SCREEN_HEIGHT, Color.WHITE);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }



    public static void main (String[] args) {
        launch(args);
    }

    public void Update() {
        //What to update? I am confused
    }
}
