package load;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Map;
import interpolations.NaturalNeighbor;

public class Estandarizar {

	public Estandarizar(Read lector, String tipo, String via, String model, Conexion c, Scanner scan) throws Exception {
		this.lector = lector;
		this.csvDatos = lector.getData();
		eConstructor(tipo, via, model, c, scan);
	}
	
	private void eConstructor(String tipo, String via, String model, Conexion c, Scanner scan) throws Exception {
		Double[] alts = new Double[csvDatos.size()];
		Double[] lats = new Double[csvDatos.size()];
		Double[] aire_libre = new Double[csvDatos.size()];
		Double[] c_aire_libre = new Double[csvDatos.size()];
		if (tipo.equals("aerogravimetria")) {
			lectorLongsLats();
			if (via.equals("IDE")) {
				zetaAereoIDE(model);
				NAereoIDE(model);
			} else if (via.equals("jar")) {
				System.out.println("Ingrese la ruta hacia el modelo geoidal global en formato .gdf para la variable Altura Anómala");
				String pathmodel_z = scan.nextLine();
				System.out.println("Ingrese la ruta hacia el modelo geoidal global en formato .gdf para la variable Ondulación geoidal");
				String pathmodel_N = scan.nextLine();
				zetaAereoJar(pathmodel_z, model);
				NAereoJar(pathmodel_N, model);
			} else {
				throw new Exception("Esa via para carga no existe");
			}
			
			MiscelaneaLectora ml = new MiscelaneaLectora("organizacion", c.conn, scan);
			while(true) {
				if (ml.organizacion.equals("2")) {
					if (lector.tieneDeriva()) {
						dch = new DerivaCarsonH(ml.organizacion, lector.getDeriva(), lector.getDerivaConcat());
						derivas = true;
					}
					break;
				} else {
					throw new Exception("No existe aún sistematización de la deriva de esa organización");
				}
			}
			int i = 0;
			for (Map<String, String> m : csvDatos) {
				alts[i] 		= Double.parseDouble(m.get("RAW_ALT"));
				lats[i] 		= Double.parseDouble(m.get("LAT"));
				aire_libre[i] 	= Double.parseDouble(m.get("FREEAIR"));
				c_aire_libre[i++] = Double.parseDouble(m.get("FACORR"));
			}
			CalcularGravedadIndirectaCarsonH cgi = new CalcularGravedadIndirectaCarsonH(lats, alts, aire_libre, c_aire_libre);
			Double[] ng = cgi.grav_ind_car_h;
			i = 0;
			for (Map<String, String> m : csvDatos) {
				m.put("GRAV_H", String.valueOf(ng[i++]));
			}
		}
		csvEst = csvDatos;
	}
	
	private void lectorLongsLats() {
		longs 	= new Double[csvDatos.size()];
		lats 	= new Double[csvDatos.size()];
		int c 	= 0;
		for (Map<String, String> row: csvDatos) {
			longs[c] 	= Double.parseDouble(row.get("LONG"));
			lats[c++] 	= Double.parseDouble(row.get("LAT"));
		}
	}
	
	private void zetaAereoIDE(String modelo) throws Exception {
		NaturalNeighbor nnzeta 	= new NaturalNeighbor(longs, lats, "ondulacion_geoidal", modelo);
		double[] interpszeta	= nnzeta.getInterps();
		int c = 0;
		for (Map<String, String> row: csvDatos) {
			row.put("zeta", String.valueOf(interpszeta[c++]));
			csvEst.add(row);
		}
	}
	
	private void NAereoIDE(String modelo) throws Exception {
		NaturalNeighbor nnN 	= new NaturalNeighbor(longs, lats, "ondulacion_geoidal", modelo);
		double[] interpsN		= nnN.getInterps();
		int c = 0;
		for (Map<String, String> row: csvDatos) {
			row.put("N", String.valueOf(interpsN[c++]));
			csvEst.add(row);
		}
	}
	
	private void zetaAereoJar(String pathmodel, String modelo) throws Exception {
		NaturalNeighbor nnzeta 	= new NaturalNeighbor(longs, lats, pathmodel, "altura_anomala", modelo);
		double[] interpszeta	= nnzeta.getInterps();
		int c = 0;
		for (Map<String, String> row: csvDatos) {
			row.put("zeta", String.valueOf(interpszeta[c++]));
			csvEst.add(row);
		}
	}
	
	private void NAereoJar(String pathmodel, String modelo) throws Exception {
		NaturalNeighbor nnN 	= new NaturalNeighbor(longs, lats, pathmodel, "ondulacion_geoidal", modelo);
		double[] interpsN		= nnN.getInterps();
		int c = 0;
		for (Map<String, String> row: csvDatos) {
			row.put("N", String.valueOf(interpsN[c++]));
			csvEst.add(row);
		}
	}
	
	public List<Map<String, String>> getData() {
		return csvEst;
	}
	
	public DerivaCarsonH getDeriva() {
		return dch;
	}
	
	public boolean tieneDeriva() {
		return derivas;
	}
	
	private boolean derivas = false;
	private Read lector;
	private List<Map<String, String>> csvDatos;
	private List<Map<String, String>> csvEst = new ArrayList<>();
	private DerivaCarsonH dch;
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
        String Dir, Flt, Line;
        for (Map<String, String> mergedRow : mergedData) {
        	Dir = mergedRow.get("Dir");
        	Flt = mergedRow.get("Flt");
        	Line = mergedRow.get("LineID#");
        	if (Flt.length() == 1 && Line.length() == 5) {
        		Flt = "0" + Flt;
        	}
        	mergedRow.put("LINE", Dir + Flt + mergedRow.get("LineID#"));
        }
        this.mergedData = mergedData;
	}
	
	public List<Map<String, String>> getDerivas() {
		return mergedData;
	}
	
	private List<Map<String, String>> mergedData;
	
}

