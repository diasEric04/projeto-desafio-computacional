package com.example.projeto_desafio_computacional;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.Normalizer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.HashMap;

import java.util.Locale;



import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;



public class TrissilabaActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private final long GAME_DURATION_MS = 120 * 1000; // 2 minutos em milissegundos (120000ms)
    private final String classe = "trissilaba";

    // Variáveis de Estado
    private int score = 0;
    private long startTime = 0;
    private boolean gameInProgress = false;
    private final List<String> submittedWords = new ArrayList<>(); // Armazena palavras VÁLIDAS para evitar repetição

    // Variáveis do Cronômetro
    private Handler timerHandler;
    private long timeLeftMillis = GAME_DURATION_MS;
    private TextView txtTimer;

    // Variáveis da UI
    private TextView txtScore;
    private TextView txtSyllabicTarget;
    private TextView txtFeedback;
    private EditText editWordInput;
    private Button btnStart;
    private Button btnSubmit;
    private Button btnVoltarMenu;

    // verificador ortografico

    private VerificadorOrtografico verificador;

    // Runnable do Cronômetro
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            timeLeftMillis = GAME_DURATION_MS - (System.currentTimeMillis() - startTime);

            if (timeLeftMillis <= 0) {
                timeLeftMillis = 0;
                endGameSummary();
                return;
            }

            int seconds = (int) (timeLeftMillis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            txtTimer.setText(String.format(Locale.getDefault(), "Tempo: %02d:%02d", minutes, seconds));
            timerHandler.postDelayed(this, 1000);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trissilaba);

        verificador = new VerificadorOrtografico(this);

        // 1. Inicializa UI Components
        txtTimer = findViewById(R.id.txtTimer);
        txtScore = findViewById(R.id.txtScore);
        txtSyllabicTarget = findViewById(R.id.txtSyllabicTarget);
        txtFeedback = findViewById(R.id.txtFeedback);
        editWordInput = findViewById(R.id.editWordInput);
        btnStart = findViewById(R.id.btnStart);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnVoltarMenu = findViewById(R.id.btnVoltarMenu);

        db = new DatabaseHelper(this);
        timerHandler = new Handler();
        initializeDatabase();

        // Configurações iniciais da UI
        txtSyllabicTarget.setText("OBJETIVO: APENAS PALAVRAS " + classe.toUpperCase() + "S");
        txtTimer.setText(String.format(Locale.getDefault(), "Tempo: %02d:%02d", (int) (GAME_DURATION_MS / 60000), 0));
        txtScore.setText("Pontos: 0");

        // Estado inicial
        setGameControlsEnabled(false);
        btnStart.setEnabled(true);


        // 3. Listeners dos Botões
        btnStart.setOnClickListener(v -> handleStartButton());
        btnSubmit.setOnClickListener(v -> submitWord());
        btnVoltarMenu.setOnClickListener(v -> finishGame());


        // Listener para a tecla Enter/Concluído
        editWordInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitWord();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });
    }

    private void initializeDatabase() {
        try {
            db.createAndOpenDatabase();
            Log.i("DB_INIT", "Banco de dados copiado/aberto com sucesso.");
        } catch (IOException e) {
            Log.e("DB_INIT", "ERRO FATAL: Falha ao copiar o banco de dados.", e);
            Toast.makeText(this, "ERRO: Falha ao carregar o banco de dados.", Toast.LENGTH_LONG).show();
            btnStart.setEnabled(false);
        }
    }

    // --- MÉTODOS DE CONTROLE DO JOGO ---

    private void setGameControlsEnabled(boolean enabled) {
        btnSubmit.setEnabled(enabled);
        editWordInput.setEnabled(enabled);
    }

    // Método initializeDatabase removido, pois o DB não é mais usado.

    private void handleStartButton() {
        if (!gameInProgress) {
            gameInProgress = true;
            score = 0;
            submittedWords.clear(); // Limpa o histórico de palavras
            startTime = System.currentTimeMillis();
            timeLeftMillis = GAME_DURATION_MS; // Garante o reset do tempo

            // Reabilita controles e começa a contagem
            setGameControlsEnabled(true);
            btnStart.setEnabled(false);
            btnStart.setText("JOGANDO...");
            txtScore.setText("Pontos: 0");
            txtFeedback.setText("O tempo está correndo! Digite as palavras...");

            timerHandler.postDelayed(timerRunnable, 0);
        }
    }

    private String removeAcentos(String text) {
        if (text == null) return "";
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalized.replaceAll("[\\p{Mn}]", "");
    }

    private Map<String, Boolean> isWordValid(String word)  {
        Map<String, Boolean> validacoes = new HashMap<>();
        validacoes.put("silabasValido", false);
        validacoes.put("ortografiaValido", false);
        if (word.isEmpty()) return validacoes;

        int numeroSilabas = Silabador.contarSilabas(word);

        if (numeroSilabas == 3) validacoes.put("silabasValido", true);

        final Boolean[] ortograficaCorreta = new Boolean[1];
        final CountDownLatch latch = new CountDownLatch(1);

        verificador.verificarPalavra(word, (Boolean estaCorreta) -> {
            ortograficaCorreta[0] = estaCorreta;
            latch.countDown();
        });

        try {
            boolean awaitResult = latch.await(2, TimeUnit.SECONDS);
            if (!awaitResult) {
                Log.w("VALIDACAO", "Timeout na verificação ortográfica");
                return validacoes;
            }

            Log.d("ortografia", word + ": " + ortograficaCorreta[0]);

            if (ortograficaCorreta[0] == null || !ortograficaCorreta[0]) {
                return validacoes;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return validacoes;
        }
        validacoes.put("ortografiaValido", true);
        return validacoes;
    }

    private void submitWord() {
        if (!gameInProgress) return;
        String wordAcento = editWordInput.getText().toString().trim().toLowerCase(Locale.getDefault());
        String wordSemAcento = removeAcentos(editWordInput.getText().toString().trim().toLowerCase(Locale.getDefault()));
        editWordInput.setText(""); // Limpa o campo para a próxima palavra

        if (wordAcento.isEmpty()) {
            txtFeedback.setText("Digite algo!");
            return;
        }

        // 1. Checa se a palavra já foi usada
        if (submittedWords.contains(wordSemAcento)) {
            txtFeedback.setText("PALAVRA REPETIDA: '" + wordAcento.toUpperCase() + "' já foi usada.");
            txtFeedback.setTextColor(0xFFFF9800); // Laranja
            return;
        }

        // 2. Valida a palavra (chamada ao método que você preencherá)
        Map<String, Boolean> validacoes = isWordValid(wordAcento);
        Boolean silabasValido = validacoes.get("silabasValido");
        Boolean ortografiaValido = validacoes.get("ortografiaValido");
        if (silabasValido && ortografiaValido) {
            score++;
            submittedWords.add(wordSemAcento); // Adiciona ao histórico de palavras válidas
            txtScore.setText("Pontos: " + score);
            txtFeedback.setText("CORRETO! " + wordAcento.toUpperCase());
            txtFeedback.setTextColor(0xFF4CAF50); // Verde
        } else if (!silabasValido) {
            int n = Silabador.contarSilabas(wordAcento);
            String numSil;
            switch (n) {
                case 1: numSil = "monossílaba"; break;
                case 2: numSil = "dissílaba"; break;
                default: numSil = "polissílaba"; break;
            }

            txtFeedback.setText("ERRO! '" + wordAcento.toUpperCase() + "' não é " + classe.toUpperCase() + ". É " + numSil.toUpperCase());
            txtFeedback.setTextColor(0xFFF44336); // Vermelho
        } else if (!ortografiaValido) {
            txtFeedback.setText("ERRO! '" + wordAcento.toUpperCase() + "' não é uma palavra conhecida");
            txtFeedback.setTextColor(0xFFF44336); // Vermelho
        }
    }

    private void endGameSummary() {
        gameInProgress = false;
        timerHandler.removeCallbacks(timerRunnable);
        setGameControlsEnabled(false);
        btnStart.setText("REINICIAR JOGO");
        btnStart.setEnabled(true);

        db.saveGameScore(classe.toLowerCase(), score, score);

        String message = String.format(
                "Seu tempo acabou!\n" +
                        "Pontuação Final: %d palavras válidas.",
                score
        );

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("FIM DO TEMPO!");
        builder.setMessage(message);

        builder.setPositiveButton("FECHAR", (dialog, id) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void finishGame() {
        if (timerHandler != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
        Intent intent = new Intent(TrissilabaActivity.this, MenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (timerHandler != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }
}