/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package akinator;

import Management.Algoritmo;
import Management.JogoStatsManager;
import Management.JsonReader;
import Management.JsonSingleton;
import Management.JsonUtils;
import Management.JsonWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.primefaces.json.JSONArray;
import org.primefaces.json.JSONException;
import org.primefaces.json.JSONObject;

/**
 *
 * @author arthu
 */
public class Interface {

    private String perguntaAtual;
    private String chaveAtual;
    private final HashMap<String, Integer> hashMapPerguntaResposta = new HashMap<>();
    private JsonSingleton jsonSingleton;
    private JsonUtils jsonUtils;
    private JsonWriter jsonWriter;
    private JsonReader jsonReader;
    private int qtdPerguntasFeitas = 0;
    private Algoritmo algoritmo;
    private JogoStatsManager statsManager;
    private int contadorNaoSei = 0;
    String[] simNaoNaoSeiOptions = {"Sim", "Não", "Não sei"};
    String[] simNaoOptions = {"Sim", "Não"};
    String[] simNaoVoltarOptions = {"Sim", "Não", "Voltar"};

    public void começarJogo() {
        this.algoritmo = new Algoritmo();
        this.jsonSingleton = new JsonSingleton();
        this.jsonUtils = new JsonUtils();
        this.jsonReader = new JsonReader();
        this.jsonWriter = new JsonWriter();
        /*
            * Número mínimo de perguntas ainda não atingiu o mínimo 
            * OU ainda não há personagens para propor E se ainda há perguntas
         */
        try {
            while (jsonSingleton.getPerguntasRestantes() > 0
                    && ((qtdPerguntasFeitas < algoritmo.QUESTIONS_THRESOLD) || (!algoritmo.existePersonagemParaPropor(qtdPerguntasFeitas)))) {
                mostrarPergunta();
                if (contadorNaoSei == 5) {
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        goToResultActivity();
        gerarEstatisticas();
    }

    private void mostrarPergunta() {
        String pergunta = "";
        try {
            pergunta = algoritmo.getPerguntaMaisRestringente();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String[] partsRequestAlgorithm = pergunta.split(";");
        this.chaveAtual = partsRequestAlgorithm[0];
        this.perguntaAtual = partsRequestAlgorithm[1];
        int resposta = JOptionPane.showOptionDialog(null, perguntaAtual, chaveAtual, JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, simNaoNaoSeiOptions, simNaoNaoSeiOptions[0]);
        if (resposta == 2) {
            contadorNaoSei++;
        }
        gerenciarPergunta(this.chaveAtual, resposta);
    }

    private void gerenciarPergunta(String chavePerguntaAtual, int respostaJogador) {
        ++qtdPerguntasFeitas;

        // Remplit la liste des questions avec les réponses données
        preencheHashMapPerguntaResposta(chavePerguntaAtual, respostaJogador);

        // Calcule du score pour chaque perso pour la question courante
        // avec la réponse donnée par le USer
        try {
            algoritmo.calculaPontuacaoParaPersonagens(chavePerguntaAtual, respostaJogador);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Elimina pergunta que já foi feita
        try {

            algoritmo.eliminaPergunta(chavePerguntaAtual);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void preencheHashMapPerguntaResposta(String chavePerguntaAtual, int respostaJogador) {
        this.hashMapPerguntaResposta.put(chavePerguntaAtual, respostaJogador);
    }

    private void goToResultActivity() {
        statsManager = new JogoStatsManager();

        int acertou = proporResultado();

        if (acertou == 0) {
            statsManager.insereJogo(algoritmo.getPersonagemMaiorPontuacao(), new Date(), true);
        } else {
            algoritmo.eliminaPersonagemListaPontuacao(algoritmo.getPersonagemMaiorPontuacao());
            try {
                proximaProposta();
            } catch (JSONException ex) {
                Logger.getLogger(Interface.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private int proporResultado() {
        if (contadorNaoSei == 5) {
            JOptionPane.showMessageDialog(null, "Minha certeza é de 100%");
            JOptionPane.showMessageDialog(null, "Minha certeza é que nem você conhece esse personagem!");
        } else {
            return JOptionPane.showOptionDialog(null,
                    "Estou com " + algoritmo.getPorcentagemAcerto(qtdPerguntasFeitas) + "% no que você estava pensando: \n\n" + algoritmo.getPersonagemMaiorPontuacao() + "\n\nAcertei?",
                    "", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, simNaoOptions, simNaoOptions[0]);
            //String namePerso = algoritmo.getPersonagemMaiorPontuacao().replaceAll("\\s+", "").replaceAll("'", "").replace("-", "");
        }
        return 0;
    }

    private void proximaProposta() throws JSONException {
        if (algoritmo.existePersonagemParaPropor(qtdPerguntasFeitas) == true) {
            proporResultado();
        } else if (jsonSingleton.getPerguntasRestantes() > 0) {
            //TODO: continuar fazendo perguntas
            mostrarPergunta();
        } else {
            JOptionPane.showMessageDialog(null, "Ok eu admito a minha derrota!", "", JOptionPane.ERROR_MESSAGE);
            int aprende = JOptionPane.showOptionDialog(null, "Voce poderia me deixar mais inteligente?", "", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                    simNaoOptions, simNaoOptions[0]);
            if (aprende == 0) {
                    String personagem = JOptionPane.showInputDialog(null, "Legal! Você poderia me dizer qual personagem estava pensando?");
                    String pergunta;
                    int resposta;
                    do {
                        pergunta = pedirPergunta();
                        resposta = pedirResposta();
                    } while (resposta == 2);
                    
                try {
                    //Verifica se personagem já existe na base de conhecimento
                    if (jsonUtils.personagemJaExiste(personagem.toUpperCase())) {
                        atualizaPersonagemExistente(personagem, newQuestionKey, pergunta, resposta);
                    } else {
                        adicionaNovoPersonagem(personagem, newQuestionKey, pergunta, resposta);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Interface.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                acabaJogo();
            }
        }
    }

    private void gerarEstatisticas() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void acabaJogo() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private String pedirPergunta() {
        return JOptionPane.showInputDialog(null, "Você poderia me fazer uma pergunta no mesmo estilo que eu fiz para você?");
    }

    private int pedirResposta() {
        return JOptionPane.showOptionDialog(null, "E qual seria a resposta?", "", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                simNaoOptions, simNaoVoltarOptions[0]);
    }
    
    private void adicionaNovoPersonagem(String nomePersonagem, String newQuestionKey, String newQuestionValue, String responseToNewQuestion) {
        // Add new character to JSON with his responses
        JSONObject newCharacter = new JSONObject();
        // Fill character with question responded before
        for (Map.Entry<String, Integer> entry : hashMapPerguntaResposta.entrySet()) {
            String questionKey = entry.getKey();
            int response = entry.getValue();
            try {
                // Test here "prob oui" , "prob non"
                newCharacter.put(questionKey, algoritmo.getResponseByCode(response));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // Fille character with other question unresponded
        JSONArray questionsFromMemory = jsonSingleton.getJsonPerguntas();
        ArrayList<String> arrayTampon = new ArrayList<String>();
        try {
            JSONObject questions = questionsFromMemory.getJSONObject(0);

            Iterator<?> keys = questions.keys();
            boolean isAlreadyInCharacter = false;
            while (keys.hasNext()) {
                if (isAlreadyInCharacter) {
                    isAlreadyInCharacter = false;
                }
                String questionKey = (String) keys.next();
                Iterator<?> keysOnPerso = newCharacter.keys();
                while (keysOnPerso.hasNext()
                        && !isAlreadyInCharacter) {
                    String questionKeyPerso = (String) keysOnPerso
                            .next();
                    if (questionKey.equals(questionKeyPerso)) {
                        isAlreadyInCharacter = true;
                    }
                }
                // If the question isn't already defined for the new
                // character, add it
                if (!isAlreadyInCharacter) {
                    arrayTampon.add(questionKey);
                }
            }
            // Add all missing keys for the new personage
            for (String key : arrayTampon) {
                newCharacter.put(key, "desconhecido");
            }

        } catch (JSONException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        // Put personage name
        try {
            newCharacter.put("Personagem", nomePersonagem.getText().toString().toUpperCase());
        } catch (JSONException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        // Insert question
        try {
            jsonUtils.adicionaPergunta(newQuestionKey, newQuestionValue);// Insertion
            // OK
            // in
            // internal
            // storage
            // Fill new question and its response
            newCharacter.put(newQuestionKey, responseToNewQuestion);

            // Fill the new question key for all characters already
            // in json personnages
            JSONArray arrayPersoTampon = new JSONArray();
            JSONArray arrayPersonnagesInMemory = new JSONArray(jsonReader.readJSONfromInternalStorage("personnages.json"));
            for (int i = 0; i < arrayPersonnagesInMemory.length(); i++) {
                JSONObject perso = arrayPersonnagesInMemory.getJSONObject(i);
                perso.put(newQuestionKey, "inconnu");

                arrayPersoTampon.put(perso);
            }
            jsonSingleton.setJsonPersonagens(arrayPersoTampon);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // Insert new character
        jsonUtils.adicionaPersonagem(newCharacter);// TODO Check
        // insertion

        try {
            // Write new json personnages
            jsonWriter.writeJson(jsonUtils.getJsonPersonagens().toString(),jsonSingleton.jsonPersonagensFile);
            // Write new json questions
            jsonWriter.writeJson(jsonUtils.getJsonPerguntas().toString(),jsonSingleton.jsonPerguntasFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    
    private void atualizaPersonagemExistente(String nomePersonagem, String chaveNovaPergunta, String newQuestionValue, int respostaNovaPergunta) throws JSONException, IOException {
        //If a personage already exists, we need to fill other informations unknows before and entered by the user
        //Get this perso 
        JSONObject jsonPersonagem = null;
        jsonPersonagem = jsonUtils.getPersonagemPorNome(nomePersonagem);
        if (jsonPersonagem != null) {
            //Fill it new properties if needed (different or named "inconnu")
            for (Map.Entry<String, Integer> entry : hashMapPerguntaResposta.entrySet()) {
                String chave = entry.getKey();
                int resposta = entry.getValue();
                //Response comparison
                String responsePerso = jsonPersonagem.getString(chave);
                if (!responsePerso.equals(algoritmo.getResponseByCode(resposta))) {
                    jsonPersonagem.put(chave, algoritmo.getResponseByCode(resposta));
                }
            }
            
            //Adiciona pergunta nova
            jsonPersonagem.put(chaveNovaPergunta, algoritmo.getResponseByCode(respostaNovaPergunta));
            //json Character with this perso
            //First delete perso to refill it
            jsonUtils.excluiPersonagemPorNome(nomePersonagem.toUpperCase());
            jsonUtils.adicionaPersonagem(jsonPersonagem);
            JSONArray jsonPersonnageWithNewCharacterFilled = jsonUtils.getJsonPersonagens();
            //Write personnages
            jsonWriter.writeJson(jsonPersonnageWithNewCharacterFilled.toString(), jsonSingleton.jsonPersonagensFile);
            //TODO: atualizar JSON de perguntas
        }
    }
}
