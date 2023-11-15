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
	public NaturalNeighbor(double longitude, double latitude) {
		printModel();
		LV = fileReader();							// The list of Vertexes readed from the .gdf file
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
	public NaturalNeighbor(Double[] longitude, Double[] latitude) {
		printModel();
		LV = fileReader();							// The list of Vertexes readed from the .gdf file
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
	public NaturalNeighbor() {
		printModel();
		LV = fileReader();							// The list of Vertexes readed from the .gdf file
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
	 * Interpolates N in specific points
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
	private List<Vertex> fileReader() {
		List<Vertex> LV = new ArrayList<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(FILE));
			while (!reader.readLine().startsWith("end_of_head")) {
				continue;
			}
			double[] data = new double[3];
			String line = new String();
			while((line = reader.readLine()) != null) {
				data = lineParser(line);
				LV.add(new Vertex(data[0] - 360, data[1], data[2]));
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return LV;
	}
	
	/**
	 * From strings to three doubles of longitude, latitude and N.
	 * @param line String of longitude, latitude and N
	 * @return double[] of longitude, latitude and N
	 */
	private double[] lineParser(String line) {
		String[] arrd = line.trim().split("\\s+");
		double[] doub = new double[3];
		doub[0] = Double.parseDouble(arrd[0]);
		doub[1] = Double.parseDouble(arrd[1]);
		doub[2] = Double.parseDouble(arrd[2]);
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
			BufferedReader reader = new BufferedReader(new FileReader(FILE));
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
	
	private double[] XLIMS = new double[2];
	private double[] YLIMS = new double[2];
	private IncrementalTin ITIN;
	private NaturalNeighborInterpolator NNI;
	private static final String FILE = "/home/nicalcoca/eclipse-workspace/JToolsQgeoid/media/EIGEN-6C4_35efcdf6a22c20614a405b784b51e8532d7e9ef6e6fe3550da2a3e802a432ab0.gdf";
	private List<Vertex> LV;
	private static Double[] LONGITUDE_A;
	private static Double[] LATITUDE_A;
	private static double LONGITUDE = -99999.999999999999;
	private static double LATITUDE = -99999.999999999999;
	
}