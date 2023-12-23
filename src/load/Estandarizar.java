package load;

import java.util.*;

import interpolations.NaturalNeighbor;

public class Estandarizar {

	public Estandarizar(Read lector, String tipo, String via, String model, Conexion c) throws Exception {
		this.lector = lector;
		eConstructor(tipo, via, model, c);
	}
	
	private void eConstructor(String tipo, String via, String model, Conexion c) throws Exception {
		if (tipo.equals("aerogravimetria")) {
			lectorLongsLats();
			if (via.equals("IDE")) {
				zetaAereoIDE(model);
				NAereoIDE(model);
				if (lector.tieneDeriva()) {
					MiscelaneaLectora ml = new MiscelaneaLectora("organizacion", c.conn);
					if (ml.organizacion.equals("2")) {
						dch = new DerivaCarsonH(ml.organizacion, lector.getDeriva(), lector.getDerivaConcat());
					}
				}
				
			} else if (via.equals("jar")) {
				Scanner scanner = new Scanner(System.in);
				System.out.println("Ingrese la ruta hacia el modelo geoidal global en formato .gdf para la variable Altura Anómala");
				String pathmodel_z = scanner.nextLine();
				System.out.println("Ingrese la ruta hacia el modelo geoidal global en formato .gdf para la variable Ondulación geoidal");
				String pathmodel_N = scanner.nextLine();
				scanner.close();
				zetaAereoJar(pathmodel_z, model);
				NAereoJar(pathmodel_N, model);
				if (lector.tieneDeriva()) {
					MiscelaneaLectora ml = new MiscelaneaLectora("organizacion", c.conn);
					if (ml.organizacion.equals("2")) {
						dch = new DerivaCarsonH(ml.organizacion, lector.getDeriva(), lector.getDerivaConcat());
					}
				}
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
	
	private Read lector;
	private static List<Map<String, String>> csvfile;
	public List<Map<String, String>> csvest = new ArrayList<>();
	public DerivaCarsonH dch;
	Double[] longs, lats;
	
}

abstract class Deriva {
	public Deriva(String org) {
		this.org = org;
	}
	String org;
}

class DerivaCarsonH extends Deriva {
	public DerivaCarsonH(String org, List<Map<String, String>> csvDeriva, List<Map<String, String>> csvConcatDeriva) {
		super(org);
		lineasDerivadas(csvDeriva, csvConcatDeriva);
	}
	private void lineasDerivadas(List<Map<String, String>> csvDeriva, List<Map<String, String>> csvConcatDeriva) {
        // Subsetting and renaming
        List<Map<String, String>> subcsvDeriva = new ArrayList<>();
        for (Map<String, String> row : csvDeriva) {
            Map<String, String> newRow = new HashMap<>();
            newRow.put("Flt", row.get("FlightNumber"));
            newRow.put("BeforeFlight", row.get("BeforeFlight"));
            newRow.put("AfterFlight", row.get("AfterFlight"));
            subcsvDeriva.add(newRow);
        }
        // Merging
        List<Map<String, String>> mergedData = new ArrayList<>();
        for (Map<String, String> concatRow : csvConcatDeriva) {
            for (Map<String, String> subdfRow : subcsvDeriva) {
                if (subdfRow.get("Flt").equals(concatRow.get("Flt"))) {
                    Map<String, String> mergedRow = new HashMap<>(concatRow);
                    mergedRow.putAll(subdfRow);
                    mergedData.add(mergedRow);
                }
            }
        }
        //Concat
        for (Map<String, String> mergedRow : mergedData) {
        	String Dir = mergedRow.get("Dir");
        	String Flt = mergedRow.get("Flt");
        	if (Flt.length() < 2) {
        		Flt = "0" + Flt;
        	}
        	mergedRow.put("LINE", Dir + Flt + mergedRow.get("LineID#"));
        }
        this.mergedData = mergedData;
	}
	
	public List<Map<String, String>> mergedData;
	
}

