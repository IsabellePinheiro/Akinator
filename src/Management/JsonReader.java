package Management;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class JsonReader {

    public JsonReader() {
    }

    /**
     * Retorna o que foi lido do <code>filename</code> em formato <code>String</code>
     * @param filename
     * @return
     * @throws IOException 
     * @throws java.io.FileNotFoundException 
     */
    public String lerJSON(File filename) throws IOException, FileNotFoundException {
        FileReader reader = new FileReader(filename);
        BufferedReader buffer = new BufferedReader(reader);
        String line = "";
        StringBuilder builder = new StringBuilder();
        while ((line = buffer.readLine()) != null) {
            builder.append(line);
        }
        return builder.toString();
    }
}
