/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package akinator;

import Management.Algoritmo;
import Management.JogoStatsManager;
import Management.JsonSingleton;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.primefaces.json.JSONException;

/**
 *
 * @author arthu
 */
public class Interface {

    private String perguntaAtual;
    private String chaveAtual;
    private final HashMap<String, Integer> hashMapPerguntaResposta = new HashMap<>();
    private JsonSingleton jsonSingleton;
    private int qtdPerguntasFeitas = 0;
    private Algoritmo algoritmo;
    private JogoStatsManager statsManager;
    private int contadorNaoSei = 0;
    String[] simNaoNaoSeiOptions = {"Sim", "Não", "Não sei"};
    String[] simNaoOptions = {"Sim", "Não"};

    public void começarJogo() {
        this.algoritmo = new Algoritmo();
        this.jsonSingleton = new JsonSingleton();

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
            //TODO: Aprender personagem
        }
    }

    private void gerarEstatisticas() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
