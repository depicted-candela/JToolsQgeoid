/**
 * 
 */
package load;

import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * 
 */

public class Cargar {

	/**
	 * @throws Exception 
	 * 
	 */
	
	public Cargar(String path, String file, String tipo, String via, String modelo, String url, String user, String password, Scanner scan, String deriva, String concatenador_deriva) throws Exception {
		Conexion c		= new Conexion(url, user, password);
		r 				= new Read(path, file, tipo, scan, deriva, concatenador_deriva);
		Estandarizar e 	= new Estandarizar(r, tipo, via, modelo, c, scan);
		constructorCargar(tipo, e, c, scan);
	}
	
	private void constructorCargar(String tipo, Estandarizar e, Conexion c, Scanner scan) throws Exception {
		if (tipo.equals("aerogravimetria")) {
			cargarAerogravimetria(tipo, e, c, scan);
		}
	}
	
	private Integer detectUltimaFila(Connection conn, String table) {
    	String sql = "SELECT id FROM public." + table + " ORDER BY id DESC LIMIT 1";
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
	    String sql = "SELECT COUNT(*) FROM public." + table; // Replace with your specific query
	    try (Statement stmt = conn.createStatement();
	         ResultSet rs 	= stmt.executeQuery(sql)) {
	        if (rs.next()) {
	            if (rs.getInt(1) != 0) {
	            	return detectUltimaFila(conn, table) + 1;
	            } else {
	            	return 1;
	            }
	        }
	    } catch (SQLException ex) {
	        System.out.println("Error occurred: " + ex.getMessage());
	        ex.printStackTrace();
	    }
	    return null;
	}
	
	private void cargarProyectoGeneral(Connection conn, Scanner scan) throws Exception {

	    Integer pid = construyeNuevoIdentificador(conn, "estandarizar_proyectos");
	    if (pid == null) throw new Exception("Salto en creación de Proyecto General, revise si hay información rota en la base de datos");
		MiscelaneaLectora ml = new MiscelaneaLectora("naturaleza", conn, scan);
		int natur = Integer.parseInt(ml.naturaleza);
		
		String sql = "INSERT INTO public.estandarizar_proyectos (id, natur_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        	pstmt.setInt(1, pid);
        	pstmt.setInt(2, natur);
        	int affectedRows = pstmt.executeUpdate();
        	System.out.println(affectedRows + " proyecto general cargado.");

        } catch (SQLException e) {
        	e.printStackTrace();
        }
	    
	}
    
	private List<String> leerParametrosProyectoAerogravimetrico(Connection con, Scanner scan) throws Exception {
		
		List<String> params = new ArrayList<String>();
		Integer i = construyeNuevoIdentificador(con, "estandarizar_proyectoaereo");
		if (i == null) {
			throw new Exception("Salto en creación de Proyecto Aereo, revise si hay información rota en la base de datos");
		}
		
		params.add(String.valueOf(i));
		MiscelaneaLectora ml = new MiscelaneaLectora("exactitud", con, scan);
		params.add(ml.exactitud);
		ml = new MiscelaneaLectora("nombre", con, scan);
		params.add(ml.nombre);
		ml = new MiscelaneaLectora("detalles", con, scan);
		params.add(ml.detalles);
		ml = new MiscelaneaLectora("archivo", con, scan);
		params.add(ml.archivo);
		ml = new MiscelaneaLectora("reporte", con, scan);
		params.add(ml.reporte);
		ml = new MiscelaneaLectora("cc", con, scan);
		params.add(ml.cc + "");
		ml = new MiscelaneaLectora("fecha", con, scan);
		params.add(ml.fecha);
		ml = new MiscelaneaLectora("elipsoide", con, scan);
		params.add(ml.elipsoide);
		ml = new MiscelaneaLectora("fuente", con, scan);
		params.add(ml.fuente);
		ml = new MiscelaneaLectora("geoide", con, scan);
		params.add(ml.geoide);
		ml = new MiscelaneaLectora("organizacion", con, scan);
		params.add(ml.organizacion);
		params.add(String.valueOf(detectUltimaFila(con, "estandarizar_proyectos")));
		ml = new MiscelaneaLectora("usuario", con, scan);
		params.add(ml.usuario);
		return params;
    }
	
	private void cargarProyectoAerogravimetrico(Connection con, Scanner scan) throws Exception {
		List<String> ls = leerParametrosProyectoAerogravimetrico(con, scan);
		String sql = "INSERT INTO public.estandarizar_proyectoaereo (id, exact, nombre, detalle, archivo, reporte, cc, fecha, "
				+ "elip_id, fuente_id, geoid_id, org_id, pry_id_id, usuario_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
        	pstmt.setInt(1, Integer.parseInt(ls.get(0)));
        	pstmt.setDouble(2, Double.parseDouble(ls.get(1)));
        	pstmt.setString(3, ls.get(2));
        	pstmt.setString(4, ls.get(3));
        	pstmt.setString(5, ls.get(4));
        	pstmt.setString(6, ls.get(5));
        	pstmt.setBoolean(7, Boolean.parseBoolean(ls.get(6)));
        	pstmt.setDate(8, java.sql.Date.valueOf(ls.get(7)));
        	pstmt.setInt(9, Integer.parseInt(ls.get(8)));
        	pstmt.setInt(10, Integer.parseInt(ls.get(9)));
        	pstmt.setInt(11, Integer.parseInt(ls.get(10)));
        	pstmt.setInt(12, Integer.parseInt(ls.get(11)));
        	pstmt.setInt(13, Integer.parseInt(ls.get(12)));
        	pstmt.setInt(14, Integer.parseInt(ls.get(13)));
        	int affectedRows = pstmt.executeUpdate();
        	System.out.println(affectedRows + " proyecto aerogravimétrico cargado.");
        } catch (SQLException e) {
        	e.printStackTrace();
        }
	}
	
	private Boolean detectaDerivas(Conexion c) {
		Integer l_id 	= detectUltimaFila(c.conn, "estandarizar_linea");
    	String sql = "SELECT * FROM public.estandarizar_correcciones WHERE linea_id = " + l_id;
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
    	String sql = "SELECT * FROM public.estandarizar_linea WHERE pry_id_id = " + pry_id_id + " AND name = '" + li_name + "';";
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
		String sql	= "INSERT INTO public.estandarizar_linea (id, name, pry_id_id) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = c.conn.prepareStatement(sql)) {
        	pstmt.setInt(1, id);
        	pstmt.setString(2, line);
        	pstmt.setInt(3, pry_id_id);
        	int affectedRows = pstmt.executeUpdate();
        	System.out.println(affectedRows + " línea cargada.");
        } catch (SQLException e) {
        	e.printStackTrace();
        }
	}
	
	private void cargarDeriva(String line, Conexion c) {
		Integer l_id 	= detectUltimaFila(c.conn, "estandarizar_linea");
		Integer id		= construyeNuevoIdentificador(c.conn, "estandarizar_correcciones");
		String sql		= "INSERT INTO public.estandarizar_linea (id, deriva, marea, linea_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = c.conn.prepareStatement(sql)) {
        	pstmt.setInt(1, id);
        	pstmt.setBoolean(2, true);
        	pstmt.setNull(3, java.sql.Types.NULL);
        	pstmt.setNull(4, l_id);
        	int affectedRows = pstmt.executeUpdate();
        	System.out.println(affectedRows + " corrección por deriva cargada.");
        } catch (SQLException e) {
        	e.printStackTrace();
        }
	}
	
	private String extraeElipsoide(Conexion c, Integer pry_ar) {
		Integer i = detectElipsoide(c, pry_ar);
    	String sql = "SELECT epsg FROM public.estandarizar_elipsoide WHERE id = " + i + ";";
	    try (Statement stmt2 = c.conn.createStatement();
	    		ResultSet rs2 	= stmt2.executeQuery(sql)) {
	    	if (rs2.next()) {
	    		return rs2.getString("epsg");
	    	}
	    	return null;
	    } catch (SQLException ex) {
	    	System.out.println("Error occurred: " + ex.getMessage());
	    	ex.printStackTrace();
	    }
		return null;
	}
	
	private Integer detectElipsoide(Conexion c, Integer pry_ar) {
    	String sql = "SELECT elip_id FROM public.estandarizar_proyectoaereo WHERE id = " + pry_ar  + ";";
	    try (Statement stmt2 = c.conn.createStatement();
	    		ResultSet rs2 	= stmt2.executeQuery(sql)) {
	    	if (rs2.next()) {
	    		return Integer.parseInt(rs2.getString("elip_id"));
	    	}
	    	return null;
	    } catch (SQLException ex) {
	    	System.out.println("Error occurred: " + ex.getMessage());
	    	ex.printStackTrace();
	    }
		return null;
	}
	
	private void cargarPuntualmenteAerogravimetria(Estandarizar e, Conexion c) {
		String id, or_id, N, _long, lat, grav_h, h_adj, h_cru, radar, zeta, line;
		Double h_t;
		Integer prys_id 	= detectUltimaFila(c.conn, "estandarizar_proyectos");
		Integer pry_id 		= detectUltimaFila(c.conn, "estandarizar_proyectoaereo");
		String ell_id		= extraeElipsoide(c, pry_id);
		Integer linea_id	= detectUltimaFila(c.conn, "estandarizar_linea");
		List<Map<String, String>> rows = e.getData();
		int rows_u = 0;
		for (Map<String, String> row : rows) {
			id	= String.valueOf(construyeNuevoIdentificador(c.conn, "estandarizar_datoaereo"));
			line 	= row.get("LINE");
			if (!detectaLineas(line, prys_id, c)) {
				cargarLinea(prys_id, line, c);
				linea_id	= detectUltimaFila(c.conn, "estandarizar_linea");
				if(detectaDerivas(c)) cargarDeriva(line, c);
			}
			N 		= row.get("N");
			_long	= row.get("LONG");
			lat		= row.get("LAT");
			grav_h 	= row.get("GRAV_H");
			h_adj 	= row.get("RAW_ALT");
			h_cru 	= row.get("ADJ_ALT");
			h_t		= Double.parseDouble(row.get("RAW_ALT")) - Double.parseDouble(row.get("TERRAIN"));
			or_id	= row.get("FID");
			radar 	= row.get("RADAR");
			zeta	= row.get("zeta");
			String sql = "INSERT INTO public.estandarizar_datoaereo (id, linea_id, pry_id, n, geom, grav_h, h_adj, h_cru, h_t, or_id, radar, zeta) "
			        + "VALUES (?, ?, ?, ?, ST_GeomFromText('POINT(' || ? || ' ' || ? || ')', ?), ?, ?, ?, ?, ?, ?, ?)";
			try (PreparedStatement pstmt = c.conn.prepareStatement(sql)) {
			    pstmt.setLong(1, Integer.parseInt(id));
			    pstmt.setInt(2, linea_id);
			    pstmt.setInt(3, pry_id);
			    pstmt.setDouble(4, Double.parseDouble(N));
			    pstmt.setDouble(5, Double.parseDouble(_long)); // longitude
			    pstmt.setDouble(6, Double.parseDouble(lat));  // latitude
			    pstmt.setInt(7, Integer.parseInt(ell_id));    // SRID
			    pstmt.setDouble(8, Double.parseDouble(grav_h));
			    pstmt.setDouble(9, Double.parseDouble(h_adj));
			    pstmt.setDouble(10, Double.parseDouble(h_cru));
			    pstmt.setDouble(11, h_t);
			    pstmt.setLong(12, Long.parseLong(or_id));     // Assuming or_id is a numeric type
			    pstmt.setDouble(13, Double.parseDouble(radar));
			    pstmt.setDouble(14, Double.parseDouble(zeta));
			    pstmt.executeUpdate();
			    c.conn.commit();
			    System.out.println("1 punto aerogravimétrico cargado.");
			} catch (SQLException ex) {
			    ex.printStackTrace();
			}
			rows_u++;
		}
		System.out.println(rows_u + " puntos insertados.");
	}
	
	private void cargarAerogravimetria(String tipo, Estandarizar e, Conexion c, Scanner scan) throws Exception {
		try {
			cargarProyectoGeneral(c.conn, scan);
		    c.conn.commit();
		    System.out.println("Proyecto general creado");
		    cargarProyectoAerogravimetrico(c.conn, scan);
		    c.conn.commit();
		    System.out.println("Proyecto aéreo subyacente creado");
		    cargarPuntualmenteAerogravimetria(e, c);
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
