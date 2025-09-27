package com.example.projeto_desafio_computacional;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.Locale;

public class FrutasActivity extends AppCompatActivity {

    // --- VARIÁVEIS DE ESTADO ---
    private DatabaseHelper db;
    private final String CATEGORY_NAME = "fruta";
    private final int MAX_ROUNDS = 10;

    // Variáveis do Jogo
    private String correctWord = "";
    private String hint1 = "";
    private String hint2 = "";
    private String hint3 = "";
    private int currentRound = 0;
    private int currentHintLevel = 0;
    private boolean gameInProgress = false;

    // Variáveis do Cronômetro
    private Handler timerHandler;
    private long startTime = 0;
    private TextView txtTimer;

    // Variáveis da UI
    private TextView txtCategoryTitle;
    private TextView txtHint;
    private TextView txtFeedback;
    private TextView txtRoundCounter;
    private EditText editGuess;
    private Button btnStart;
    private Button btnHint;
    private Button btnSubmit;


    // --- CRONÔMETRO LÓGICA ---
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            // Formata o tempo e atualiza a UI
            txtTimer.setText(String.format(Locale.getDefault(), "Tempo: %02d:%02d", minutes, seconds));

            // Agenda a próxima execução para 1 segundo depois
            timerHandler.postDelayed(this, 1000);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frutas);

        // 1. Inicializa UI Components
        txtTimer = findViewById(R.id.txtTimer);
        txtCategoryTitle = findViewById(R.id.txtCategoryTitle);
        txtHint = findViewById(R.id.txtHint);
        txtFeedback = findViewById(R.id.txtFeedback);
        txtRoundCounter = findViewById(R.id.txtRoundCounter);
        editGuess = findViewById(R.id.editGuess);
        btnStart = findViewById(R.id.btnStart);
        btnHint = findViewById(R.id.btnHint);
        btnSubmit = findViewById(R.id.btnSubmit);

        // 2. Inicializa Banco de Dados e Cronômetro
        db = new DatabaseHelper(this);
        timerHandler = new Handler();

        // Garante que o DB seja copiado antes de usar
        initializeDatabase();

        // Define o texto inicial da categoria e rodada
        txtCategoryTitle.setText("Categoria: " + CATEGORY_NAME.toUpperCase());
        txtRoundCounter.setText(String.format(Locale.getDefault(), "Rodada: %d/%d", currentRound, MAX_ROUNDS));


        // 3. Listeners dos Botões

        // Botão INICIAR/PRÓXIMA RODADA
        btnStart.setOnClickListener(v -> handleStartButton());

        // Botão DICA
        btnHint.setOnClickListener(v -> showNextHint());

        // Botão TENTAR
        btnSubmit.setOnClickListener(v -> checkGuess());

        // Botão "Voltar ao Menu"
        findViewById(R.id.btnVoltarMenu).setOnClickListener(v -> finishGame());
    }

    // --- MÉTODOS DE CONTROLE DO JOGO ---

    /** Inicializa o banco de dados. CHAVE para o sucesso. */
    private void initializeDatabase() {
        try {
            db.createAndOpenDatabase();
            Log.i("DB_INIT", "Banco de dados copiado/aberto com sucesso.");
        } catch (IOException e) {
            Log.e("DB_INIT", "ERRO FATAL: Falha ao copiar o banco de dados.", e);
            Toast.makeText(this, "ERRO: Falha ao carregar o banco de dados.", Toast.LENGTH_LONG).show();
            btnStart.setEnabled(false); // Desabilita o jogo se o DB falhar
        }
    }

    /** Lógica do botão INICIAR/PRÓXIMA RODADA */
    private void handleStartButton() {
        if (!gameInProgress) {
            // Inicia o jogo
            startTime = System.currentTimeMillis();
            timerHandler.postDelayed(timerRunnable, 0); // Começa o cronômetro
            gameInProgress = true;

            // Habilita/Desabilita botões
            btnStart.setText("PRÓXIMA RODADA");
            btnStart.setEnabled(false);
            btnSubmit.setEnabled(true);
            btnHint.setEnabled(true);
            txtFeedback.setText("");
        }

        if (currentRound < MAX_ROUNDS) {
            startNewRound();
        } else {
            // Fim do Jogo
            endGameSummary();
        }
    }

    /** Prepara os dados e a UI para a próxima rodada */
    private void startNewRound() {
        currentRound++;
        currentHintLevel = 0; // Reseta as dicas
        editGuess.setText("");
        txtFeedback.setText("");
        txtHint.setText("Carregando pista...");
        txtRoundCounter.setText(String.format(Locale.getDefault(), "Rodada: %d/%d", currentRound, MAX_ROUNDS));
        editGuess.setEnabled(true); // Garante que o input esteja habilitado
        btnSubmit.setEnabled(true);
        btnHint.setEnabled(true);

        // 1. Puxa a palavra aleatória do DB
        String[] randomData = db.getRandomDataByCategory(CATEGORY_NAME);

        if (randomData != null && randomData.length >= 7) {
            // ATENÇÃO: Os índices [3], [4], [5], [6] dependem da ordem das colunas no seu SELECT * JOIN.
            // Aqui presumimos que: [3]=PALAVRA, [4]=DICA1, [5]=DICA2, [6]=DICA3
            correctWord = randomData[3].trim().toLowerCase(Locale.getDefault());
            hint1 = randomData[4].trim();
            hint2 = randomData[5].trim();
            hint3 = randomData[6].trim();

            Log.d("GAME_LOG", "Palavra Correta: " + correctWord);

            // Exibe a primeira pista (Nível 1)
            showNextHint();
        } else {
            // Falha ao carregar dados
            txtHint.setText("ERRO: Banco de dados não retornou dados válidos. Cheque o Logcat.");
            btnStart.setEnabled(false);
            btnSubmit.setEnabled(false);
            btnHint.setEnabled(false);
        }
    }

    /** Controla as dicas progressivas */
    private void showNextHint() {
        currentHintLevel++;

        StringBuilder hintAccumulator = new StringBuilder("Pistas Reveladas:\n");
        boolean isLastHint = false;

        if (currentHintLevel >= 1) {
            hintAccumulator.append("- DICA 1: ").append(hint1).append("\n");
        }
        if (currentHintLevel >= 2) {
            hintAccumulator.append("- DICA 2: ").append(hint2).append("\n");
        }
        if (currentHintLevel >= 3) {
            hintAccumulator.append("- DICA 3: ").append(hint3).append("\n");
            isLastHint = true;
        }

        // Atualiza o TextView com todas as dicas acumuladas
        txtHint.setText(hintAccumulator.toString().trim());

        // Desabilita o botão se atingiu o limite de dicas
        if (isLastHint) {
            btnHint.setEnabled(false);
            Toast.makeText(this, "Todas as dicas foram reveladas.", Toast.LENGTH_SHORT).show();
        }
    }

    /** Verifica a tentativa do usuário */
    private void checkGuess() {
        if (correctWord.isEmpty()) return;

        String guess = editGuess.getText().toString().trim().toLowerCase(Locale.getDefault());

        if (guess.isEmpty()) {
            txtFeedback.setText("Digite sua tentativa!");
            txtFeedback.setTextColor(0xFFF44336); // Vermelho
            return;
        }

        if (guess.equals(correctWord)) {
            // ACERTOU
            txtFeedback.setText("CORRETO! Você acertou a palavra!");
            txtFeedback.setTextColor(0xFF4CAF50); // Verde
            prepareForNextRound();
        } else {
            // ERROU
            txtFeedback.setText("INCORRETO! Tente novamente ou peça outra dica.");
            txtFeedback.setTextColor(0xFFF44336); // Vermelho
        }
    }

    /** Limpa e prepara a UI após um acerto para a próxima rodada ou fim do jogo */
    private void prepareForNextRound() {
        editGuess.setEnabled(false); // Desabilita o input
        btnHint.setEnabled(false);
        btnSubmit.setEnabled(false);

        if (currentRound < MAX_ROUNDS) {
            btnStart.setEnabled(true);
            btnStart.setText("PRÓXIMA RODADA (" + (currentRound + 1) + "/" + MAX_ROUNDS + ")");
        } else {
            endGameSummary();
        }
    }

    /** Encerra o jogo e mostra o resumo */
    private void endGameSummary() {
        gameInProgress = false;
        timerHandler.removeCallbacks(timerRunnable); // Para o cronômetro
        btnStart.setEnabled(false);
        btnHint.setEnabled(false);
        btnSubmit.setEnabled(false);
        editGuess.setEnabled(false);

        String finalTime = txtTimer.getText().toString();

        txtHint.setText("FIM DO JOGO!");
        txtFeedback.setText("Você completou " + MAX_ROUNDS + " rodadas. Tempo total: " + finalTime);
        txtFeedback.setTextColor(0xFF3F51B5); // Azul
    }

    /** Lógica do botão Voltar */
    private void finishGame() {
        if (timerHandler != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
        Intent intent = new Intent(FrutasActivity.this, MenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Garante que o cronômetro pare se a Activity não estiver mais visível
        if (timerHandler != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }
}