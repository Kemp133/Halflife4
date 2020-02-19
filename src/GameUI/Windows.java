package GameUI;
/*This is the main menu after
log-in successfully to the database*/

import com.halflife3.Networking.Client.ClientGame;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
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
    static Stage game_stage = new Stage();

    public Windows() throws FileNotFoundException {
    }

    public StackPane getPane(){
        return this.pane;
    }

    private static final double SCREEN_WIDTH = 800;
    private static final double SCREEN_HEIGHT = 600;

    //load game from sql
    private void loadgame() {

    }

    private void addBackground() throws FileNotFoundException {
        FileInputStream inputStream = new FileInputStream("res/first_menu.png"); //change the backgraoud file plz
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
        setpStage(primaryStage);
        try {
            primaryStage.setTitle("Team HalfLife");
            primaryStage.setResizable(true);
            Scene scene = new Scene(createContent(), SCREEN_WIDTH, SCREEN_HEIGHT, Color.WHITE);
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    @Override
    public void init(){

    }

    public static void main(String[] args) throws FileNotFoundException {
       launch();
    }



    private Stage getpStage() {
        try {
            return this.pStage;
        } catch (Exception e) {
            System.err.println("primary stage not built!");
        }
        return null;
    }

    public void setpStage(Stage stage) {
        this.pStage = stage;
    }

    @Override
        public void stop(){
           Platform.exit();
    }

    private void addMenu() throws IOException {
        main_menu.getStartItem().setOnAction(actionEvent -> {
            try {
                main_menu.Mute();
                new ClientGame(getpStage()).getStarted();
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
}
