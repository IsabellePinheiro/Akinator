package Management;

import java.io.File;
import java.io.IOException;
import org.primefaces.json.JSONArray;
import org.primefaces.json.JSONException;

public class JsonSingleton {

    private JSONArray jsonPersonagens;
    private JSONArray jsonPerguntas;
    private JSONArray jsonEstatisticas;
    private JsonReader jsonReader;
    private JsonWriter jsonWriter;
    

    public final String jsonStatsFile = "src/resources/data/estatisticas.json";
    public final String jsonPersonagensFile = "src/resources/data/personagens.json";
    public final String jsonPerguntasFile = "src/resources/data/perguntas.json";

    
    public JsonSingleton() {
        this.jsonReader = new JsonReader();
        this.jsonWriter = new JsonWriter();
        String jsonStringPersos = getJson(new File(jsonPersonagensFile));
        String jsonStringPerguntas = getJson(new File(jsonPerguntasFile));
        String jsonStringStats = getJson(new File(jsonStatsFile));
        try {
            this.jsonPersonagens = new JSONArray(jsonStringPersos);
            this.jsonPerguntas = new JSONArray(jsonStringPerguntas);
            this.jsonEstatisticas = new JSONArray(jsonStringStats);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*
    * Récupère le contenu du fichier dans le Storage
    * @param String filename Le nom du fichier JSON
    * */
    private String getJson(File fileName) {
        String jsonString = "";
        try {
            if(!fileName.exists()){
                fileName.createNewFile();
                this.jsonWriter.writeJson("[{}]", fileName.getPath());
            }
            jsonString = jsonReader.lerJSONBaseConhecimento(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonString;
    }

    public void iniciaJSONs() {
        try {
            this.setJsonPerguntas(new JSONArray(jsonReader.lerJSONBaseConhecimento(new File(jsonPerguntasFile))));
            this.setJsonPersonagens(new JSONArray(jsonReader.lerJSONBaseConhecimento(new File(jsonPersonagensFile))));
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Retorna JSON das estatísticas
     * @return 
     */
    public JSONArray getJsonEstatisticas() {
        return jsonEstatisticas != null ? jsonEstatisticas : new JSONArray();
    }

    /**
     * Retorna JSON dos personagens
     * @return 
     */
    public JSONArray getJsonPersonagens() {
        return jsonPersonagens;
    }

    /**
     * Retorna JSON das perguntas
     * @return 
     */
    public JSONArray getJsonPerguntas() {
        return jsonPerguntas;
    }

    public void setJsonPersonagens(JSONArray newJson) {
        this.jsonPersonagens = newJson;
    }

    public void setJsonPerguntas(JSONArray newJson) {
        this.jsonPerguntas = newJson;
    }

    public int getPerguntasRestantes() throws JSONException {
        return jsonPerguntas.getJSONObject(0).length();
    }
}
