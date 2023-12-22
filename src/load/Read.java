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

	public Read(String path, String file, String tipo) throws IOException {
		constructorLector(tipo, path, file);
	}
	
	private void constructorLector(String tipo, String path, String file) throws IOException {
		if (tipo.equals("aerogravimetria")) {
			this.tipo = tipo;
			csvfile = fromString(path, file);
			// ESCRIBIR DERIVA Y CONCATENACIÓN AQUÍ
		}
	}
	
	private List<Map<String, String>> fromString(String path, String file) throws IOException {
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
            return rows;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
	}
	
	public List<Map<String, String>> getData() {
		return csvfile;
	}
	
	public List<Map<String, String>> getDeriva() {
		return csvDeriva;
	}
	
	public List<Map<String, String>> getDerivaConcat() {
		return csvDerivaConcat;
	}
	
	private List<Map<String, String>> csvfile, csvDeriva, csvDerivaConcat;
	private String tipo;

}
