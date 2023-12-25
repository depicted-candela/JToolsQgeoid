package load;

import java.util.Scanner;

public class testCargar {

	public static void main(String[] args) throws Exception {
		Scanner scanner = new Scanner(System.in);
        try {
    		new Cargar("/Documentos/Trabajo/IGAC/interoperabilidad/raw/aerial/1_2006_CUENCA_DEL_YARI_CAGUAN_GRAV_HI/datos",
    				"1_2006_CUENCA_DEL_YARI_CAGUAN_GRAV_HI.csv",
    				"aerogravimetria",
    				"IDE",
    				"eigen-6c4",
    				"jdbc:postgresql://localhost:5432/geodesia",
    				"depiction",
    				"afsstgLm",
    				scanner,
    				"1_2006_CUENCA_DEL_YARI_CAGUAN_GRAV_HI_DRIFT.csv",
    				"1_2006_CUENCA_DEL_YARI_CAGUAN_GRAV_HI_CONCAT.csv");
        } finally {
            scanner.close(); // Close the scanner here when you're completely done with it
        }

	}
	
}
