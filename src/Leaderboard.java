import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.*;

public class Leaderboard extends Application {
    private ObservableList<ObservableList> data;
    private TableView tableview;

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

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        tableview = new TableView();
        ResultSet rs = getData();

        TableColumn nameColumn = new TableColumn("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn scoreColumn = new TableColumn("Score");
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("Score"));

        tableview.getColumns().addAll(nameColumn, scoreColumn);

        while (rs.next()) {
            User user = new User(rs.getString(2), rs.getInt(3));
            tableview.getItems().add(user);
        }

        Scene scene = new Scene(tableview);

        stage.setScene(scene);
        stage.show();
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
