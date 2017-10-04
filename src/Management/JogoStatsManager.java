package Management;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import org.primefaces.json.JSONArray;
import org.primefaces.json.JSONException;
import org.primefaces.json.JSONObject;

public class JogoStatsManager {

    private final JsonWriter jsonWriter;
    private final JsonBaseConhecimento jsonSingleton;
    
    //Lista contendo todas as estatísticas de todos os personagens
    private final ArrayList<JSONObject> listStatsPersonagens = new ArrayList<>();

    public JogoStatsManager() {
        jsonWriter = new JsonWriter();
        jsonSingleton = new JsonBaseConhecimento();
        preencheEstatisticasPersonagem();
    }

    public ArrayList<JSONObject> getListStatsPersonagens() {
        return listStatsPersonagens;
    }

    /**
     * Ordena personagens por qtd jogada
     */
    public void ordenarPersonagens() {
        Collections.sort(listStatsPersonagens, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject first, JSONObject second) {
                int returnValue = 0;
                try {
                    returnValue = second.getInt("qtdJogada") - first.getInt("qtdJogada");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return returnValue;
            }
        });
    }

    /**
     * Insere um novo jogo ao arquivo de estatisticas
     *
     * @param personagem 
     * @param data 
     * @param achou 
     */
    public void insereJogo(String personagem, Date data, boolean achou) {
        JSONArray stats = jsonSingleton.getJsonEstatisticas();
        JSONObject jogoStat = new JSONObject();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy 'às' hh:mm:ss");

        try {
            jogoStat.put("personagem", personagem);
            jogoStat.put("data", sdf.format(data));
            jogoStat.put("achou", achou);
            stats.put(jogoStat);

            jsonWriter.writeJson(stats.toString(), jsonSingleton.jsonStatsFile);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retorna qtd de vezes que o jogo foi jogado
     *
     * @return int
     */
    public Integer getQtdJogada() {
        JSONArray jsonStats = jsonSingleton.getJsonEstatisticas();
        if (jsonStats == null) {
            return 0;
        }
        return jsonStats.length();
    }

    /**
     * Retorna qtd de vezes que o jogo conseguiu adivinhar o personagem
     *
     * @return int
     */
    public Integer getQtdJogosGanhos() {
        JSONArray jsonStats = jsonSingleton.getJsonEstatisticas();
        int qtdAchou = 0;
        for (int i = 0; i < jsonStats.length(); i++) {

            JSONObject jogo;
            try {
                jogo = (JSONObject) jsonStats.get(i);
                if (jogo.getBoolean("achou") == true) {
                    qtdAchou++;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return qtdAchou;
    }

    /**
     * Retorna personagem que mais é utilizado no jogo
     *
     * @return JSONObject
     */
    public JSONObject getPersonagemMaisJogado() {
        ordenarPersonagens();
        return listStatsPersonagens.get(0);
    }

    /**
     * Preenche lista de estatisticas de todos os jogos
     */
    private void preencheEstatisticasPersonagem() {
        JSONArray jsonStats = jsonSingleton.getJsonEstatisticas();
        JSONArray jsonPersonagens = jsonSingleton.getJsonPersonagens();

        if (jsonStats.length() != 0) {
            for (int i = 0; i < jsonPersonagens.length(); i++) {

                int qtdJogada = 0;
                JSONObject personagem = null;

                for (int j = 0; j < jsonStats.length(); j++) {
                    JSONObject jogo;

                    try {
                        jogo = (JSONObject) jsonStats.get(j);
                        personagem = (JSONObject) jsonPersonagens.get(i);
                        if (jogo.getString("personagem").equals(personagem.getString("Personagem"))) {
                            qtdJogada++;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    JSONObject personagemStat = new JSONObject();
                    personagemStat.put("personagem", personagem.getString("Personagem"));
                    personagemStat.put("qtdJogada", qtdJogada);
                    this.listStatsPersonagens.add(personagemStat);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Retorna dados do último jogo
     * @return 
     */
    public JSONObject getUltimoJogo() {
        JSONArray jsonStats = jsonSingleton.getJsonEstatisticas();
        JSONObject ultimoJogo = null;
        try {
            ultimoJogo = (JSONObject) jsonStats.get(jsonStats.length() - 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ultimoJogo;
    }
}
