package load;

import java.util.*;

import interpolations.NaturalNeighbor;

public class Estandarizar {

	public Estandarizar(List<Map<String, String>> rows, String tipo, String via, String model) throws Exception {
		csvfile = rows;
		eConstructor(tipo, via, model);
		
	}
	
	private void eConstructor(String tipo, String via, String model) throws Exception {
		if (tipo.equals("aerogravimetria")) {
			lectorLongsLats();
			if (via.equals("IDE")) {
				zetaAereoIDE(model);
				NAereoIDE(model);
			} else if (via.equals("jar")) {
				Scanner scanner = new Scanner(System.in);
				System.out.println("Ingrese la ruta hacia el modelo geoidal global en formato .gdf para la variable Altura Anómala");
				String pathmodel_z = scanner.nextLine();
				System.out.println("Ingrese la ruta hacia el modelo geoidal global en formato .gdf para la variable Ondulación geoidal");
				String pathmodel_N = scanner.nextLine();
				scanner.close();
				zetaAereoJar(pathmodel_z, model);
				NAereoJar(pathmodel_N, model);
			} else {
				throw new Exception("Esa via para carga no existe");
			}
		}
	}
	
	private void lectorLongsLats() {
		longs 	= new Double[csvfile.size()];
		lats 	= new Double[csvfile.size()];
		int c 	= 0;
		for (Map<String, String> row: csvfile) {
			longs[c] 	= Double.parseDouble(row.get("LONG"));
			lats[c] 	= Double.parseDouble(row.get("LAT"));
			c++;
		}
	}
	
	private void zetaAereoIDE(String modelo) throws Exception {
		NaturalNeighbor nnzeta 	= new NaturalNeighbor(longs, lats, "ondulacion_geoidal", modelo);
		double[] interpszeta	= nnzeta.getInterps();
		int c = 0;
		for (Map<String, String> row: csvfile) {
			row.put("zeta", String.valueOf(interpszeta[c]));
			csvest.add(row);
			c++;
		}
	}
	
	private void NAereoIDE(String modelo) throws Exception {
		NaturalNeighbor nnN 	= new NaturalNeighbor(longs, lats, "ondulacion_geoidal", modelo);
		double[] interpsN		= nnN.getInterps();
		int c = 0;
		for (Map<String, String> row: csvfile) {
			row.put("N", String.valueOf(interpsN[c]));
			csvest.add(row);
			c++;
		}
	}
	
	private void zetaAereoJar(String pathmodel, String modelo) throws Exception {
		NaturalNeighbor nnzeta 	= new NaturalNeighbor(longs, lats, pathmodel, "altura_anomala", modelo);
		double[] interpszeta	= nnzeta.getInterps();
		int c = 0;
		for (Map<String, String> row: csvfile) {
			row.put("zeta", String.valueOf(interpszeta[c]));
			csvest.add(row);
			c++;
		}
	}
	
	private void NAereoJar(String pathmodel, String modelo) throws Exception {
		NaturalNeighbor nnN 	= new NaturalNeighbor(longs, lats, pathmodel, "ondulacion_geoidal", modelo);
		double[] interpsN		= nnN.getInterps();
		int c = 0;
		for (Map<String, String> row: csvfile) {
			row.put("N", String.valueOf(interpsN[c]));
			csvest.add(row);
			c++;
		}
	}
	
	public List<Map<String, String>> getData() {
		return csvest;
	}

	private static List<Map<String, String>> csvfile;
	public List<Map<String, String>> csvest = new ArrayList<>();
	Double[] longs, lats;
	
}
