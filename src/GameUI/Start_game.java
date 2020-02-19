package GameUI;

import com.halflife3.Networking.Client.MainClient;
import javafx.application.Platform;
import javafx.stage.Stage;

public class Start_game {
    private static Windows stage;
    public Start_game(Windows stage){
        this.stage = stage;
    }

    public static void main(String[] args) {
        stage.stop();
        Platform.runLater(
                new Runnable(){
                    public void run(){
                        MainClient.main(null);
                }
        }
        );
    }
}
