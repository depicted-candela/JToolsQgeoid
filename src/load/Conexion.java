/**
 * 
 */
package load;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.*; 
import java.util.*; 
import java.lang.*; 
import org.postgis.*;
import org.postgresql.util.PGobject; 

/**
 * 
 */
public class Conexion {

	/**
	 * @throws ClassNotFoundException 
	 * 
	 */
	@SuppressWarnings("unchecked")
	public Conexion(String url, String user, String password) throws ClassNotFoundException {
		
        try {
            // Attempting to establish a connection to the database
        	Class.forName("org.postgresql.Driver"); 
            conn = DriverManager.getConnection(url, user, password);
             /* Add the geometry types to the connection. Note that you 
             * must cast the connection to the pgsql-specific connection 
             * implementation before calling the addDataType() method. 
             */
             ((org.postgresql.PGConnection)conn).addDataType("geometry",(Class<? extends PGobject>) Class.forName("org.postgis.PGgeometry"));
             ((org.postgresql.PGConnection)conn).addDataType("box3d",(Class<? extends PGobject>) Class.forName("org.postgis.PGbox3d"));
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
