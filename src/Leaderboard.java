import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.*;

public class Leaderboard extends Application {
    //Stage window;
    //TableView<Product> table;

    private ObservableList<ObservableList> data;
    private TableView tableview;

    public void getData() throws SQLException {
        Connection c = null;
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/Leaderboard", "postgres", "123");

            if (c != null) {
                System.out.println("Connected successfully");
            } else {
                System.out.println("Connection failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }

        //Creating the Statement
        Statement stmt = c.createStatement();
        String query = "Select * from \"TopLeaderboard\" where playername = 'Joe'";
        //Executing the query
        ResultSet rs = stmt.executeQuery(query);
        //Retrieving ResultSetMetaData object
        ResultSetMetaData rsMetaData = rs.getMetaData();
        System.out.println("Column name: " + rsMetaData.getColumnName(1));
        System.out.println("Column name: " + rsMetaData.getColumnName(2));
        if (rs.next() == false) {
            System.out.println("no match");
        } else {
            System.out.println("match");
        }
        while (rs.next()) {

            System.out.print(rs.getString(1));
            System.out.print(": ");
            System.out.println(rs.getInt(2));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        tableview = new TableView();
        getData();

        Scene scene = new Scene(tableview);

        stage.setScene(scene);
        stage.show();
    }
}
