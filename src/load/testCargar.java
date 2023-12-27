package load;

import java.util.Scanner;

public class testCargar {

	public static void main(String[] args) throws Exception {
		Scanner scanner = new Scanner(System.in);
        try {
    		new Cargar("/home/depiction/Documents/geodesia/interoperabilidad/aerogravimetria/ecopetrol/3_2006_LLANOS_CENTRO_GRAV/datos",
    				"3_2006_LLANOS_CENTRO_GRAV.csv",
    				"aerogravimetria",
    				"IDE",
    				"eigen-6c4",
    				"jdbc:postgresql://localhost:5432/geodesia",
    				"depiction",
    				"afsstgLm",
    				scanner,
    				"",
    				"");
        } finally {
            scanner.close(); // Close the scanner here when you're completely done with it
        }

	}
	
}
