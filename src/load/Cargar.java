/**
 * 
 */
package load;

import java.nio.file.*;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.*;
import java.util.Scanner;

/**
 * 
 */

public class Cargar {

	/**
	 * @throws Exception 
	 * 
	 */
	
	public Cargar(String path, String file, String tipo, String via, String modelo, String url, String user, String password) throws Exception {
		Conexion c		= new Conexion(url, user, password);
		r 				= new Read(path, file, tipo, c);
		Estandarizar e 	= new Estandarizar(r, tipo, via, modelo, c);
		constructorCargar(tipo, e, c);
	}
	
	private void constructorCargar(String tipo, Estandarizar e, Conexion c) throws Exception {
		if (tipo == "aerogravimetria") {
			cargarAerogravimetria(tipo, e, c);
		}
	}
	
	private Integer detectUltimaFila(Connection conn, String table) {
    	String sql = "SELECT id FROM " + table + " ORDER BY id DESC LIMIT 1";
	    try (Statement stmt2 = conn.createStatement();
	    		ResultSet rs2 	= stmt2.executeQuery(sql)) {
	    	if (rs2.next()) {
	    		return Integer.parseInt(rs2.getString("id"));
		    }
	    } catch (SQLException ex) {
	    	System.out.println("Error occurred: " + ex.getMessage());
	    	ex.printStackTrace();
	    }
	    return null;
	}
	
	private Integer construyeNuevoIdentificador(Connection conn, String table) {
	    String sql = "SELECT COUNT(*) FROM " + table; // Replace with your specific query
	    try (Statement stmt = conn.createStatement();
	         ResultSet rs 	= stmt.executeQuery(sql)) {
	        if (rs.next()) {
	            if (rs.getInt(1) != 0) {
	            	return detectUltimaFila(conn, table) + 1;
	            }
	        }
	    } catch (SQLException ex) {
	        System.out.println("Error occurred: " + ex.getMessage());
	        ex.printStackTrace();
	    }
	    return null;
	}
	
	private void cargarProyectoGeneral(Connection conn) throws Exception {

	    Integer pid = construyeNuevoIdentificador(conn, "estandarizar_proyectos");
	    if (pid == null) throw new Exception("Salto en creación de Proyecto General, revise si hay información rota en la base de datos");
	    printNaturalezas(conn);
	    
		Scanner scanner = new Scanner(System.in);
		int natur = Integer.parseInt(scanner.nextLine());
		scanner.close();
		
		String sql = "INSERT INTO estandarizar_proyectos (id, nombre) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

        	pstmt.setInt(1, pid);
        	pstmt.setInt(2, natur);
        	int affectedRows = pstmt.executeUpdate();
        	System.out.println(affectedRows + " rows inserted.");

        } catch (SQLException e) {
        	e.printStackTrace();
        }
	    
	}
	
	private void printNaturalezas(Connection conn) {
    	String sql = "SELECT * FROM estandarizar_naturalezas ORDER BY id";
    	int id;
    	String natur;
	    try (Statement stmt2 = conn.createStatement();
	    		ResultSet rs 	= stmt2.executeQuery(sql)) {
	    	System.out.println("Seleccione una de las naturalezas");
	    	while (rs.next()) {
	    		id 		= Integer.parseInt(rs.getString("id"));
	    		natur	= rs.getString("nombre");
	    		System.out.println("id: " + id + ", naturaleza: " + natur);
	    	}
	    } catch (SQLException ex) {
	    	System.out.println("Error occurred: " + ex.getMessage());
	    	ex.printStackTrace();
	    }
	}
    
	private List<String> leerParametrosProyectoAerogravimetrico(Connection con) throws Exception {
		
		List<String> params = new ArrayList<String>();
		Integer i = construyeNuevoIdentificador(con, "estandarizar_proyectoaereo");
		if (i == null) {
			throw new Exception("Salto en creación de Proyecto Aereo, revise si hay información rota en la base de datos");
		}
		
		params.add(String.valueOf(i));
		MiscelaneaLectora ml = new MiscelaneaLectora("exactitud", con);
		params.add(ml.exactitud);
		ml = new MiscelaneaLectora("nombre", con);
		params.add(ml.nombre);
		ml = new MiscelaneaLectora("detalles", con);
		params.add(ml.detalles);
		ml = new MiscelaneaLectora("archivo", con);
		params.add(ml.archivo);
		ml = new MiscelaneaLectora("reporte", con);
		params.add(ml.reporte);
		ml = new MiscelaneaLectora("cc", con);
		params.add(ml.cc + "");
		ml = new MiscelaneaLectora("fecha", con);
		params.add(ml.fuente);
		ml = new MiscelaneaLectora("elipsoide", con);
		params.add(ml.elipsoide);
		ml = new MiscelaneaLectora("fuente", con);
		params.add(ml.fuente);
		ml = new MiscelaneaLectora("geoide", con);
		params.add(ml.geoide);
		ml = new MiscelaneaLectora("organizacion", con);
		params.add(ml.organizacion);
		params.add(String.valueOf(detectUltimaFila(con, "estandarizar_proyectos")));
		ml = new MiscelaneaLectora("usuario", con);
		params.add(ml.usuario);
		return params;
    }
	
	private void cargarProyectoAerogravimetrico(Connection con) throws Exception {
		List<String> ls = leerParametrosProyectoAerogravimetrico(con);
		String sql = "INSERT INTO estandarizar_proyectoaereo (id, exact, nombre, detalle, archivo, reporte, cc, fecha, "
				+ "elip_id, fuente_id, geoid_id, org_id, pry_id_id, usuario_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
        	pstmt.setInt(1, Integer.parseInt(ls.get(0)));
        	pstmt.setDouble(2, Double.parseDouble(ls.get(1)));
        	pstmt.setString(3, ls.get(2));
        	pstmt.setString(4, ls.get(3));
        	pstmt.setString(5, ls.get(4));
        	pstmt.setString(6, ls.get(5));
        	pstmt.setDouble(7, Double.parseDouble(ls.get(6)));
        	pstmt.setDate(8, java.sql.Date.valueOf(ls.get(7)));
        	pstmt.setInt(9, Integer.parseInt(ls.get(8)));
        	pstmt.setInt(10, Integer.parseInt(ls.get(9)));
        	pstmt.setInt(11, Integer.parseInt(ls.get(10)));
        	pstmt.setInt(12, Integer.parseInt(ls.get(11)));
        	pstmt.setInt(13, Integer.parseInt(ls.get(12)));
        	pstmt.setInt(14, Integer.parseInt(ls.get(13)));
        	int affectedRows = pstmt.executeUpdate();
        	System.out.println(affectedRows + " rows inserted.");
        } catch (SQLException e) {
        	e.printStackTrace();
        }
	}
	
	private Boolean detectaDerivas(Conexion c) {
		Integer l_id 	= detectUltimaFila(c.conn, "estandarizar_linea");
    	String sql = "SELECT * FROM estandarizar_correcciones WHERE linea_id = " + l_id;
	    try (Statement stmt2 = c.conn.createStatement();
	    		ResultSet rs2 	= stmt2.executeQuery(sql)) {
	    	if (rs2.next()) {
	    		return true;
		    } else {
		    	return false;
		    }
	    } catch (SQLException ex) {
	    	System.out.println("Error occurred: " + ex.getMessage());
	    	ex.printStackTrace();
	    }
	    return null;
	}
	
	private Boolean detectaLineas(String li_name, Integer pry_id_id, Conexion c) {
    	String sql = "SELECT * FROM estandarizar_linea WHERE pry_id_id = " + pry_id_id + " AND name = " + li_name;
	    try (Statement stmt2 = c.conn.createStatement();
	    		ResultSet rs2 	= stmt2.executeQuery(sql)) {
	    	if (rs2.next()) {
	    		return true;
		    } else {
		    	return false;
		    }
	    } catch (SQLException ex) {
	    	System.out.println("Error occurred: " + ex.getMessage());
	    	ex.printStackTrace();
	    }
	    return null;
	}
	
	private void cargarLinea(Integer pry_id_id, String line, Conexion c) {
		Integer id	= construyeNuevoIdentificador(c.conn, "estandarizar_linea");
		String sql	= "INSERT INTO estandarizar_linea (id, name, pry_id_id) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = c.conn.prepareStatement(sql)) {
        	pstmt.setInt(1, id);
        	pstmt.setString(2, line);
        	pstmt.setInt(3, pry_id_id);
        	int affectedRows = pstmt.executeUpdate();
        	System.out.println(affectedRows + " rows inserted.");
        } catch (SQLException e) {
        	e.printStackTrace();
        }
	}
	
	private void cargarDeriva(String line, Conexion c) {
		Integer l_id 	= detectUltimaFila(c.conn, "estandarizar_linea");
		Integer id		= construyeNuevoIdentificador(c.conn, "estandarizar_correcciones");
		String sql		= "INSERT INTO estandarizar_linea (id, deriva, marea, linea_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = c.conn.prepareStatement(sql)) {
        	pstmt.setInt(1, id);
        	pstmt.setBoolean(2, true);
        	pstmt.setNull(3, java.sql.Types.NULL);
        	pstmt.setNull(4, l_id);
        	int affectedRows = pstmt.executeUpdate();
        	System.out.println(affectedRows + " rows inserted.");
        } catch (SQLException e) {
        	e.printStackTrace();
        }
	}
	
	private Double gravedadIndirectaCarson() {
		
	}
	
	private void cargarPuntualmenteAerogravimetria(Estandarizar e, Conexion c) {
		Integer id, linea_id, pry_id;
		Double N, grav_h, h_adj, h_cru, h_t, or_id, radar, zeta, x, y;
		String line;
		Integer pa_id 	= detectUltimaFila(c.conn, "estandarizar_proyectos");
		List<Map<String, String>> rows = e.getData();
		for (Map<String, String> row : rows) {
			line 	= row.get("LINE");
			if (detectaLineas(line, pa_id, c)) {
				cargarLinea(pa_id, line, c);
				if(detectaDerivas(c)) cargarDeriva(line, c);
			}
			
			N 		= Double.parseDouble(row.get("N"));
			grav_h 	= Double.parseDouble(row.get(""));
			N 		= Double.parseDouble(row.get("N"));
			N 		= Double.parseDouble(row.get("N"));
			N 		= Double.parseDouble(row.get("N"));
			N 		= Double.parseDouble(row.get("N"));
			N 		= Double.parseDouble(row.get("N"));
			
			String sql		= "INSERT INTO estandarizar_linea (id, linea_id, pry_id, N, geom, grav_h, h_adj, h_cru, h_t, or_id, radar, zeta)"
					+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	        try (PreparedStatement pstmt = c.conn.prepareStatement(sql)) {
	        	pstmt.setInt(1, id);
	        	pstmt.setBoolean(2, true);
	        	pstmt.setNull(3, java.sql.Types.NULL);
	        	pstmt.setNull(4, l_id);
	        	int affectedRows = pstmt.executeUpdate();
	        	System.out.println(affectedRows + " rows inserted.");
	        } catch (SQLException e) {
	        	e.printStackTrace();
	        }
		}
	}
	
	private void cargarAerogravimetria(String tipo, Estandarizar e, Conexion c) throws Exception {
		try {
			cargarProyectoGeneral(c.conn);
		    c.conn.commit();
		    System.out.println("Proyecto general creado");
		    cargarProyectoAerogravimetrico(c.conn);
		    c.conn.commit();
		    System.out.println("Proyecto aéreo subyacente creado");
		    cargarPuntualmenteAerogravimetria(e, c);
		    c.conn.commit();
		    System.out.println("Aerogravimetría puntual subyacente creada");
		} catch (SQLException ex1) {
		    System.out.println("Error occurred during insertion: " + ex1.getMessage());
		    ex1.printStackTrace();
		    try {
		        if (c.conn != null) {
		        	c.conn.rollback(); // Rollback in case of error
		        }
		    } catch (SQLException ex2) {
		        System.out.println("Error during rollback: " + ex2.getMessage());
		        ex2.printStackTrace();
		    }
		} finally {
		    try {
		        if (c.conn != null) {
		        	c.conn.setAutoCommit(true); // Restore auto-commit before closing
		            c.conn.close(); // Close the connection
		        }
		    } catch (SQLException ex3) {
		        System.out.println("Error occurred while closing the connection: " + ex3.getMessage());
		        ex3.printStackTrace();
		    }
		}
	}
	
	private Read r;
	
}
