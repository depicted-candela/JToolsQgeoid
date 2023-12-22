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
		Read r 			= new Read(path, file);
		Estandarizar e 	= new Estandarizar(r.getData(), tipo, via, modelo);
		Conexion c		= new Conexion(url, user, password);
		constructorCargar(url, user, password, tipo, e, c.conn);
	}
	
	private void constructorCargar(String url, String user, String password, String tipo, Estandarizar e, Connection c) throws Exception {
		if (tipo == "aerogravimetria") {
			cargarAerogravimetria(url, user, password, tipo, e, c);
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
	
	
	private boolean leerCC() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Es conocido el efecto Cross-Coupling del proyecto aéreo? (true or false): ");
        boolean boolValue;
        
        // This loop continues until a valid boolean is entered
        while (true) {
            if (scanner.hasNextBoolean()) {
            	boolValue = scanner.nextBoolean();
            	scanner.close();
                return boolValue;
            } else {
                System.out.println("Entrada inválida. Por favor ingrese 'true' o 'false'.");
                scanner.next(); // Consume the invalid input
                System.out.print("Es conocido el efecto Cross-Coupling del proyecto aéreo? (true or false): ");
            }
        }
        
	}
	
	private String leerGeoide(Connection con) {
		
        Scanner scanner = new Scanner(System.in);
        System.out.println("GEOIDES.");
        selectFila(con, "entrada_datos_insumoqgeoidal", "id", "abrv");
        System.out.print("Seleccione uno de los modelos geoidales globales: ");
        String input = scanner.nextLine();

        while(true) {
            if (validarExistencia(con, "entrada_datos_insumoqgeoidal", input)) {
            	scanner.close();
                return input;
            } else {
                System.out.print("Geoide global inválido. Por favor ingrese el identificador correcto para un geoide: ");
                input = scanner.nextLine();
            }
        }
        
	}

	private String leerUsuario(Connection con) {
		
        Scanner scanner = new Scanner(System.in);
        System.out.println("ELIPSOIDES.");
        selectFila(con, "auth_user", "id", "email");
        System.out.print("Seleccione uno de los usuarios: ");
        String input = scanner.nextLine();

        while(true) {
            if (validarExistencia(con, "estandarizar_fuente", input)) {
            	scanner.close();
                return input;
            } else {
                System.out.print("Usuario inválido. Por favor ingrese el identificador correcto para un usuario: ");
                input = scanner.nextLine();
            }
        }
        
	}
	
	private String leerElipsoide(Connection con) {
		
        Scanner scanner = new Scanner(System.in);
        System.out.println("ELIPSOIDES.");
        selectFila(con, "estandarizar_elipsoide", "id", "epsg");
        System.out.print("Seleccione uno de los elipsoides: ");
        String input = scanner.nextLine();

        while(true) {
            if (validarExistencia(con, "estandarizar_fuente", input)) {
            	scanner.close();
                return input;
            } else {
                System.out.print("Elipsoide inválido. Por favor ingrese el identificador correcto para un elipsoide: ");
                input = scanner.nextLine();
            }
        }
        
	}
	
    private String leerFuente(Connection con) {
    	
        Scanner scanner = new Scanner(System.in);
        System.out.println("ORGANIZACIONES.");
        selectFila(con, "estandarizar_fuente", "id", "nombre");
        System.out.print("Seleccione una de las fuentes: ");
        String input = scanner.nextLine();

        while(true) {
            if (validarExistencia(con, "estandarizar_fuente", input)) {
            	scanner.close();
                return input;
            } else {
                System.out.print("Fuente inválida. Por favor ingrese el identificador correcto para una fuente: ");
                input = scanner.nextLine();
            }
        }
        
    }
	
	private void selectFila(Connection conn, String tabla, String id, String nombre) {
    	String sql = "SELECT * FROM " + tabla + " ORDER BY id";
    	int _id;
    	String _nombre;
	    try (Statement stmt = conn.createStatement();
	    		ResultSet rs 	= stmt.executeQuery(sql)) {
	    	System.out.println("Seleccione una fila de " + tabla);
	    	while (rs.next()) {
	    		_id 		= Integer.parseInt(rs.getString(id));
	    		_nombre		= rs.getString(nombre);
	    		System.out.println(id + ": " + _id + ", " + nombre + ": " + _nombre);
	    	}
	    } catch (SQLException ex) {
	    	System.out.println("Error occurred: " + ex.getMessage());
	    	ex.printStackTrace();
	    }
	}
	
	private boolean validarExistencia(Connection con, String tabla, String id) {
    	String sql = "SELECT * FROM " + tabla + " WHERE id = " + id;
	    try (Statement stmt = con.createStatement();
	    		ResultSet rs 	= stmt.executeQuery(sql)) {
	    	return rs.next();
	    } catch (SQLException ex) {
	    	System.out.println("Error occurred: " + ex.getMessage());
	    	ex.printStackTrace();
	    }
		return false;
	}
	
    private String leerOrganizacion(Connection con) {
    	
        Scanner scanner = new Scanner(System.in);
        System.out.println("ORGANIZACIONES.");
        selectFila(con, "estandarizar_organizacion", "id", "nombre");
        System.out.print("Seleccione una de las organizaciones: ");
        String input = scanner.nextLine();

        while(true) {
            if (validarExistencia(con, "estandarizar_organizacion", input)) {
            	scanner.close();
                return input;
            } else {
                System.out.print("Organización inválida. Por favor ingrese el identificador correcto para una organización: ");
                input = scanner.nextLine();
            }
        }
        
    }
	
    private String leerDetalles() {
    	
        Scanner scanner = new Scanner(System.in);
        System.out.println("DETALLES.");
        System.out.print("Ingrese los detalles del proyecto: ");
        String input = scanner.nextLine();

        while(true) {
            if (input.length() > 15) {
            	scanner.close();
                return input;
            } else {
                System.out.print("Entrada inválida. Por favor ingrese detalles con más de 15 caracteres: ");
                input = scanner.nextLine();
            }
        }
        
    }
	
    private String leerReporte() {
    	
        Scanner scanner = new Scanner(System.in);
        System.out.println("REPORTE.");
        System.out.print("Ingrese la dirección en el sistema del documento aunado con toda la información técnica y descriptiva del proyecto: ");
        String input = scanner.nextLine();

        while(true) {
            if (Files.exists(Paths.get(input))) {
                scanner.close();
                return input;
            } else {
                System.out.print("Entrada inválida. Por favor ingrese la dirección del archivo correctamente: ");
                input = scanner.nextLine();
            }
        }
        
    }
    
    private String leerArchivo() {
    	
        Scanner scanner = new Scanner(System.in);
        System.out.println("ARCHIVO.");
        System.out.print("Ingrese la dirección en el sistema del archivo con todos los datos alfanuméricos y espaciales del proyecto: ");
        String input = scanner.nextLine();

        while(true) {
            if (Files.exists(Paths.get(input))) {
                scanner.close();
                return input;
            } else {
                System.out.print("Entrada inválida. Por favor ingrese la dirección del archivo correctamente: ");
                input = scanner.nextLine();
            }
        }
        
    }
	
    private String leerNombre() {
    	
        Scanner scanner = new Scanner(System.in);
        System.out.println("NOMBRE.");
        System.out.print("Ingrese el nombre del proyecto: ");
        String input = scanner.nextLine();

        while(true) {
            if (input.length() > 5) {
            	scanner.close();
                return input;
            } else {
                System.out.print("Entrada inválida. Por favor ingrese un nombre con más de 5 caracteres: ");
                input = scanner.nextLine();
            }
        }
        
    }
    
    private String leerExactitud() {
    	
        Scanner scanner = new Scanner(System.in);
        System.out.println("EXACTITUD.");
        System.out.print("Ingrese un número de 4 cifras con 3 decimales: ");
        String input = scanner.nextLine();

        // Regular expression to match the pattern
        String regex = "^\\d{1,4}(\\.\\d{1,3})?$";
        while(true) {
            if (Pattern.matches(regex, input)) {
            	scanner.close();
                return input;
            } else {
                System.out.print("Entrada inválida. Por favor ingrese un número de 4 cifras con 3 decimales: ");
                input = scanner.nextLine();
            }
        }
        
    }
    
    private String leerFecha() {
    	
        Scanner scanner = new Scanner(System.in);
        System.out.println("FECHA.");
        System.out.print("Ingrese la fecha aproximada de captura de los datos (yyyy-MM-dd): ");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        while (true) {
            String dateString = scanner.nextLine();
            try {
                LocalDate localDate = LocalDate.parse(dateString, formatter);
                scanner.close();
                return localDate.toString();
            } catch (DateTimeParseException e) {
            	System.out.print("Ingrese bien la fecha aproximada de captura de los datos (yyyy-MM-dd): ");
            }
        }
        
    }
    
	private List<String> leerParametrosProyectoAerogravimetrico(Connection con) throws Exception {
		
		List<String> params = new ArrayList<String>();
		Integer i = construyeNuevoIdentificador(con, "estandarizar_proyectoaereo");
		if (i == null) {
			throw new Exception("Salto en creación de Proyecto Aereo, revise si hay información rota en la base de datos");
		}
		params.add(String.valueOf(i));
		params.add(leerExactitud());
		params.add(leerNombre());
		params.add(leerDetalles());
		params.add(leerArchivo());
		params.add(leerReporte());
		params.add(leerCC() + "");
		params.add(leerFecha());
		params.add(leerElipsoide(con));
		params.add(leerFuente(con));
		params.add(leerGeoide(con));
		params.add(leerOrganizacion(con));
		params.add(String.valueOf(detectUltimaFila(con, "estandarizar_proyectos")));
		params.add(leerUsuario(con));
		
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
	
	private void cargarPuntualmenteAerogravimetria(Connection con) {
		
	}
	
	private void cargarAerogravimetria(String url, String user, String password, String tipo, Estandarizar e, Connection conn) throws Exception {
		try {
			cargarProyectoGeneral(conn);
		    conn.commit();
		    System.out.println("Proyecto general creado");
		    cargarProyectoAerogravimetrico(conn);
		    conn.commit();
		    System.out.println("Proyecto aéreo subyacente creado");
		    cargarPuntualmenteAerogravimetria(conn);
		    conn.commit();
		    System.out.println("Puntualmente aerogravimetría subyacente creada");
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
}
