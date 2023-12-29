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

	public Read(String path, String file, String tipo, Scanner scan, String deriva, String concatenador_deriva) throws IOException {
		constructorLector(tipo, path, file, scan, deriva, concatenador_deriva);
	}
	
	private void constructorLector(String tipo, String path, String file, Scanner scan, String deriva, String concatenador_deriva) throws IOException {
		if (tipo.equals("aerogravimetria")) {
			this.tipo = tipo;
			csvDatos = fromString(path, file, 1);
			if (deriva.length() != 0 || concatenador_deriva.length() != 0) {
				csvDeriva = fromString(path, deriva, 0);
				csvDerivaConcat = fromString(path, concatenador_deriva, 0);
				derivas = true;
			}
		}
	}
	
	private List<Map<String, String>> fromString(String path, String file, int opt) throws IOException {
        Path d_path 		= Paths.get(path);
        Path infile 		= Paths.get(file);
        Path path_infile	= d_path.resolve(infile);
        List<Map<String, String>> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path_infile.toFile()))) {
            String line;
            String[] headers = br.readLine().split(","); // Read headers
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                Map<String, String> row = new HashMap<>();
                int c = 0;
                if (opt == 0) {
                    for (int i = 0; i < headers.length; i++) {
                        row.put(headers[i], values[i]);
                    }
                    rows.add(row);
                } else if (opt == 1) {
                    for (int i = 0; i < headers.length; i++) {
                    	if (headers[i].equals("LONG") &&  Math.abs(Double.parseDouble(values[i])) < 1) {
                    		System.out.println("Longitud igual a cero");
                    		c++;
                    		continue;
                    	} else if (headers[i].equals("LAT") && Math.abs(Double.parseDouble(values[i])) < 1) {
                    		System.out.println("Latitud igual a cero");
                    		c++;
                    		continue;
                    	} else {
                    		row.put(headers[i], values[i]);
                    	}
                    }
                }
                if (c > 0) {
                	continue;
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
		return csvDatos;
	}
	
	public void putData(List<Map<String, String>> lmss) {
		csvDatos = lmss;
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
	
	private List<Map<String, String>> csvDatos, csvDeriva, csvDerivaConcat;
	private String tipo;
	private boolean derivas = false;
	
}

class MiscelaneaLectora {
	
	public MiscelaneaLectora(String tipo, Connection con, Scanner scan) {
		switch(tipo) {
			case "cc": leerCC(scan); break;
			case "geoide": leerGeoide(con, scan); break;
			case "usuario": leerUsuario(con, scan); break;
			case "elipsoide": leerElipsoide(con, scan); break;
			case "organizacion": leerOrganizacion(con, scan); break;
			case "fuente": leerFuente(con, scan); break;
			case "detalles": leerDetalles(scan); break;
			case "nombre": leerNombre(scan); break;
			case "exactitud": leerExactitud(scan); break;
			case "fecha": leerFecha(scan); break;
			case "reporte": leerReporte(scan); break;
			case "archivo": leerArchivo(scan); break;
			case "naturaleza": leerNaturaleza(con, scan); break;
			case "tipo-carson": leerTipoCarson(scan); break;
			default: break;
		}
	}
	
	private void leerTipoCarson(Scanner scanner) {
		
		System.out.println("TIPO CARSON.");
        System.out.print("Tipo Carson: ");
        String input = scanner.nextLine();

        while(true) {
            if (input.equals("1") || input.equals("2") || input.equals("3")) {
            	tipoCarson = input;
                break;
            } else {
                System.out.print("Ingrese el identificador correcto: ");
                input = scanner.nextLine();
            }
        }
        
	}
	
	private void leerCC(Scanner scanner) {
		
		System.out.println("CROSS-COUPLING.");
        System.out.print("Es conocido el efecto Cross-Coupling del proyecto aéreo? (true or false): ");
        boolean boolValue;
        
        // This loop continues until a valid boolean is entered
        while (true) {
            if (scanner.hasNextBoolean()) {
            	boolValue = scanner.nextBoolean();
        		cc = boolValue;
        		break;
            } else {
                System.out.println("Entrada inválida. Por favor ingrese 'true' o 'false'.");
                scanner.next(); // Consume the invalid input
                System.out.print("Es conocido el efecto Cross-Coupling del proyecto aéreo? (true or false): ");
            }
        }
        
	}
	
	private void leerGeoide(Connection con, Scanner scanner) {
		
        System.out.println("GEOIDES.");
        mostrarFilas(con, "estandarizar_geoide", "id", "nombre");
        System.out.print("Seleccione uno de los modelos geoidales globales: ");
        String input = scanner.nextLine();

        while(true) {
            if (validarExistencia(con, "entrada_datos_insumoqgeoidal", input)) {
                geoide = input;
                break;
            } else {
                System.out.print("Geoide global inválido. Por favor ingrese el identificador correcto para un geoide: ");
                input = scanner.nextLine();
            }
        }
        
	}

	private void leerUsuario(Connection con, Scanner scanner) {
		
        System.out.println("USUARIOS.");
        mostrarFilas(con, "auth_user", "id", "email");
        System.out.print("Seleccione uno de los usuarios: ");
        String input = scanner.nextLine();

        while(true) {
            if (validarExistencia(con, "estandarizar_fuente", input)) {
            	usuario = input;
            	break;
            } else {
                System.out.print("Usuario inválido. Por favor ingrese el identificador correcto para un usuario: ");
                input = scanner.nextLine();
            }
        }
        
	}
	
	private void leerElipsoide(Connection con, Scanner scanner) {
		
        System.out.println("ELIPSOIDES.");
        mostrarFilas(con, "estandarizar_elipsoide", "id", "epsg");
        System.out.print("Seleccione uno de los elipsoides: ");
        String input = scanner.nextLine();
        while(true) {
            if (validarExistencia(con, "estandarizar_fuente", input)) {
            	elipsoide = input;
            	break;
            } else {
                System.out.print("Elipsoide inválido. Por favor ingrese el identificador correcto para un elipsoide: ");
                input = scanner.nextLine();
            }
        }
	}
	
	private void leerNaturaleza(Connection con, Scanner scanner) {
		
        System.out.println("NATURALEZAS.");
        mostrarFilas(con, "estandarizar_naturaleza", "id", "nombre");
        System.out.print("Seleccione una de las naturalezas: ");
        String input = scanner.nextLine();
        while(true) {
            if (validarExistencia(con, "estandarizar_naturaleza", input)) {
                naturaleza = input;
                break;
            } else {
                System.out.print("Naturaleza inválida. Por favor ingrese el identificador correcto para una naturaleza: ");
                input = scanner.nextLine();
            }
        }
	}
	
    private void leerOrganizacion(Connection con, Scanner scanner) {
    	
        System.out.println("ORGANIZACIONES.");
        mostrarFilas(con, "estandarizar_organizacion", "id", "nombre");
        System.out.print("Seleccione una de las organizaciones: ");
        String input = scanner.nextLine();
        while(true) {
            if (validarExistencia(con, "estandarizar_organizacion", input)) {
                organizacion = input;
                break;
            } else {
                System.out.print("Organización inválida. Por favor ingrese el identificador correcto para una organización: ");
                input = scanner.nextLine();
            }
        }
        
    }
	
    private void leerFuente(Connection con, Scanner scanner) {
    	
        System.out.println("FUENTES.");
        mostrarFilas(con, "estandarizar_fuente", "id", "nombre");
        System.out.print("Seleccione una de las fuentes: ");
        String input = scanner.nextLine();

        while(true) {
            if (validarExistencia(con, "estandarizar_fuente", input)) {
                fuente = input;
                break;
            } else {
                System.out.print("Fuente inválida. Por favor ingrese el identificador correcto para una fuente: ");
                input = scanner.nextLine();
            }
        }
        
    }
    
    private void leerDetalles(Scanner scanner) {
    	
        System.out.println("DETALLES.");
        System.out.print("Ingrese los detalles del proyecto: ");
        String input = scanner.nextLine();

        while(true) {
            if (input.length() > 15) {
                detalles = input;
                break;
            } else {
                System.out.print("Entrada inválida. Por favor ingrese detalles con más de 15 caracteres: ");
                input = scanner.nextLine();
            }
        }
        
    }
	
    private void leerReporte(Scanner scanner) {
    	
        System.out.println("REPORTE.");
        System.out.print("Ingrese la dirección en el sistema del documento aunado con toda la información técnica y descriptiva del proyecto: ");
        String input = scanner.nextLine();

        while(true) {
            if (Files.exists(Paths.get(input))) {
                reporte = input;
                break;
            } else {
                System.out.print("Entrada inválida. Por favor ingrese la dirección del archivo correctamente: ");
                input = scanner.nextLine();
            }
        }
        
    }
    
    private void leerArchivo(Scanner scanner) {
    	
        System.out.println("ARCHIVO.");
        System.out.print("Ingrese la dirección en el sistema del archivo con todos los datos alfanuméricos y espaciales del proyecto: ");
        String input = scanner.nextLine();

        while(true) {
            if (Files.exists(Paths.get(input))) {
                archivo = input;
                break;
            } else {
                System.out.print("Entrada inválida. Por favor ingrese la dirección del archivo correctamente: ");
                input = scanner.nextLine();
            }
        }
        
    }
	
    private void leerNombre(Scanner scanner) {
    	
        System.out.println("NOMBRE.");
        System.out.print("Ingrese el nombre del proyecto: ");
        String input = scanner.nextLine();

        while(true) {
            if (input.length() > 5) {
                nombre = input;
                break;
            } else {
                System.out.print("Entrada inválida. Por favor ingrese un nombre con más de 5 caracteres: ");
                input = scanner.nextLine();
            }
        }
        
    }
    
    private void leerExactitud(Scanner scanner) {
    	
        System.out.println("EXACTITUD.");
        System.out.print("Ingrese un número de 4 cifras con 3 decimales: ");
        String input = scanner.nextLine();

        // Regular expression to match the pattern
        String regex = "^0(\\.\\d{3})?$";
        while(true) {
            if (Pattern.matches(regex, input)) {
                exactitud = input;
                break;
            } else {
                System.out.print("Entrada inválida. Por favor ingrese un número de 4 cifras con 3 decimales que comience con cero (0.005 por ejemplo): ");
                input = scanner.nextLine();
            }
        }
        
    }
    
    private void leerFecha(Scanner scanner) {
    	
        System.out.println("FECHA.");
        System.out.print("Ingrese la fecha aproximada de captura de los datos (yyyy-MM-dd): ");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        while (true) {
            String dateString = scanner.nextLine();
            try {
                LocalDate localDate = LocalDate.parse(dateString, formatter);
                fecha = localDate.toString();
                break;
            } catch (DateTimeParseException e) {
            	System.out.print("Ingrese bien la fecha aproximada de captura de los datos (yyyy-MM-dd): ");
            }
        }
        
    }
    
	private boolean validarExistencia(Connection con, String tabla, String id) {
    	String sql = "SELECT * FROM public." + tabla + " WHERE id = " + id;
	    try (Statement stmt = con.createStatement();
	    		ResultSet rs 	= stmt.executeQuery(sql)) {
	    	return rs.next();
	    } catch (SQLException ex) {
	    	System.out.println("Error occurred: " + ex.getMessage());
	    	ex.printStackTrace();
	    }
		return false;
	}
    
	private void mostrarFilas(Connection conn, String tabla, String id, String nombre) {
    	String sql = "SELECT * FROM public." + tabla + " ORDER BY id";
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
	public String geoide, usuario, elipsoide, organizacion, fuente, detalles, reporte, archivo, nombre, exactitud, fecha, naturaleza, csvDeriva, csvConcatDeriva, tipoCarson;
	
}
