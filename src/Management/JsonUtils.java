package Management;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.primefaces.json.JSONArray;
import org.primefaces.json.JSONException;
import org.primefaces.json.JSONObject;

/**
 * JsonUtils, usado para gerenciar diferentes JSONs da aplicação
 */
public class JsonUtils {

    private JsonBaseConhecimento jsonBaseConhecimento;

    public JsonBaseConhecimento getSingleton() {
        if (this.jsonBaseConhecimento == null) {
            this.jsonBaseConhecimento = new JsonBaseConhecimento();
        }
        return jsonBaseConhecimento;
    }

    /**
     * buscaPersonagensPorChavePergunta retorna coleção de personagems de acordo
     * com a pergunta passada por parâmetro
     *
     * @param chave chave da pergunta
     * @param respostaEsperada resposta esperada para a pergunta
     * @return
     * @throws JSONException
     */
    public JSONArray buscaPersonagensPorChavePergunta(String chave, String respostaEsperada) throws JSONException {
        JSONArray personagensArray = new JSONArray();
        // Carrega personagens
        JSONArray personagens = jsonBaseConhecimento.getJsonPersonagens();

        // Seleciona personagens que tenham a resposta esperada para a pergunta
        for (int i = 0; i < personagens.length(); i++) {
            JSONObject personagem = personagens.getJSONObject(i);
            String resposta = personagem.getString(chave);
            if (resposta.equals(respostaEsperada)) {
                personagensArray.put(personagem);
            }
        }
        return personagensArray;
    }

    /**
     * adicionaPersonagem, adiciona personagens e suas perguntas
     *
     * @param personagem JSONObject representando personagem já com suas
     * perguntas
     */
    public void adicionaPersonagem(JSONObject personagem) {
        JSONArray newArray = jsonBaseConhecimento.getJsonPersonagens();
        newArray.put(personagem);
        jsonBaseConhecimento.setJsonPersonagens(newArray);
    }

    /**
     * Adiciona nova pergunta em JSON de perguntas
     *
     * @param chave
     * @param pergunta
     * @throws JSONException
     */
    public void adicionaPergunta(Integer chave, String pergunta) throws JSONException {
        JSONArray newJsonArray = jsonBaseConhecimento.getJsonBasePerguntas();
        newJsonArray.getJSONObject(0).put(chave.toString(), pergunta);
        jsonBaseConhecimento.setJsonPerguntas(newJsonArray);
    }

    /**
     * getPergunta retorna pergunta de acordo com chave
     *
     * @param chave uma key JSON
     * @return a toString question
     * @throws JSONException
     */
    public String getPergunta(String chave) throws JSONException {
        return jsonBaseConhecimento.getJsonPerguntas().getJSONObject(0).getString(chave);
    }

    /**
     * Retorna JSONObject de personagem de acordo com nome
     *
     * @param nome
     * @return
     * @throws JSONException
     */
    public JSONObject getPersonagemPorNome(String nome) throws JSONException {
        JSONObject personagemRetorno = null;
        for (int i = 0; i < jsonBaseConhecimento.getJsonPersonagens().length(); i++) {
            JSONObject personagem = jsonBaseConhecimento.getJsonPersonagens().getJSONObject(i);
            String nomePersonagem = personagem.getString("Personagem");
            if (nomePersonagem.equals(nome)) {
                personagemRetorno = personagem;
                break;
            }
        }
        return personagemRetorno;
    }

    /**
     * Deleta personagem de acordo com nome
     *
     * @param nome
     * @throws JSONException
     */
    public void excluiPersonagemPorNome(String nome) throws JSONException {
        JSONArray arrayTemp = new JSONArray();
        for (int i = 0; i < jsonBaseConhecimento.getJsonPersonagens().length(); i++) {
            JSONObject personagem = jsonBaseConhecimento.getJsonPersonagens().getJSONObject(i);
            String nomePersonagem = personagem.getString("Personagem");
            if (!nomePersonagem.equals(nome)) {
                arrayTemp.put(personagem);
            }
        }
        jsonBaseConhecimento.setJsonPersonagens(arrayTemp);
    }

    public JSONArray getJsonPerguntas() {
        return jsonBaseConhecimento.getJsonPerguntas();
    }

    public JSONArray getJsonPersonagens() {
        return jsonBaseConhecimento.getJsonPersonagens();
    }

    /**
     * Deleta lista de personagens
     *
     * @param nomesPersonagens
     * @throws JSONException
     */
    public void excluiPersonagens(ArrayList<String> nomesPersonagens) throws JSONException {
        JSONArray arrayTemp = new JSONArray();
        for (String nome : nomesPersonagens) {
            for (int i = 0; i < jsonBaseConhecimento.getJsonPersonagens().length(); i++) {
                JSONObject personagem = jsonBaseConhecimento.getJsonPersonagens().getJSONObject(i);
                String nomePersonagem = personagem.getString("Personagem");
                if (!nomePersonagem.equals(nome)) {
                    arrayTemp.put(personagem);
                }
            }
        }
        jsonBaseConhecimento.setJsonPersonagens(arrayTemp);
    }

    /**
     * Remove pergunta pela sua chave
     *
     * @param chave
     * @throws JSONException
     */
    public void excluiPerguntaPorChave(Integer chave) throws JSONException {
        jsonBaseConhecimento.getJsonPerguntas().getJSONObject(0).remove(chave.toString());
    }

    /**
     * Retorna <code>TRUE</code> se personagem já existe e <code>FALSE</code>
     * caso contrário
     *
     * @param nome
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public boolean personagemJaExiste(String nome) throws JSONException, IOException {
        boolean response = false;
        JsonReader jsonReader = new JsonReader();
        JSONArray personagens = new JSONArray(jsonReader.lerJSONBaseConhecimento(new File(jsonBaseConhecimento.jsonPersonagensFile)));

        for (int i = 0; i < personagens.length(); i++) {
            JSONObject personagem = null;
            try {
                String nomePersonagem = null;
                personagem = personagens.getJSONObject(i);

                nomePersonagem = personagem.getString("Personagem");
                if (nomePersonagem.toUpperCase().equals(nome)) {
                    response = true;
                    break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return response;
    }

    public Integer getNovaChave() {
        try {
            return jsonBaseConhecimento.getJsonBasePerguntas().getJSONObject(0).length();
        } catch (JSONException ex) {
            Logger.getLogger(JsonUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public JsonBaseConhecimento getJsonBaseConhecimento() {
        if (jsonBaseConhecimento == null) {
            jsonBaseConhecimento = new JsonBaseConhecimento();
        }
        return jsonBaseConhecimento;
    }

}
