package com.halflife3.DatabaseUI;

import com.halflife3.Controller.SceneManager;
import com.halflife3.Networking.NetworkingUtilities;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.*;

//TODO: Add button to be able to exit

public class Leaderboard {
    private ObservableList<User> data;
    private TableView tableView;
    private BorderPane borderPane;
    private Pane paneRight;
    private Pane paneLeft;
    private Pane paneBottom;
    private Button exit = new Button("Back to Main Menu");

    public Leaderboard() {
                /*
        Font properties
         */
        Font paladinFont = Font.loadFont(getClass().getClassLoader().getResourceAsStream("Font/PaladinsSemiItalic.otf"), 40);

        /*
        Label properties
         */
        Label title = new Label("Leaderboard");
        title.setFont(paladinFont);
        title.setStyle("-fx-text-fill: linear-gradient(#0000FF, #FFFFFF 95%);");

        /*
        Pane properties
         */
        paneRight = new Pane();
        paneRight.setMinSize(100, 600);

        paneLeft = new Pane();
        paneLeft.setMinSize(100, 600);


        paneBottom = new Pane();
        paneBottom.setMinSize(600, 80);

        /*
        TableView properties
         */
        tableView = new TableView();
        TableColumn nameColumn = new TableColumn("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn scoreColumn = new TableColumn("Score");
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("Score"));

        tableView.getColumns().addAll(nameColumn, scoreColumn);

        try {
            tableView.setItems(getTableData());
        } catch (SQLException e) {
            NetworkingUtilities.CreateErrorMessage(
                    "SQL Error Occured",
                    "A SQL error occured",
                    "State: " + e.getSQLState() + "\n" + "Message" + e.getMessage()
            );
        }

        //tableView.setPadding(new Insets(0, 0, 10, 0));

        //Distributes column space evenly
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        /*
        BorderPane properties
         */
        Insets insets = new Insets(30);
        borderPane = new BorderPane();
        borderPane.setMaxSize(800, 600);
        borderPane.setMinSize(800, 600);
        borderPane.setCenter(tableView);
        borderPane.setTop(title);
        BorderPane.setMargin(title, insets);
        borderPane.setRight(paneRight);
        borderPane.setLeft(paneLeft);
        borderPane.setAlignment(title, Pos.TOP_CENTER);
        borderPane.setBottom(paneBottom);
        borderPane.setBackground(addBackground());
    }

    public Scene getLeaderboardScene(int width, int height) {
        Scene scene = new Scene(borderPane, width, height);
        scene.getStylesheets().add(getClass().getClassLoader().getResource("Leaderboard/LeaderboardStyleSheet.css").toExternalForm());
        return scene;
    }

    public ResultSet getData() throws SQLException {
        Connection c = null;
        PreparedStatement preparedStatement = null;
        try {
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://rogue.db.elephantsql.com:5432/nuzmlzpr";
            url = url.trim();
            c = DriverManager.getConnection(url, "nuzmlzpr", "pd7OdC_3BiVrAPNU68CETtFtBaqFxJFB");

            //Creating the query
            String query = "Select * from userdatascore order by score desc";
            //Creating the Statement
            preparedStatement = c.prepareStatement(query);
            //Executing the query
            return preparedStatement.executeQuery();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        } finally {
            //closeConnections(c, preparedStatement, null); //TODO: Fix - Not working currently - Seems to remove the result set somehow as well, maybe something to do with closing the prepared statement(more than likely)
            c.close();
        }
        return null;
    }

    private ObservableList getTableData() throws SQLException {
        data = FXCollections.observableArrayList();
        ResultSet rs = getData();
        int i = 0;

        while (rs.next() && i < 10) {
            User user = new User(rs.getString(2), rs.getInt(3));
            data.add(user);
            i++;
        }
        return data;
    }

    private Background addBackground() {
        try {
            Image image = new Image(getClass().getClassLoader().getResourceAsStream("Leaderboard/LeaderboardBackground.jpg"));
            BackgroundSize backgroundSize = new BackgroundSize(800, 600, false, false, false, true);
            BackgroundImage backgroundImage = new BackgroundImage(image, BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize);
            return new Background(backgroundImage);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return null;
    }


    public Scene returnScene(){
        /*
        Font properties
         */
        Font paladinFont = Font.loadFont(getClass().getClassLoader().getResourceAsStream("Font/PaladinsSemiItalic.otf"), 40);


        /*
        Label properties
         */
        Label title = new Label("Leaderboard");
        title.setFont(paladinFont);
        title.setStyle("-fx-text-fill: linear-gradient(#0000FF, #FFFFFF 95%);");

        /*
        Pane properties
         */
        paneRight = new Pane();
        paneRight.setMinSize(100, 600);

        paneLeft = new Pane();
        paneLeft.setMinSize(100, 600);

        exit.setOnAction(actionEvent -> {
            try {
                SceneManager.getInstance().restorePreviousScene(); //Can't call this inside of animation timer
            } catch(Exception e) {
                NetworkingUtilities.CreateErrorMessage(
                        "Scene Stack Empty!",
                        "SceneManager threw an exception",
                        "Message" + e.getMessage()
                );
            }
        });

        VBox vb = new VBox(exit);
        vb.setAlignment(Pos.BASELINE_CENTER);
        vb.setMinSize(600,80);
        /*
        TableView properties
         */
        tableView = new TableView();
        TableColumn nameColumn = new TableColumn("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn scoreColumn = new TableColumn("Score");
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("Score"));

        tableView.getColumns().addAll(nameColumn, scoreColumn);

        try{
            tableView.setItems(getTableData());
        }catch (Exception e){
            e.printStackTrace();
        }

        //tableView.setPadding(new Insets(0, 0, 10, 0));

        //Distributes column space evenly
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        /*
        BorderPane properties
         */
        Insets insets = new Insets(30);
        borderPane = new BorderPane();
        borderPane.setMaxSize(800, 600);
        borderPane.setMinSize(800, 600);
        borderPane.setCenter(tableView);
        borderPane.setTop(title);
        BorderPane.setMargin(title, insets);
        borderPane.setRight(paneRight);
        borderPane.setLeft(paneLeft);
        borderPane.setAlignment(title, Pos.TOP_CENTER);
        borderPane.setBottom(vb);
        borderPane.setBackground(addBackground());

        Scene scene = new Scene(borderPane);

        File f = new File("Leaderboard/LeaderboardStyleSheet.css");
        scene.getStylesheets().add(getClass().getClassLoader().getResource("Leaderboard/LeaderboardStyleSheet.css").toExternalForm());

        return scene;
    }

    //TODO: May not be needed before prototype if issue not fixed
    public void closeConnections(Connection c, PreparedStatement p, ResultSet r) {
        if (c != null) {
            try {
                c.close();
            } catch (SQLException e) { /* ignored */}
        }
        if (p != null) {
            try {
                p.close();
            } catch (SQLException e) { /* ignored */}
        }
        if (r != null) {
            try {
                r.close();
            } catch (SQLException e) { /* ignored */}
        }
    }
}
