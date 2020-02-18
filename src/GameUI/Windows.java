package GameUI;/*This is the main menu after
log-in successfully to the database*/

import com.halflife3.View.Main;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/*The class shows game manu after log-in successfully
* Function include:
* new game;load game
* audio setings*/
public class Windows extends Application {

    private String filepng = null;
    private StackPane pane = new StackPane();
    private LinMenu main_menu = LinMenu.getInstance();
    private Stage pStage = null;

    public Windows() throws FileNotFoundException {
    }

    private static final double SCREEN_WIDTH = Screen.getPrimary().getBounds().getWidth();
    private static final double SCREEN_HEIGHT = Screen.getPrimary().getBounds().getHeight();


    private void addMenu() throws IOException {
        main_menu.getStartItem().setOnAction(actionEvent -> {
            try {
                entergameStage();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        main_menu.getLoadItem().setOnAction(actionEvent->{
            loadgame();
        });
        VBox manuBar = main_menu.getBar();
        manuBar.setAlignment(Pos.CENTER);
        pane.getChildren().add(manuBar);
    }

    //load game from sql
    private void loadgame() {

    }

    private void addBackground() throws FileNotFoundException {
        FileInputStream inputStream = new FileInputStream("res/windows-backgroud.jpg"); //change the backgraoud file plz
        Image image = new Image(inputStream);

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(SCREEN_WIDTH);
        imageView.setFitHeight(SCREEN_HEIGHT);

        Pane backgroud = new Pane(imageView);

        pane.getChildren().add(backgroud);
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
            primaryStage.setResizable(true);
            pStage = primaryStage;
            Scene scene = new Scene(createContent(), SCREEN_WIDTH, SCREEN_HEIGHT, Color.WHITE);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        launch(args);
    }

    public void entergameStage() throws Exception {
        new Main().start(pStage);
        pStage.centerOnScreen();
    }

    public void Update() {
        //get ther

    }


    private Stage getpStage() {
        try {
            return this.pStage;
        } catch (Exception e) {
            System.err.println("primary stage not built!");
        }
        return null;
    }
}
