/**
 * 
 */
package interpolations;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
//import java.util.List;

//import org.tinfour.common.Vertex;

/**
 * 
 */
public class NaturalNeighborTest {

	/**
	 * @param args
	 * @throws Exception 
	 */
	
	private void fileReader() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader("/home/nicalcoca/eclipse-workspace/JToolsQgeoid/media/7_2008_LLANOS_SUR_GRAV_TEST.csv"));
			while (!reader.readLine().startsWith("LAT")) {
				continue;
			}
			Double[] data = new Double[2];
			String line = new String();
			while((line = reader.readLine()) != null) {
				data = lineParser(line);
				longs.add(data[1]);
				latts.add(data[0]);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Double[] lineParser(String line) {
		String[] arrd = line.trim().split("\\s+");
		Double[] doub = new Double[2];
		doub[0] = Double.parseDouble(arrd[0]);
		doub[1] = Double.parseDouble(arrd[1]);
		return doub;
	}
	
	public void run() throws Exception {
		fileReader();
		Double[] lo = new Double[longs.size()];
		longs.toArray(lo);
		Double[] la = new Double[latts.size()];
		latts.toArray(la);
		NaturalNeighbor nn = new NaturalNeighbor(lo, la, "", "");
		double[] interps = nn.getInterps();
		for (int i = 0; i < interps.length; i++) {
			if (longs.get(i) < -73.3 && longs.get(i) > -73.4) {
				if (latts.get(i) > 4.08 && latts.get(i) < 4.10) {
					System.out.println(longs.get(i));
					System.out.println(latts.get(i));
					System.out.println(interps[i]);
				}
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		new NaturalNeighborTest().run();
	}
	
	private ArrayList<Double> longs = new ArrayList<Double>();
	private ArrayList<Double> latts = new ArrayList<Double>();

}
