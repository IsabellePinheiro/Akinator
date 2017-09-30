package Management;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class JsonWriter {

    public JsonWriter() {
    }

    /**
     * Escreve JSON em arquivo passado por parâmetro
     * @param jsonToWrite
     * @param filename
     * @throws IOException 
     */
    public void writeJson(String jsonToWrite, String filename) throws IOException {

        BufferedWriter buffW = new BufferedWriter(new FileWriter(filename));
        buffW.write(jsonToWrite);
        buffW.close();
    }

}
