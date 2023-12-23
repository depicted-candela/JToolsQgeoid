/**
 * 
 */
package load;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * 
 */
public class Read {

	public Read(String path, String file, String tipo, Conexion c) throws IOException {
		C = c;
		constructorLector(tipo, path, file);
	}
	
	private void constructorLector(String tipo, String path, String file) throws IOException {
		if (tipo.equals("aerogravimetria")) {
			this.tipo = tipo;
			csvfile = fromString(path, file);
	        
	        MiscelaneaLectora ml = new MiscelaneaLectora("deriva", C.conn);
	        
	        if (ml.cc) {
	        	derivas = true;
				lectorDeriva();
				lectorConcatDeriva();
	        }
		}
	}
	
	private void lectorDeriva() throws IOException {

		System.out.println("DERIVA.");
        Scanner scanner = new Scanner(System.in);
        System.out.print("Ingrese la dirección en el sistema del archivo con derivas: ");
        String input = scanner.nextLine();

        while(true) {
            if (Files.exists(Paths.get(input))) {
                scanner.close();
                Path p_file = Paths.get(input);
                String f_nm = p_file.getFileName().toString();
                String path = p_file.getParent().toString();
                csvDeriva = fromString(path, f_nm);
            } else {
                System.out.print("Entrada inválida. Por favor ingrese la dirección del archivo correctamente: ");
                input = scanner.nextLine();
            }
        }
		
	}
	
	private void lectorConcatDeriva() throws IOException {

		System.out.println("CONCATENADOR DE DERIVA.");
        Scanner scanner = new Scanner(System.in);
        System.out.print("Ingrese la dirección en el sistema del archivo contenedor de derivas: ");
        String input = scanner.nextLine();

        while(true) {
            if (Files.exists(Paths.get(input))) {
                scanner.close();
                Path p_file = Paths.get(input);
                String f_nm = p_file.getFileName().toString();
                String path = p_file.getParent().toString();
                csvDerivaConcat = fromString(path, f_nm);
            } else {
                System.out.print("Entrada inválida. Por favor ingrese la dirección del archivo correctamente: ");
                input = scanner.nextLine();
            }
        }
		
	}
	
	private List<Map<String, String>> fromString(String path, String file) throws IOException {
        Path d_path = Paths.get(path);
        Path infile = Paths.get(file);
        Path path_infile = d_path.resolve(infile);
        List<Map<String, String>> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path_infile.toFile()))) {
            String line;
            String[] headers = br.readLine().split(","); // Read headers
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                Map<String, String> row = new HashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    row.put(headers[i], values[i]);
                }
                rows.add(row);
            }
            return rows;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
	}
	
	public List<Map<String, String>> getData() {
		return csvfile;
	}
	
	public List<Map<String, String>> getDeriva() {
		return csvDeriva;
	}
	
	public List<Map<String, String>> getDerivaConcat() {
		return csvDerivaConcat;
	}
	
	public String getTipo() {
		return tipo;
	}
	
	public boolean tieneDeriva() {
		return derivas;
	}
	
	private List<Map<String, String>> csvfile, csvDeriva, csvDerivaConcat;
	private String tipo;
	private boolean derivas;
	private static Conexion C;
	
}

class MiscelaneaLectora {
	
	public MiscelaneaLectora(String tipo, Connection con) {
		switch(tipo) {
			case "cc": leerCC(); break;
			case "geoide": leerGeoide(con); break;
			case "usuario": leerUsuario(con); break;
			case "elipsoide": leerElipsoide(con); break;
			case "organizacion": leerOrganizacion(con); break;
			case "fuente": leerFuente(con); break;
			case "detalles": leerDetalles(); break;
			case "nombre": leerNombre(); break;
			case "exactitud": leerExactitud(); break;
			case "fecha": leerFecha(); break;
			case "reporte": leerReporte(); break;
			case "archivo": leerArchivo(); break;
			default: break;
		}
	}
	
	private void leerCC() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Es conocido el efecto Cross-Coupling del proyecto aéreo? (true or false): ");
        boolean boolValue;
        
        // This loop continues until a valid boolean is entered
        while (true) {
            if (scanner.hasNextBoolean()) {
            	boolValue = scanner.nextBoolean();
            	scanner.close();
        		cc = boolValue;
        		break;
            } else {
                System.out.println("Entrada inválida. Por favor ingrese 'true' o 'false'.");
                scanner.next(); // Consume the invalid input
                System.out.print("Es conocido el efecto Cross-Coupling del proyecto aéreo? (true or false): ");
            }
        }
        
	}
	
	private void leerGeoide(Connection con) {
		
        Scanner scanner = new Scanner(System.in);
        System.out.println("GEOIDES.");
        selectFila(con, "entrada_datos_insumoqgeoidal", "id", "abrv");
        System.out.print("Seleccione uno de los modelos geoidales globales: ");
        String input = scanner.nextLine();

        while(true) {
            if (validarExistencia(con, "entrada_datos_insumoqgeoidal", input)) {
            	scanner.close();
                geoide = input;
                break;
            } else {
                System.out.print("Geoide global inválido. Por favor ingrese el identificador correcto para un geoide: ");
                input = scanner.nextLine();
            }
        }
        
	}

	private void leerUsuario(Connection con) {
		
        Scanner scanner = new Scanner(System.in);
        System.out.println("USUARIOS.");
        selectFila(con, "auth_user", "id", "email");
        System.out.print("Seleccione uno de los usuarios: ");
        String input = scanner.nextLine();

        while(true) {
            if (validarExistencia(con, "estandarizar_fuente", input)) {
            	scanner.close();
            	usuario = input;
            	break;
            } else {
                System.out.print("Usuario inválido. Por favor ingrese el identificador correcto para un usuario: ");
                input = scanner.nextLine();
            }
        }
        
	}
	
	private void leerElipsoide(Connection con) {
		
        Scanner scanner = new Scanner(System.in);
        System.out.println("ELIPSOIDES.");
        selectFila(con, "estandarizar_elipsoide", "id", "epsg");
        System.out.print("Seleccione uno de los elipsoides: ");
        String input = scanner.nextLine();

        while(true) {
            if (validarExistencia(con, "estandarizar_fuente", input)) {
            	scanner.close();
            	elipsoide = input;
            	break;
            } else {
                System.out.print("Elipsoide inválido. Por favor ingrese el identificador correcto para un elipsoide: ");
                input = scanner.nextLine();
            }
        }
        
	}
	
    private void leerOrganizacion(Connection con) {
    	
        Scanner scanner = new Scanner(System.in);
        System.out.println("ORGANIZACIONES.");
        selectFila(con, "estandarizar_organizacion", "id", "nombre");
        System.out.print("Seleccione una de las organizaciones: ");
        String input = scanner.nextLine();

        while(true) {
            if (validarExistencia(con, "estandarizar_organizacion", input)) {
            	scanner.close();
                organizacion = input;
                break;
            } else {
                System.out.print("Organización inválida. Por favor ingrese el identificador correcto para una organización: ");
                input = scanner.nextLine();
            }
        }
        
    }
	
    private void leerFuente(Connection con) {
    	
        Scanner scanner = new Scanner(System.in);
        System.out.println("FUENTES.");
        selectFila(con, "estandarizar_fuente", "id", "nombre");
        System.out.print("Seleccione una de las fuentes: ");
        String input = scanner.nextLine();

        while(true) {
            if (validarExistencia(con, "estandarizar_fuente", input)) {
            	scanner.close();
                fuente = input;
                break;
            } else {
                System.out.print("Fuente inválida. Por favor ingrese el identificador correcto para una fuente: ");
                input = scanner.nextLine();
            }
        }
        
    }
    
    private void leerDetalles() {
    	
        Scanner scanner = new Scanner(System.in);
        System.out.println("DETALLES.");
        System.out.print("Ingrese los detalles del proyecto: ");
        String input = scanner.nextLine();

        while(true) {
            if (input.length() > 15) {
            	scanner.close();
                detalles = input;
                break;
            } else {
                System.out.print("Entrada inválida. Por favor ingrese detalles con más de 15 caracteres: ");
                input = scanner.nextLine();
            }
        }
        
    }
	
    private void leerReporte() {
    	
        Scanner scanner = new Scanner(System.in);
        System.out.println("REPORTE.");
        System.out.print("Ingrese la dirección en el sistema del documento aunado con toda la información técnica y descriptiva del proyecto: ");
        String input = scanner.nextLine();

        while(true) {
            if (Files.exists(Paths.get(input))) {
                scanner.close();
                reporte = input;
                break;
            } else {
                System.out.print("Entrada inválida. Por favor ingrese la dirección del archivo correctamente: ");
                input = scanner.nextLine();
            }
        }
        
    }
    
    private void leerArchivo() {
    	
        Scanner scanner = new Scanner(System.in);
        System.out.println("ARCHIVO.");
        System.out.print("Ingrese la dirección en el sistema del archivo con todos los datos alfanuméricos y espaciales del proyecto: ");
        String input = scanner.nextLine();

        while(true) {
            if (Files.exists(Paths.get(input))) {
                scanner.close();
                archivo = input;
                break;
            } else {
                System.out.print("Entrada inválida. Por favor ingrese la dirección del archivo correctamente: ");
                input = scanner.nextLine();
            }
        }
        
    }
	
    private void leerNombre() {
    	
        Scanner scanner = new Scanner(System.in);
        System.out.println("NOMBRE.");
        System.out.print("Ingrese el nombre del proyecto: ");
        String input = scanner.nextLine();

        while(true) {
            if (input.length() > 5) {
            	scanner.close();
                nombre = input;
                break;
            } else {
                System.out.print("Entrada inválida. Por favor ingrese un nombre con más de 5 caracteres: ");
                input = scanner.nextLine();
            }
        }
        
    }
    
    private void leerExactitud() {
    	
        Scanner scanner = new Scanner(System.in);
        System.out.println("EXACTITUD.");
        System.out.print("Ingrese un número de 4 cifras con 3 decimales: ");
        String input = scanner.nextLine();

        // Regular expression to match the pattern
        String regex = "^\\d{1,4}(\\.\\d{1,3})?$";
        while(true) {
            if (Pattern.matches(regex, input)) {
            	scanner.close();
                exactitud = input;
                break;
            } else {
                System.out.print("Entrada inválida. Por favor ingrese un número de 4 cifras con 3 decimales: ");
                input = scanner.nextLine();
            }
        }
        
    }
    
    private void leerFecha() {
    	
        Scanner scanner = new Scanner(System.in);
        System.out.println("FECHA.");
        System.out.print("Ingrese la fecha aproximada de captura de los datos (yyyy-MM-dd): ");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        while (true) {
            String dateString = scanner.nextLine();
            try {
                LocalDate localDate = LocalDate.parse(dateString, formatter);
                scanner.close();
                fecha = localDate.toString();
                break;
            } catch (DateTimeParseException e) {
            	System.out.print("Ingrese bien la fecha aproximada de captura de los datos (yyyy-MM-dd): ");
            }
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
	
	public boolean cc;
	public String geoide, usuario, elipsoide, organizacion, fuente, detalles, reporte, archivo, nombre, exactitud, fecha;
	
}
