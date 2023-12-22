package load;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

public class ConectarCargar {

	/**
	 * 
	 */
	public ConectarCargar(String url, String user, String password, String tipo, Estandarizar est) {
		conectar(url, user, password, tipo, est);
	}
	
	private void conectar(String url, String user, String password, String tipo, Estandarizar est) {
        try {
            // Attempting to establish a connection to the database
            conn = DriverManager.getConnection(url, user, password);
            if (conn != null) {
                System.out.println("Conectado a postgres satisfactoriamente!");
                constructorCargue(tipo, est);
                conn.setAutoCommit(false);
                return;
            } else {
                System.out.println("Revise los parámetros de conexión a la base de datos!");
            }

            conn.close(); // Close connection after use

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
	}
	
	private void constructorCargue(String tipo, Estandarizar est) {
		System.out.println(tipo);
		if (tipo.equals("aerogravimetria")) {
			cargarAerogravimetria(est);
		}
	}
	
	private void cargarAerogravimetria(Estandarizar est) {
		
		try {
		    // Disable auto-commit for batch operations
		    String sql = "SELECT COUNT(*) FROM your_table"; // Replace with your specific query
		    try (Statement stmt = conn.createStatement();
		         ResultSet rs 	= stmt.executeQuery(sql)) {
		    	System.out.println("E");
		        if (rs.next()) {
		        	System.out.println("F");
		            int count = rs.getInt(1); // The count is in the first column of the ResultSet
		            System.out.println("G");
		            System.out.println("Number of rows: " + count);
		        }
		        System.out.println("H");
		    } catch (SQLException ex) {
		    	System.out.println("I");
		        System.out.println("Error occurred: " + ex.getMessage());
		        ex.printStackTrace();
		        System.out.println("J");
		    }
		    
//		    // Insert into the first table
//		    String sql1 = "INSERT INTO public.estandarizar_proyectos (column1, column2) VALUES (?, ?)";
//		    try (PreparedStatement pstmt1 = conn.prepareStatement(sql1)) {
//		        pstmt1.setString(1, "value1");
//		        pstmt1.setInt(2, 123);
//		        pstmt1.executeUpdate();
//		    }
//	
//		    // Insert into the second table
//		    String sql2 = "INSERT INTO table2 (column3, column4) VALUES (?, ?)";
//		    try (PreparedStatement pstmt2 = conn.prepareStatement(sql2)) {
//		        pstmt2.setString(1, "value3");
//		        pstmt2.setString(2, "value4");
//		        pstmt2.executeUpdate();
//		    }
	
		    // Commit the transaction
		    conn.commit();
	
		    System.out.println("Data inserted into multiple tables successfully.");

		} catch (SQLException ex1) {
		    System.out.println("Error occurred during insertion: " + ex1.getMessage());
		    ex1.printStackTrace();
		    try {
		        if (conn != null) {
		            conn.rollback(); // Rollback in case of error
		        }
		    } catch (SQLException ex2) {
		        System.out.println("Error during rollback: " + ex2.getMessage());
		        ex2.printStackTrace();
		    }
		} finally {
		    try {
		        if (conn != null) {
		            conn.setAutoCommit(true); // Restore auto-commit before closing
		            conn.close(); // Close the connection
		        }
		    } catch (SQLException ex3) {
		        System.out.println("Error occurred while closing the connection: " + ex3.getMessage());
		        ex3.printStackTrace();
		    }
		}
	}

	public Connection conn;

}
