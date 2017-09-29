/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package akinator;

import Management.JsonReader;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.primefaces.json.JSONException;
import org.primefaces.json.JSONObject;

/**
 *
 * @author arthu
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //Base de conhecimento
        File personagensArquivo = new File("personagens.json");
        
        //Máquina de inferência?
        File perguntasArquivo = new File("perguntas.json");
        
        
        JsonReader jsonReader = new JsonReader();
        JSONObject jsonPersonagens = null;
        JSONObject jsonPerguntas = null;
        
        
        //Carrega base de conhecimento
        try {
            jsonPersonagens = new JSONObject(jsonReader.lerJSON(personagensArquivo));
            jsonPerguntas = new JSONObject(jsonReader.lerJSON(perguntasArquivo));
        } catch (IOException | JSONException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Interface inter = new Interface();
        //Começa o jogo enviando base de conhecimento e máquina de inferencia?
        inter.começarJogo(jsonPersonagens, jsonPerguntas);
    }
}

