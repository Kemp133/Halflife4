/*package com.halflife3.GameUI;

import com.halflife3.Model.Player;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class IpAndScore extends Application {
    private String clientIP;
    private int score;
    private Player player;
    private Pane root;

    public IpAndScore(Player player){
        this.player = player;
        this.clientIP = player.getIpOfClient();
        this.root = new Pane();
        root.getChildren().add(new Text("player Ip is:" + this.getClientIP()));
        //this.score = player.getScore();
        Task<Void> task = new Task<Void>(){
            @Override
            public Void call() throws Exception {

            }
        };
    }

    public String getClientIP() {
        return clientIP;
    }

    @Override
    public void start(Stage stage) throws Exception {

    }
}*/
