import java.sql.*;

public class JdbcTest {
    // Adding 'throws Exception' lets us skip try-catch blocks
    public static void main(String[] args) throws Exception {
        
        // 1. Define your connection details
        String url = "jdbc:mysql://localhost:3306/lab_practice";
        String user = "root";
        String password = "NION_551@";

        // 2. Load the MySQL Driver
        Class.forName("com.mysql.cj.jdbc.Driver");

        // 3. Establish the connection
        Connection con = DriverManager.getConnection(url, user, password);

        // 4. Create a statement to talk to the DB
        Statement stmt = con.createStatement();

        // 5. Execute a simple query (assuming you have a table named 'users')
        ResultSet rs = stmt.executeQuery("SELECT * FROM employee");

        // 6. Print the data
        while (rs.next()) {
            System.out.println(rs.getString(1));
            System.out.println(rs.getString(2));
            System.out.println(rs.getString(3));
            System.out.println(rs.getString(4)); // Prints the first column
        }

        // 7. Close the connection
        con.close();
        
        System.out.println("Process finished successfully!");
    }
    
}