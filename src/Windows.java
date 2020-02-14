import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
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
        VBox manuBar = main_menu.getBar();
        manuBar.setAlignment(Pos.CENTER);
        pane.getChildren().add(manuBar);
    }

    private void addBackground() throws FileNotFoundException {
        FileInputStream inputStream = new FileInputStream("res/button_image.png"); //change the backgraoud file plz
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
//        //turn to leader border
//        //FXMLLoader loader = new FXMLLoader(getClass().getResource("half-life.fxml"));  //use fxml for a new map
//        //BorderPane root = new BorderPane();
//        Parent root = loader.getRoot();
//        Button exit = new Button("Exit");
////        exit.setAlignment(Pos.CENTER);
////        exit.setOnAction(actionEvent -> Platform.exit());
//        Stage Newstage = new Stage();
//        try{
//            Newstage.setScene(new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT));
//        }catch(Exception e){
//            System.err.println("root is null");
//        }
//        Newstage.show();
//        root.requestFocus();
//        //root.getChildren().add(exit);
        //pane.getChildren().add(root);
        Leaderboard lboard = new Leaderboard();
        lboard.start(getpStage());
    }

    public void Update() {

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
