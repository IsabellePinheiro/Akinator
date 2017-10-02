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
import java.io.File;
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
    private Integer chaveAtual;
    private final HashMap<Integer, Integer> hashMapPerguntaResposta = new HashMap<>();
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
        this.jsonUtils = new JsonUtils();
        this.jsonReader = new JsonReader();
        this.jsonWriter = new JsonWriter();
        /*
            * Número mínimo de perguntas ainda não atingiu o mínimo 
            * OU ainda não há personagens para propor E se ainda há perguntas
         */
        try {
            while (jsonUtils.getJsonSingleton().getPerguntasRestantes() > 0
                    && ((qtdPerguntasFeitas < algoritmo.QUESTIONS_THRESOLD) || (!algoritmo.existePersonagemParaPropor(qtdPerguntasFeitas)))) {
                mostrarPergunta();
                if (contadorNaoSei == 5) {
                    break;
                }
            }
        } catch (JSONException e) {
            Logger.getLogger(Interface.class.getName()).log(Level.SEVERE, null, e);
        }
        goToResultActivity();
        gerarEstatisticas();
    }

    private void mostrarPergunta() {
        String pergunta = "";
        try {
            pergunta = algoritmo.getPerguntaMaisRestringente();
        } catch (JSONException e) {
            Logger.getLogger(Interface.class.getName()).log(Level.SEVERE, null, e);
        }

        String[] partsRequestAlgorithm = pergunta.split(";");
        this.chaveAtual = Integer.parseInt(partsRequestAlgorithm[0]);
        this.perguntaAtual = partsRequestAlgorithm[1];
        int resposta = JOptionPane.showOptionDialog(null, perguntaAtual, chaveAtual.toString(), JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, simNaoNaoSeiOptions, simNaoNaoSeiOptions[0]);
        if (resposta == 2) {
            contadorNaoSei++;
        }
        gerenciarPergunta(this.chaveAtual, resposta);
    }

    private void gerenciarPergunta(Integer chavePerguntaAtual, int respostaJogador) {
        ++qtdPerguntasFeitas;

        // Salva as respostas dadas para cada pergunta
        preencheHashMapPerguntaResposta(chavePerguntaAtual, respostaJogador);

        // Calcula pontuação para cada personagem
        try {
            algoritmo.calculaPontuacaoParaPersonagens(chavePerguntaAtual, respostaJogador);
        } catch (JSONException e) {
            Logger.getLogger(Interface.class.getName()).log(Level.SEVERE, null, e);
        }

        // Elimina pergunta que já foi feita
        try {

            algoritmo.eliminaPergunta(chavePerguntaAtual);
        } catch (JSONException e) {
            Logger.getLogger(Interface.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private void preencheHashMapPerguntaResposta(Integer chavePerguntaAtual, int respostaJogador) {
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
        } else if (jsonUtils.getJsonSingleton().getPerguntasRestantes() > 0) {
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
                        atualizaPersonagemExistente(personagem, jsonUtils.getNovaChave(), pergunta, resposta);
                    } else {
                        adicionaNovoPersonagem(personagem, jsonUtils.getNovaChave(), pergunta, resposta);
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
    
    private void adicionaNovoPersonagem(String nomePersonagem, Integer chaveNovaPergunta, String novaPergunta, int respotaNovaPergunta) {
        // Cria novo personagem
        JSONObject novoPersonagem = new JSONObject();
        // Adiciona respostas dadas para as perguntas anteiores
        hashMapPerguntaResposta.entrySet().forEach((entry) -> {
            Integer chavePergunta =  entry.getKey();
            int resposta = entry.getValue();
            try {
                novoPersonagem.put(chavePergunta .toString(), algoritmo.getRespostaPorChave(resposta));
            } catch (JSONException e) {
                Logger.getLogger(Interface.class.getName()).log(Level.SEVERE, null, e);
            }
        });
        
        
        // Preencher perguntas não respondidas com "desconhecido"
        JSONArray perguntasConhecidas = jsonUtils.getJsonSingleton().getJsonPerguntas();
        ArrayList<String> perguntasNaoPreenchidas = new ArrayList<>();
        try {
            JSONObject perguntas = perguntasConhecidas.getJSONObject(0);

            Iterator<?> chaves = perguntas.keys();
            boolean jaFoiPreenchida = false;
            while (chaves.hasNext()) {
                if (jaFoiPreenchida) {
                    jaFoiPreenchida = false;
                }
                String chavePergunta = (String) chaves.next();
                Iterator<?> chavesPreenchidas = novoPersonagem.keys();
                while (chavesPreenchidas.hasNext() && !jaFoiPreenchida) {
                    String questionKeyPerso = (String) chavesPreenchidas.next();
                    if (chavePergunta.equals(questionKeyPerso)) {
                        jaFoiPreenchida = true;
                    }
                }
                
                //Se não foi preenchida, deve ser agora
                if (!jaFoiPreenchida) {
                    perguntasNaoPreenchidas.add(chavePergunta);
                }
            }
            
            // Preenche personagem
            for (String chave : perguntasNaoPreenchidas) {
                novoPersonagem.put(chave, "desconhecido");
            }
        } catch (JSONException e) {
            Logger.getLogger(Interface.class.getName()).log(Level.SEVERE, null, e);
        }
        
        // Colocar nome do personagem
        try {
            novoPersonagem.put("Personagem", nomePersonagem.toUpperCase());
        } catch (JSONException e) {
            Logger.getLogger(Interface.class.getName()).log(Level.SEVERE, null, e);
        }

        try {
            // Insere pergunta no JSON das perguntas e no novo personagem
            jsonUtils.adicionaPergunta(chaveNovaPergunta, novaPergunta);
            novoPersonagem.put(chaveNovaPergunta.toString(), respotaNovaPergunta);

            
            //Preenche com desconhecido em todos os personagens que já estão na base de conhecimento
            JSONArray novoJSONPersonagens = new JSONArray();
            JSONArray personagensConhecidos = new JSONArray(jsonReader.lerJSONBaseConhecimento(new File(jsonUtils.getJsonSingleton().jsonPersonagensFile)));
            for (int i = 0; i < personagensConhecidos.length(); i++) {
                JSONObject perso = personagensConhecidos.getJSONObject(i);
                perso.put(chaveNovaPergunta.toString(), "desconhecido");

                novoJSONPersonagens.put(perso);
            }
            jsonUtils.getJsonSingleton().setJsonPersonagens(novoJSONPersonagens);
        }catch (JSONException | IOException e) {
            Logger.getLogger(Interface.class.getName()).log(Level.SEVERE, null, e);
        }
        
        
        jsonUtils.adicionaPersonagem(novoPersonagem);

        try {
            // Escreve novo JSON de personagens na base de conhecimento
            jsonWriter.writeJson(jsonUtils.getJsonPersonagens().toString(),jsonUtils.getJsonSingleton().jsonPersonagensFile);
            // Escreve novo JSON de perguntas na base de conhecimento
            jsonWriter.writeJson(jsonUtils.getJsonPerguntas().toString(),jsonUtils.getJsonSingleton().jsonPerguntasFile);
        } catch (IOException e) {
            Logger.getLogger(Interface.class.getName()).log(Level.SEVERE, null, e);
        }

    }
    
    private void atualizaPersonagemExistente(String nomePersonagem, Integer chaveNovaPergunta, String novaPergunta, int respostaNovaPergunta) throws JSONException, IOException {
        //Se personagem já existe, informações desconhecidas anteriormente devem ser inseridas
        //Respostas são buscadas pelas respostas dadas durante o jogo
        JSONObject jsonPersonagem = null;
        jsonPersonagem = jsonUtils.getPersonagemPorNome(nomePersonagem);
        if (jsonPersonagem != null) {
            for (Map.Entry<Integer, Integer> entry : hashMapPerguntaResposta.entrySet()) {
                Integer chave = entry.getKey();
                int resposta = entry.getValue();

                String respostaPersonagem = jsonPersonagem.getString(chave.toString());
                if (!respostaPersonagem.equals(algoritmo.getRespostaPorChave(resposta))) {
                    jsonPersonagem.put(chave.toString(), algoritmo.getRespostaPorChave(resposta));
                }
            }
            
            
            //Adiciona pergunta nova
            jsonPersonagem.put(chaveNovaPergunta.toString(), algoritmo.getRespostaPorChave(respostaNovaPergunta));
            
            //json Character with this perso
            //First delete perso to refill it
            jsonUtils.excluiPersonagemPorNome(nomePersonagem.toUpperCase());
            jsonUtils.adicionaPersonagem(jsonPersonagem);
            
            //Escreve novo JSON na base de conhecimento
            jsonWriter.writeJson(jsonUtils.getJsonPersonagens().toString(), jsonUtils.getJsonSingleton().jsonPersonagensFile);
            
            
            //Escreve nova pergunta na base de conhecimento
            jsonUtils.adicionaPergunta(chaveNovaPergunta, novaPergunta);
            jsonWriter.writeJson(jsonUtils.getJsonPerguntas().toString(),jsonUtils.getJsonSingleton().jsonPerguntasFile);
        }
    }
}
