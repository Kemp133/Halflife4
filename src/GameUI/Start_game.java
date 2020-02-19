package GameUI;

import com.halflife3.GameUI.Windows;
import com.halflife3.Networking.Client.MainClient;
import javafx.stage.Stage;

public class Start_game {
    private static Windows stage;
    public Start_game(Windows stage){
        this.stage = stage;
    }

    public static void main(String[] args) throws Exception {
        stage.stop();
        new Thread(() -> MainClient.main(null)).start();
    }
}
