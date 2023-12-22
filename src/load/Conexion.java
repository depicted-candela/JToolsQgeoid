/**
 * 
 */
package load;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 
 */
public class Conexion {

	/**
	 * 
	 */
	public Conexion(String url, String user, String password) {
		
        try {
            // Attempting to establish a connection to the database
            conn = DriverManager.getConnection(url, user, password);
            if (conn != null) {
            	conn.setAutoCommit(false);
                System.out.println("Conectado a postgres satisfactoriamente!");
                return;
            } else {
                System.out.println("Revise los parámetros de conexión a la base de datos!");
            }

//            conn.close(); // Close connection after use

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
	}

	public Connection conn;
	
}
