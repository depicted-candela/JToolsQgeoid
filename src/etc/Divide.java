package etc;

import java.io.*;

public class Divide {

    public static void main(String[] args) throws IOException {

        String inputCsvFile = "/home/depiction/Documents/geodesia/interoperabilidad/aerogravimetria/sgc/T9_2010_BLOQUE_CPE8_GRAV/datos/T9_2010_BLOQUE_CPE8_GRAV.csv"; // Replace with your file path
        String outputFilePath = "/home/depiction/Documents/geodesia/interoperabilidad/aerogravimetria/sgc/T9_2010_BLOQUE_CPE8_GRAV/datos/T9_2010_BLOQUE_CPE8_GRAVH.csv"; // Replace with your file path

        String[] newHeaders = {"OBJECTID", "FID", "LINE", "FLT", "DATE", "TIME", "LAT", "LONG", "RAW_ALT", "UTM_X", "UTM_Y", "UTM_Z", "MSL_Z", "TM_X", "TM_Y", "TERRAIN", "FX", "FY", "FZ", "GRVRAW", "EOTCOR", "GRVEOT", "FACORR", "FREEAIR", "FRA28s", "FRA50s", "SBGCOR_2_6", "ECCOR_2_67", "TERCOR_2_6", "LEVCOR_2_6", "BGL28s_2_6", "BGL50s_2_6", "SBGCOR_2_2", "ECCOR_2_20", "TERCOR_2_2", "BGL28s_2_2", "BGL50s_2_2", "BG28s_2_67", "BG28s_2_20", "BG50s_2_67", "BG50s_2_20", "FA3000", "BG3000_2_6", "BG3000_2_2", "FA4500", "BG4500_2_6", "BG4500_2_2", "Proyecto"};

        changeCSVHeaders(inputCsvFile, outputFilePath, newHeaders);

        // Store headers for later use
        String headerLine = String.join(",", newHeaders);

        BufferedReader reader = new BufferedReader(new FileReader(outputFilePath));

        // Count total lines (including headers)
        int lines = 0;
        while (reader.readLine() != null) lines++;
        reader.close();

        // Calculate lines per file (excluding headers in each file)
        int linesPerFile = (int) Math.ceil((double) (lines - 1) / 3);

        // Read and split the file
        reader = new BufferedReader(new FileReader(outputFilePath));
        reader.readLine(); // Skip the original header line

        for (int i = 0; i < 3; i++) {
            PrintWriter writer = new PrintWriter("/home/depiction/Documents/geodesia/interoperabilidad/aerogravimetria/sgc/T9_2010_BLOQUE_CPE8_GRAV/datos/T9_2010_BLOQUE_CPE8_GRAVH_" + (i + 1) + ".csv", "UTF-8");

            // Write the headers to each file
            writer.println(headerLine);

            for (int j = 0; j < linesPerFile && reader.ready(); j++) {
                writer.println(reader.readLine());
            }

            writer.close();
        }

        reader.close();
    }

    private static void changeCSVHeaders(String inputFilePath, String outputFilePath, String[] newHeaders) {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {

            // Read the first line (original headers)
            String originalHeaders = reader.readLine();

            if (originalHeaders != null) {
                // Write the new headers
                writer.write(String.join(",", newHeaders));
                writer.newLine();

                // Write the rest of the file
                String line;
                while ((line = reader.readLine()) != null) {
                    writer.write(line);
                    writer.newLine();
                }
            }

            System.out.println("CSV headers have been changed successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
