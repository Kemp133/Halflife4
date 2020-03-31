import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Database {

    public static void main() {
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

        try {
            PreparedStatement pst = c.prepareStatement("SELECT * FROM \"TopLeaderboard\""); //or leaderboard?
            if (pst != null) {
                System.out.println("Success");
            } else {
                System.out.println("Failed");
            }
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {

                System.out.print(rs.getString(1));
                System.out.print(": ");
                System.out.println(rs.getInt(2));
            }

        } catch (SQLException ex) {

            Logger lgr = Logger.getLogger(Database.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
}
