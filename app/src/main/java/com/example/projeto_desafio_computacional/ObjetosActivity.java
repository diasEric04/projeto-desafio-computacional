package com.example.projeto_desafio_computacional;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ObjetosActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private final String CATEGORY_NAME = "objeto";
    private final int MAX_ROUNDS = 10;

    // Variáveis do Jogo
    private String correctWord = "";
    private String hint1 = "";
    private String hint2 = "";
    private String hint3 = "";
    private int currentRound = 0;
    private int currentHintLevel = 0;
    private boolean gameInProgress = false;
    private String syllableClassification = "";

    // Variáveis de Pontuação
    private int totalPontos = 0;
    private int totalDicasUsadas = 0;
    private int pontosPorAcerto = 50;
    private int bonusTempo = 50;
    private int tempoPorRodada = 0;
    private List<Integer> temposRodadas = new ArrayList<>();
    private List<Integer> pontosRodadas = new ArrayList<>();

    // Variáveis do Cronômetro
    private Handler timerHandler;
    private Handler bonusTimerHandler;
    private long startTime = 0;
    private long rodadaStartTime = 0;
    private TextView txtTimer;
    private TextView txtPontuacao;
    private TextView txtBonus;
    private TextView txtPontosAcerto;
    private TextView txtRoundCounter;

    // Variáveis da UI
    private TextView txtCategoryTitle;
    private TextView txtSyllabicClass;
    private TextView txtHint;
    private TextView txtFeedback;
    private EditText editGuess;
    private Button btnStart;
    private Button btnHint;
    private Button btnSubmit;
    private Button btnSkip;

    // Runnable do Cronômetro Principal
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            txtTimer.setText(String.format(Locale.getDefault(), "Tempo: %02d:%02d", minutes, seconds));
            timerHandler.postDelayed(this, 1000);
        }
    };

    // Runnable do Bônus de Tempo
    private final Runnable bonusTimerRunnable = new Runnable() {
        @Override
        public void run() {
            long elapsed = (System.currentTimeMillis() - rodadaStartTime) / 1000;

            if (elapsed >= 10 && elapsed < 20) {
                bonusTempo = 40;
            } else if (elapsed >= 20 && elapsed < 30) {
                bonusTempo = 30;
            } else if (elapsed >= 30 && elapsed < 40) {
                bonusTempo = 20;
            } else if (elapsed >= 40 && elapsed < 50) {
                bonusTempo = 10;
            } else if (elapsed >= 50) {
                bonusTempo = 0;
            }

            txtBonus.setText("+" + bonusTempo);
            bonusTimerHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_objetos);

        // Inicializa UI Components
        txtTimer = findViewById(R.id.txtTimer);
        txtPontuacao = findViewById(R.id.txtPontuacao);
        txtBonus = findViewById(R.id.txtBonus);
        txtPontosAcerto = findViewById(R.id.txtPontosAcerto);
        txtCategoryTitle = findViewById(R.id.txtCategoryTitle);
        txtSyllabicClass = findViewById(R.id.txtSyllabicClass);
        txtHint = findViewById(R.id.txtHint);
        txtFeedback = findViewById(R.id.txtFeedback);
        txtRoundCounter = findViewById(R.id.txtRoundCounter);
        editGuess = findViewById(R.id.editGuess);
        btnStart = findViewById(R.id.btnStart);
        btnHint = findViewById(R.id.btnHint);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnSkip = findViewById(R.id.btnSkip);

        // Inicializa Banco de Dados e Handlers
        db = new DatabaseHelper(this);
        timerHandler = new Handler();
        bonusTimerHandler = new Handler();

        initializeDatabase();

        txtCategoryTitle.setText("Categoria: " + CATEGORY_NAME.toUpperCase());
        txtRoundCounter.setText(String.format(Locale.getDefault(), "Rodada: %d/%d", currentRound, MAX_ROUNDS));
        txtPontuacao.setText("Pontos: 0");
        txtBonus.setText("+0");
        txtPontosAcerto.setText("50 pts");

        // Listeners dos Botões
        btnStart.setOnClickListener(v -> handleStartButton());
        btnHint.setOnClickListener(v -> showNextHint());
        btnSubmit.setOnClickListener(v -> checkGuess());
        btnSkip.setOnClickListener(v -> skipRound());
        findViewById(R.id.btnVoltarMenu).setOnClickListener(v -> finishGame());

        editGuess.setOnEditorActionListener((v, actionId, event) -> {
            // Verifica se a ação disparada foi "Done" (Concluído)
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Chama a mesma função que o botão TENTAR
                checkGuess();

                // Oculta o teclado após a tentativa para desobstruir a tela
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                // Retorna true para indicar que o evento foi consumido (tratado)
                return true;
            }
            // Retorna false para permitir que outras ações de teclado funcionem normalmente
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

    private void handleStartButton() {
        if (!gameInProgress) {
            startTime = System.currentTimeMillis();
            timerHandler.postDelayed(timerRunnable, 0);
            gameInProgress = true;

            btnStart.setText("PRÓXIMA RODADA");
            btnStart.setEnabled(false);
            btnSubmit.setEnabled(true);
            btnHint.setEnabled(true);
            btnSkip.setEnabled(true);
            txtFeedback.setText("");
        }

        if (currentRound < MAX_ROUNDS) {
            startNewRound();
        } else {
            endGameSummary();
        }
    }

    private void startNewRound() {
        currentRound++;
        currentHintLevel = 0;
        pontosPorAcerto = 50;
        bonusTempo = 50;
        editGuess.setText("");
        txtFeedback.setText("");
        txtHint.setText("Carregando pista...");
        txtRoundCounter.setText(String.format(Locale.getDefault(), "Rodada: %d/%d", currentRound, MAX_ROUNDS));
        txtPontosAcerto.setText(pontosPorAcerto + " pts");
        txtBonus.setText("+" + bonusTempo);
        txtSyllabicClass.setText("Classificação: Carregando...");
        btnStart.setEnabled(false);
        editGuess.setEnabled(true);
        btnSubmit.setEnabled(true);
        btnHint.setEnabled(true);

        // Inicia timer da rodada para bônus
        rodadaStartTime = System.currentTimeMillis();
        bonusTimerHandler.postDelayed(bonusTimerRunnable, 0);

        String[] randomData = db.getRandomDataByCategory(CATEGORY_NAME);

        if (randomData != null && randomData.length >= 7) {
            correctWord = randomData[3].trim().toLowerCase(Locale.getDefault());
            hint1 = randomData[4].trim();
            hint2 = randomData[5].trim();
            hint3 = randomData[6].trim();

            // Nova classificação
            syllableClassification = db.getClass(correctWord);
            txtSyllabicClass.setText("Classificação: " + syllableClassification.toUpperCase());

            Log.d("GAME_LOG", "Palavra Correta: " + correctWord);
            showNextHint();
        } else {
            txtHint.setText("ERRO: Banco de dados não retornou dados válidos.");
            txtSyllabicClass.setText("Classificação: ERRO");
            btnStart.setEnabled(false);
            btnSubmit.setEnabled(false);
            btnHint.setEnabled(false);
        }
    }

    private void showNextHint() {
        currentHintLevel++;

        if (currentHintLevel == 2) {
            pontosPorAcerto = 30;
            txtPontosAcerto.setText(pontosPorAcerto + " pts");
        } else if (currentHintLevel >= 3) {
            pontosPorAcerto = 10;
            txtPontosAcerto.setText(pontosPorAcerto + " pts");
        }

        StringBuilder hintAccumulator = new StringBuilder("Pistas Reveladas:\n");
        boolean isLastHint = false;

        if (currentHintLevel >= 1) {
            hintAccumulator.append("- DICA 1: ").append(hint1).append("\n");
        }
        if (currentHintLevel >= 2) {
            hintAccumulator.append("- DICA 2: ").append(hint2).append("\n");
            totalDicasUsadas++;
        }
        if (currentHintLevel >= 3) {
            hintAccumulator.append("- DICA 3: ").append(hint3).append("\n");
            isLastHint = true;
            totalDicasUsadas++;
        }

        txtHint.setText(hintAccumulator.toString().trim());

        if (isLastHint) {
            btnHint.setEnabled(false);
            Toast.makeText(this, "Todas as dicas foram reveladas.", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkGuess() {
        if (correctWord.isEmpty()) return;

        String guess = editGuess.getText().toString().trim().toLowerCase(Locale.getDefault());

        if (guess.isEmpty()) {
            txtFeedback.setText("Digite sua tentativa!");
            txtFeedback.setTextColor(0xFFF44336);
            return;
        }

        if (guess.equals(correctWord)) {
            // Calcula tempo da rodada
            int tempoRodada = (int) ((System.currentTimeMillis() - rodadaStartTime) / 1000);
            temposRodadas.add(tempoRodada);

            // Calcula pontos da rodada
            int pontosRodada = pontosPorAcerto + bonusTempo;
            pontosRodadas.add(pontosRodada);
            totalPontos += pontosRodada;

            txtFeedback.setText("CORRETO! +" + pontosRodada + " pontos!");
            txtFeedback.setTextColor(0xFF4CAF50);
            txtPontuacao.setText("Pontos: " + totalPontos);

            // Para o timer de bônus da rodada
            bonusTimerHandler.removeCallbacks(bonusTimerRunnable);

            prepareForNextRound();
        } else {
            txtFeedback.setText("INCORRETO! Tente novamente ou peça outra dica.");
            txtFeedback.setTextColor(0xFFF44336);
        }
    }

    private void prepareForNextRound() {
        editGuess.setEnabled(false);
        btnHint.setEnabled(false);
        btnSubmit.setEnabled(false);

        if (currentRound < MAX_ROUNDS) {
            btnStart.setEnabled(true);
            btnStart.setText("PRÓXIMA RODADA (" + (currentRound + 1) + "/" + MAX_ROUNDS + ")");
        } else {
            endGameSummary();
        }
    }

    private void skipRound() {
        if (!gameInProgress || currentRound >= MAX_ROUNDS) return;

        // 1. Calcula tempo (para estatísticas), mas não pontua
        int tempoRodada = (int) ((System.currentTimeMillis() - rodadaStartTime) / 1000);
        temposRodadas.add(tempoRodada);
        pontosRodadas.add(0); // Adiciona 0 pontos para a rodada pulada

        // 2. Para o timer de bônus
        bonusTimerHandler.removeCallbacks(bonusTimerRunnable);

        // 3. Feedback visual
        txtFeedback.setText("PALAVRA PULADA: A resposta era '" + correctWord.toUpperCase(Locale.getDefault()) + "'.");
        txtFeedback.setTextColor(0xFF9E9E9E); // Cinza

        // 4. Prepara para a próxima rodada
        prepareForNextRound();
    }


    private void endGameSummary() {
        gameInProgress = false;
        timerHandler.removeCallbacks(timerRunnable);
        bonusTimerHandler.removeCallbacks(bonusTimerRunnable);
        btnStart.setEnabled(false);
        btnHint.setEnabled(false);
        btnSubmit.setEnabled(false);
        editGuess.setEnabled(false);

        // Salva pontuação no banco
        db.saveGameScore(CATEGORY_NAME, totalPontos, totalPontos);

        // Calcula estatísticas
        int tempoTotal = 0;
        int menorTempo = Integer.MAX_VALUE;
        int maiorTempo = 0;

        for (int tempo : temposRodadas) {
            tempoTotal += tempo;
            if (tempo < menorTempo) menorTempo = tempo;
            if (tempo > maiorTempo) maiorTempo = tempo;
        }

        int tempoMedio = temposRodadas.isEmpty() ? 0 : tempoTotal / temposRodadas.size();

        showGameSummaryDialog(tempoMedio, menorTempo, maiorTempo);
    }

    private void showGameSummaryDialog (int tempoMedio, int menorTempo, int maiorTempo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("FIM DO JOGO!");

        // CORREÇÃO: Calcular estatísticas corretamente
        int totalPontosAcerto = 0;
        int totalPontosBonus = 0;

        for (int i = 0; i < pontosRodadas.size(); i++) {
            int pontosBase = pontosRodadas.get(i) - 50; // Remove bônus base de 50
            totalPontosBonus += 50; // Cada rodada tem bônus base de 50


            totalPontosAcerto += pontosBase;
        }

        String message = String.format(
                "Pontuação Total: %d pontos\n\n" +
                        "📊 Estatísticas:\n" +
                        "• Dicas Usadas: %d/20\n" +
                        "• Pontos por Acerto: %d\n" +
                        "• Pontos Bônus: %d\n" +
                        "• Tempo Médio: %d segundos\n" +
                        "• Menor Tempo: %d segundos\n" +
                        "• Maior Tempo: %d segundos",
                totalPontos,
                totalDicasUsadas,  // Dicas usadas no formato X/20
                totalPontosAcerto, // Pontos somente por acerto
                totalPontosBonus,  // Pontos somente por bônus
                tempoMedio,
                menorTempo,
                maiorTempo
        );

        builder.setMessage(message);
        builder.setPositiveButton("FECHAR", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void finishGame() {
        if (timerHandler != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
        if (bonusTimerHandler != null) {
            bonusTimerHandler.removeCallbacks(bonusTimerRunnable);
        }
        Intent intent = new Intent(ObjetosActivity.this, MenuActivity.class);
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
        if (bonusTimerHandler != null) {
            bonusTimerHandler.removeCallbacks(bonusTimerRunnable);
        }
    }
}