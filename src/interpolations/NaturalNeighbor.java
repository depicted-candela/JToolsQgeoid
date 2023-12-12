/**
 * 
 */
package interpolations;
import org.tinfour.interpolation.*;
import org.tinfour.standard.*;
import org.tinfour.common.*;
import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
/**
 * 
 */
public class NaturalNeighbor {
	
	/**
	 * Constructor to the interpolation with a point to interpolate
	 * @param longitude The longitude coordinate where the interpolation is computed
	 * @param latitude The latitude coordinate where the interpolation is computed
	 * @throws Exception 
	 */
	public NaturalNeighbor(double longitude, double latitude) throws Exception {
		modelSelection("", "");
		printModel();
		LV = fileReader("", "");					// The list of Vertexes readed from the .gdf file
		setBounds();								// Set boundaries of the map to interpolate
		LONGITUDE = longitude;
		LATITUDE = latitude;
		itinNniInitialiazer();
	}
	
	/**
	 * Constructor to the interpolation with a point to interpolate
	 * @param longitude The array of longitude coordinates where the interpolation is computed
	 * @param latitude The array of latitude coordinates where the interpolation is computed
	 * @throws Exception 
	 */
	public NaturalNeighbor(Double[] longitude, Double[] latitude, String measurement, String model) throws Exception {
		modelSelection(measurement, model);
		printModel();
		LV = fileReader(measurement, model);		// The list of Vertexes readed from the .gdf file
		setBounds();								// Set boundaries of the map to interpolate
		LONGITUDE_A = longitude;
		LATITUDE_A = latitude;
		itinNniInitialiazer();
	}
	
	/**
	 * Initializes the Delaunay Triangulation
	 */
	private void itinNniInitialiazer() {
		ITIN = new IncrementalTin();				// Initialization of the Delaunay Triangulation
		for (Vertex v: LV) ITIN.add(v);				// Filled of the Delaunay Triangulation 
													// Geographical limits of the Delaunay Triangulation
													// Validates the coordinates to interpolate in
		NNI = new NaturalNeighborInterpolator(ITIN);
	}
	
	/**
	 * Constructor to the interpolation without the point to interpolate
	 * @throws Exception 
	 */
	public NaturalNeighbor() throws Exception {
		printModel();
		LV = fileReader("", "");					// The list of Vertexes readed from the .gdf file
		setBounds();								// Set boundaries of the map to interpolate
		itinNniInitialiazer();
	}
	
	/**
	 * Creates the boundaries of the map to interpolate
	 */
	private void setBounds() {
		XLIMS[0] = LV.get(0).getX();
		XLIMS[1] = LV.get(LV.size() - 1).getX();
		YLIMS[0] = LV.get(0).getY();
		YLIMS[1] = LV.get(LV.size() - 1).getY();
	}
	
	/**
	 * Interpolates N in an specific point
	 * @return Interpolated value of N in a specific point
	 * @throws Exception
	 */
	public double getPunctualInterp() throws Exception {
		validator(LONGITUDE, LATITUDE);				// Validates the coordinates to interpolate in
													// Interpolates the point
		return NNI.interpolate(LONGITUDE, LATITUDE, null);
	}
	
	/**
	 * Interpolates the measurement in specific points
	 * @return Interpolated value of N in a specific point
	 * @throws Exception
	 */
	public double[] getInterps() throws Exception {
		
		validator_array();							// Validates the coordinates to interpolate in
													
		double[] interps = new double[LONGITUDE_A.length];
													// Interpolates all the points
		for (int i = 0; i < LONGITUDE_A.length; i++) {
			interps[i] = NNI.interpolate(LONGITUDE_A[i], LATITUDE_A[i], null);
		}
		
		return interps;
	}
	
	/**
	 * Indicates the coordinates of the point to interpolate
	 * in.
	 * @param longitude
	 * @param latitude
	 */
	public void setPoint(double longitude, double latitude) {
		LONGITUDE = longitude;
		LATITUDE =  latitude;
	}
	
	/**
	 * Indicates the coordinates of the points to interpolate
	 * in.
	 * @param longitude
	 * @param latitude
	 */
	public void setPoints(Double[] longitude, Double[] latitude) {
		LONGITUDE_A = longitude;
		LATITUDE_A =  latitude;
	}
	
	/**
	 * Validates if the coordinates of the point are within the bounds
	 * of the global geoid model.
	 * @param longitude
	 * @param latitude
	 * @throws Exception
	 */
	private void validator(double longitude, double latitude) throws Exception {
		if (longitude == -99999.999999999999 || latitude == -99999.999999999999) throw new Exception("You need to provide punctual coordinates");
		if (longitude < XLIMS[0] || longitude > XLIMS[1]) {
			throw new Exception("The x (" + String.valueOf(longitude) + ") coordinate is out of bound (" + String.valueOf(XLIMS[0]) + " " + String.valueOf(XLIMS[1]) + ")");
		} else if(latitude > YLIMS[0] || latitude < YLIMS[1]) {
			throw new Exception("The y (" + String.valueOf(latitude) + ") coordinate is out of bound (" + String.valueOf(YLIMS[0]) + " " + String.valueOf(YLIMS[1]) +  ")");
		}
	}
	
	/**
	 * Validates if the coordinates of the points are within the bounds
	 * of the global geoid model.
	 * @throws Exception
	 */
	private void validator_array() throws Exception {
		
		if (LONGITUDE_A.length != LATITUDE_A.length) {
			throw new Exception("Longitures are " + String.valueOf(LATITUDE_A.length) + " and Latitudes " + String.valueOf(LONGITUDE_A.length));
		}
		for (int i = 0; i < LONGITUDE_A.length; i++) {
			validator(LONGITUDE_A[i], LATITUDE_A[i]);
		}
	}
	
	/**
	 * Reads all the points of the global geoid model and creates a List
	 * Vertex to the Delaunay Triangulation ITIN.
	 * @return List<Vertex> of points readed from the global geoid model
	 */
	private List<Vertex> fileReader(String measurement, String model) throws Exception {
		List<Vertex> LV = new ArrayList<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(MODEL));
			while (!reader.readLine().startsWith("end_of_head")) {
				continue;
			}
			double[] data 	= new double[3];
			String line 	= new String();
			while((line = reader.readLine()) != null) {
				int	i 		= measurementSegregatorGDF(measurement, model);
				data 		= lineParser(line, i);
				LV.add(new Vertex(data[0] - 360, data[1], data[2]));
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return LV;
	}
	
	private int measurementSegregatorGDF(String measurement, String model) throws Exception {
		if (measurement.equals("altura_anomala") || measurement.equals("anomalous_height") || measurement.equals("")) {
			if (model.equals("eigen-6c4") || model.equals("")) {
				return 3;
			} else {
				throw new Exception("Setting {" + measurement + ", " + model + "} is not available");
			}
		} else if (measurement.equals("ondulacion_geoidal") || measurement.equals("geoid_undulation")) {
			if (model.equals("eigen-6c4") || model.equals("")) {
				return 2;
			} else {
				throw new Exception("Setting {" + measurement + ", " + model + "} is not available");
			}
		} else {
			throw new Exception("Setting {" + measurement + ", " + model + "} is not available");
		}
	}
	
	/**
	 * From strings to three doubles of longitude, latitude and N.
	 * @param line String of longitude, latitude and N
	 * @return double[] of longitude, latitude and N
	 */
	private double[] lineParser(String line, int value) {
		String[] arrd = line.trim().split("\\s+");
		double[] doub = new double[3];
		doub[0] = Double.parseDouble(arrd[0]);
		doub[1] = Double.parseDouble(arrd[1]);
		doub[2] = Double.parseDouble(arrd[value]);
		return doub;
	}
	
	/**
	 * Retrieves longitudinal limits of the geoid global model.
	 * @return double[] longitudinal of limits of the geoid global model
	 */
	public double[] getXLims() {
		return XLIMS;
	}
	
	/**
	 * Retrieves latitudinal limits of the geoid global model.
	 * @return double[] of latitudinal limits of the geoid global model
	 */
	public double[] getYLims() {
		return YLIMS;
	}
	
	/**
	 * The model used for the interpolation.
	 */
	private void printModel() {
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(MODEL));
			System.out.println("El modelo global utilizado");
			String line = reader.readLine();
			while (true) {
				line = reader.readLine();
				System.out.println(line);
				if (line.startsWith("         grid_format")) break;
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void modelSelection(String measurement, String model) {
		if (measurement.equals("altura_anomala") || measurement.equals("anomalous_height") || measurement.equals("")) {
			if (model.equals("eigen-6c4") || model.equals("")) MODEL	= GRS80_EIGEN6C4_ANOMALOUS_HEIGHT;
		} else if (measurement.equals("ondulacion_geoidal") || measurement.equals("geoid_undulation")) {
			if (model.equals("eigen-6c4") || model.equals("")) MODEL	= GRS80_EIGEN6C4_GEOID_UNDULATION;
		}
	}
	
	private double[] XLIMS = new double[2];
	private double[] YLIMS = new double[2];
	private IncrementalTin ITIN;
	private NaturalNeighborInterpolator NNI;
//	private static final String WGS84_EIGEN6C4_ANOMALOUS_HEIGHT = "/home/depiction/Documents/geodesia/interoperabilidad/recursos/globales/wgs84/altura_anomala_superficial/EIGEN-6C4_d27d1a201f4ce240c85d7c72521b53ee0a1b11c2dabd5da80f056965599be574.gdf";
//	private static final String WGS84_EIGEN6C4_GEOID_UNDULATION = "/home/depiction/Documents/geodesia/interoperabilidad/recursos/globales/wgs84/ondulacion_geoidal/EIGEN-6C4_43d6360b3cdab92632c04d89fcbc7c6b27b8f6957bd3c8b8925abbf2a360c080.gdf";
	private static final String GRS80_EIGEN6C4_ANOMALOUS_HEIGHT = "/home/depiction/Documents/geodesia/interoperabilidad/recursos/globales/grs80/altura_anomala_superficial/EIGEN-6C4_c8adae46c592500bc7a1c46a5d076ecce0089518302485dfc6c8593de3021af7.gdf";
	private static final String GRS80_EIGEN6C4_GEOID_UNDULATION = "/home/depiction/Documents/geodesia/interoperabilidad/recursos/globales/grs80/ondulacion_geoidal/EIGEN-6C4_c70e7337892362aa969231db1aa361b2c546b298a3f7c3e07fb08024063b7d73.gdf";
	private String MODEL;
	private List<Vertex> LV;
	private static Double[] LONGITUDE_A;
	private static Double[] LATITUDE_A;
	private static double LONGITUDE = -99999.999999999999;
	private static double LATITUDE = -99999.999999999999;
	
}