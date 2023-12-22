/**
 * 
 */
package load;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 */
public class Read {

	public Read(String path, String file) throws IOException {
		fromString(path, file);
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	
	private void fromString(String path, String file) throws IOException {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        csvfile = rows;
	}
	
	public List<Map<String, String>> getData() {
		return csvfile;
	}
	
	private List<Map<String, String>> csvfile;

}
