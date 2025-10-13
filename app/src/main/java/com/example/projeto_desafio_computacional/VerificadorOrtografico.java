package com.example.projeto_desafio_computacional;

import android.content.Context;

import java.text.Normalizer;
import java.util.function.Consumer;
import java.util.HashSet;
import java.util.Set;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import android.util.Log;

public class VerificadorOrtografico {
    private Context context;
    private Set<String> dicionario;
    private boolean dicionarioCarregado = false;

    public VerificadorOrtografico(Context context) {
        this.context = context;
        carregarDicionario();
    }

    private String removeAcentos(String text) {
        if (text == null) return "";
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalized.replaceAll("[\\p{Mn}]", "");
    }

    /**
     * Carrega o dicionário do arquivo TXT na pasta assets
     */
    private void carregarDicionario() {
        dicionario = new HashSet<>();
        new Thread(() -> {
            try {
                InputStream inputStream = context.getAssets().open("dicionario.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String linha;
                int palavrasCarregadas = 0;

                while ((linha = reader.readLine()) != null) {
                    linha = linha.trim().toLowerCase();
                    if (!linha.isEmpty()) {
                        dicionario.add(linha);
                        palavrasCarregadas++;
                    }
                }
                reader.close();
                inputStream.close();

                dicionarioCarregado = true;
                Log.d("DICIONARIO", "Dicionário carregado com " + palavrasCarregadas + " palavras");

            } catch (IOException e) {
                Log.e("DICIONARIO", "Erro ao carregar dicionário: " + e.getMessage());
                // Fallback: carrega palavras básicas
                carregarDicionarioBasico();
            }
        }).start();
    }

    /**
     * Dicionário básico de fallback se o arquivo não existir
     */
    private void carregarDicionarioBasico() {
        String[] palavrasBasicas = {
                "pera", "mesa", "casa", "bola", "gato", "rato", "pato", "lua", "sol", "mar",
                "rede", "pote", "faca", "copo", "prato", "garfo", "colher", "panela", "fogo",
                "rio", "mato", "flor", "fruta", "verde", "azul", "amarelo", "preto", "branco",
                "livro", "caderno", "lapis", "caneta", "borracha", "regua", "mochila", "carta",
                "porta", "janela", "telhado", "parede", "chao", "teto", "sapato", "camisa",
                "calca", "meia", "bone", "relogio", "oculos", "anel", "pulseira", "colar"
        };

        for (String palavra : palavrasBasicas) {
            dicionario.add(palavra.toLowerCase());
        }
        dicionarioCarregado = true;
        Log.d("DICIONARIO", "Dicionário básico carregado com " + palavrasBasicas.length + " palavras");
    }

    /**
     * Verifica se uma palavra está no dicionário
     */
    public void verificarPalavra(String palavra, Consumer<Boolean> callback) {
        // Se o dicionário ainda não carregou, espera um pouco
        if (!dicionarioCarregado) {
            Log.d("VERIFICADOR", "Dicionário ainda carregando...");
            new Thread(() -> {
                try {
                    // Espera até 2 segundos pelo carregamento
                    for (int i = 0; i < 20 && !dicionarioCarregado; i++) {
                        Thread.sleep(100);
                    }

                    boolean resultado = dicionario.contains(removeAcentos(palavra.toLowerCase()));
                    callback.accept(resultado);

                } catch (InterruptedException e) {
                    callback.accept(false);
                }
            }).start();
            return;
        }

        // Verifica diretamente no dicionário
        boolean resultado = dicionario.contains(removeAcentos(palavra.toLowerCase()));
        callback.accept(resultado);
    }

}