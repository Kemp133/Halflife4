package GameUI;

import GameUI.ServerDisplay;
import com.halflife3.DatabaseUI.Login;
import com.halflife3.Networking.Server.Server;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static javafx.scene.paint.Color.WHITE;

//The menu set for choose server and client
public class FirstMenu extends Application {
    private Stage pstage = null;
    private static final double SCREEN_WIDTH = 800;
    private static final double SCREEN_HEIGHT = 600;
    StackPane root = new StackPane();
    private Button client_m;
    private Button server_m;
    private Text text = new Text("Please choose one");

    public FirstMenu() throws FileNotFoundException {
        FileInputStream input = new FileInputStream("res/button_image.png");
        javafx.scene.image.Image image = new Image(input);

        client_m = new Button("Client",new ImageView(image));
        server_m = new Button("Server",new ImageView(image));

        server_m.setOnAction(e->{
            //jump to server main class
            try {
                runServer();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        client_m.setOnAction(e->{
            //jump to run client class
            //load the login page
            try {
                new Login().start(pstage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void runServer() throws Exception {
        new ServerDisplay().start(pstage);
        pstage.centerOnScreen();
        Server myServer = new Server();
        myServer.run();
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.setPstage(stage);
        stage.setTitle("Server/client menu");
        stage.setScene(new Scene(createContent(), SCREEN_WIDTH, SCREEN_HEIGHT, WHITE));
        stage.show();
    }

    public StackPane createContent(){
        VBox pane = new VBox(100);
        pane.getChildren().addAll(text,server_m,client_m);
        pane.setAlignment(Pos.CENTER);
        root.getChildren().add(pane);
        return root;
    }

    public Stage getPstage() {
        return pstage;
    }

    public void setPstage(Stage pstage) {
        this.pstage = pstage;
    }
}
