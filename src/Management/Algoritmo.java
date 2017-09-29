package Management;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.primefaces.json.JSONArray;
import org.primefaces.json.JSONException;
import org.primefaces.json.JSONObject;

/**
 * Created by Stef on 23/03/2015.
 */
public class Algoritmo {

    /*
	 * Lista de personagens que não correspondem as perguntas feitas
     */
    private static ArrayList<String> listPersosEliminados = new ArrayList<String>();

    /*
        * Resposta por código da resposta
     */
    private static HashMap<Integer, String> responseByResponseCode = new HashMap<Integer, String>();

    static {
        responseByResponseCode.put(0, "sim");
        responseByResponseCode.put(1, "não");
        responseByResponseCode.put(2, "não sei");
    }

    public String getResponseByCode(String code) {
        return responseByResponseCode.get(code);
    }

    /*
	 * Lista de personagens com suas pontuações
     */
    private static HashMap<String, Double> pontuacaoPersonagens = new HashMap<String, Double>();
    /*
	 * Intancia da classe gerenciadora de JSONs
     */
    private static JsonUtils jsonUtils;

    //Quantidade mínima de perguntas antes de propor um resultado
    public final int QUESTIONS_THRESOLD = 15;

    /*
	 * Pontuação mínima para poder propor uma resultado
     */
    private final int PROPOSAL_THRESOLD = 10;

    public void resetAllData() {
        if (!pontuacaoPersonagens.isEmpty()) {
            pontuacaoPersonagens.clear();
        }
        if (!listPersosEliminados.isEmpty()) {
            listPersosEliminados.clear();
        }
    }

    public HashMap<String, Double> getPontuacaoPersonagens() {
        return pontuacaoPersonagens;
    }

    /* Ordena lista de pontuação em ordem decrescente
    * TODO Test sorting list scores, doesn't work
    * */
    public void ordenarListaPontuacaoDesc() {

        Set<Map.Entry<String, Double>> entries = pontuacaoPersonagens.entrySet();
        List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>(entries);

        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> first, Map.Entry<String, Double> second) {

                return first.getValue().compareTo(second.getValue());
            }
        });

        LinkedHashMap<String, Double> sortedHashMap = new LinkedHashMap<String, Double>(list.size());

        for (Map.Entry<String, Double> entry : list) {
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        Set<Map.Entry<String, Double>> entrySorted = sortedHashMap.entrySet();

        this.pontuacaoPersonagens.clear();

        for (Map.Entry<String, Double> entry : entrySorted) {
            this.pontuacaoPersonagens.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Retorna a pergunta mais restringente
     *
     * @return
     */
    public String getPerguntaMaisRestringente() throws JSONException {

        String pergunta = "";
        JSONArray jsonPerguntas = jsonUtils.getSingleton().getJsonPerguntas();
        JSONObject questions = jsonPerguntas.getJSONObject(0);

        Iterator keys = questions.keys();
        int pontuacaoAtual = 0;
        String chaveMaisRestringente = "";

        while (keys.hasNext()) {

            String chave = (String) keys.next();
            JSONArray persosWhereSim = jsonUtils.buscaPersonagensPorChavePergunta(chave, "sim");
            JSONArray persosWhereNao = jsonUtils.buscaPersonagensPorChavePergunta(chave, "não");

            int qtdSim = persosWhereSim.length();
            int qtdNao = persosWhereNao.length();

            int pontucaoCalculada = (qtdSim + 1) * (qtdNao + 1);
            if (pontuacaoAtual < pontucaoCalculada) {
                pontuacaoAtual = pontucaoCalculada;
                chaveMaisRestringente = chave;
            }

        }

        pergunta = jsonUtils.getPergunta(chaveMaisRestringente);
        return chaveMaisRestringente + ";" + pergunta;
    }

    /**
     *
     * @param chave
     * @param respostaJogador
     * @throws JSONException
     */
    public void calculaPontuacaoParaPersonagens(String chave, int respostaJogadorCode) throws JSONException {
        JSONArray personagens = jsonUtils.getSingleton().getJsonPersonagens();
        String respostaJogador = responseByResponseCode.get(respostaJogadorCode);

        for (int i = 0; i < personagens.length(); ++i) {

            JSONObject personagem = personagens.getJSONObject(i);
            String resposta = personagem.getString(chave);
            String nomePersonagem = personagem.getString("Personagem");

            double pontuacao = getPontuacao(respostaJogador, resposta);

            //On vérifie si le perso a déjà un score
            if (pontuacaoPersonagens.containsKey(nomePersonagem)) {
                pontuacao += pontuacaoPersonagens.get(nomePersonagem);
            }
            pontuacaoPersonagens.put(nomePersonagem, pontuacao);

        }

    }

    /**
     * Pontuação para a pergunta de acordo com a resposta dada pelo jogador e a
     * resposta correta do personagem
     *
     * @param respostaJogador
     * @param respostaPersonagem
     * @return
     */
    private Double getPontuacao(String respostaJogador, String respostaPersonagem) {
        double score = 0;
        if (respostaJogador.equals(respostaPersonagem)) {
            score = 1;
        } else {
            score = -1;
        }
        return score;
    }

    /**
     * Elimina pergunta que já foi feita
     * @param chave
     * @throws JSONException 
     */
    public void eliminaPergunta(String chave) throws JSONException {
        jsonUtils.excluiPerguntaPorChave(chave);
    }

    /*
        * Deleta personagem da lista de pontuação 
     */
    public void eliminaPersonagemListaPontuacao(String nome) {
        pontuacaoPersonagens.remove(nome);
    }

    /*
	 * Adiciona personagem à base de conhecimento
	 * 
	 * @return void
     */
    public void adicionaNovoPersonagem(HashMap<String, String> caracteristicas) throws IOException {
        JSONObject personagem = new JSONObject(caracteristicas);
        jsonUtils.adicionaPersonagem(personagem);
    }

    /**
     * Adiciona personagem à lista de personagens aliminados
     *
     * @param nome
     */
    public void adicionaPersonagemEliminado(String nome) {
        //Adiciona se ainda não existir na lista
        if (!listPersosEliminados.contains(nome)) {
            listPersosEliminados.add(nome);
        }
    }

    /**
     * Retorna lista de personagens eliminados
     *
     * @return
     */
    public ArrayList<String> getPersosEliminados() {
        return listPersosEliminados;
    }


    /*
	 * Retorna número de personagens excluídos
	 * 
	 * @return 
     */
    public int qtdPersonagensEliminados() {
        return listPersosEliminados.size();
    }

    /*
	 * Limpa lista de personagens eliminados
     */
    public void clearPersosEliminados() {
        listPersosEliminados.clear();
    }

    /*
	 * Retorna número de personagens que ainda não foram eliminados
     */
    public int qtdPersonagensRestantes() {
        return jsonUtils.getSingleton().getJsonPersonagens().length() - listPersosEliminados.size();

    }

    /**
     * Retorna probabilidade final de acerto
     *
     * @param qtdQuestoes
     * @return
     */
    public String getPorcentagemAcerto(double qtdQuestoes) {
        double pontuacaoPersonagem = 0;
        for (Map.Entry<String, Double> entry : pontuacaoPersonagens.entrySet()) {
            if (pontuacaoPersonagem < entry.getValue()) {
                pontuacaoPersonagem = entry.getValue();
            }
        }
        pontuacaoPersonagem = (pontuacaoPersonagem / (double) (qtdQuestoes));
        pontuacaoPersonagem *= 100;
        DecimalFormat df = new DecimalFormat("0.00");
        String porcentagemPontuacaoFinal = df.format(pontuacaoPersonagem);
        return (porcentagemPontuacaoFinal);
    }

    /*
	 * Retorna personagem com a maior pontuação	 * 
	 * @return
     */
    public String getPersonagemMaiorPontuacao() {

        String nome = "";
        double pontuacao = 0;

        for (Map.Entry<String, Double> entry : pontuacaoPersonagens.entrySet()) {
            if (pontuacao < entry.getValue()) {
                pontuacao = entry.getValue();
                nome = entry.getKey();
            }
        }
        return nome;
    }

    /*
        * Retorna se há pelo menos 1 personagem com pontuação maior que a pontuação setada na constante PROPOSAL_THRESOLD
     */
    public boolean existePersonagemParaPropor(double qtdPerguntas) {
        boolean existe = false;
        double porcentagem = 0;

        for (Map.Entry<String, Double> entry : pontuacaoPersonagens.entrySet()) {

            porcentagem = ((double) entry.getValue() / (double) (qtdPerguntas)) * (double) 100;

            if (porcentagem >= (double) PROPOSAL_THRESOLD) {
                existe = true;
                break;
            }
        }
        return existe;
    }
}
