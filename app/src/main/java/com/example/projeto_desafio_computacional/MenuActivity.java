package com.example.projeto_desafio_computacional;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class MenuActivity extends AppCompatActivity {

    // Adicionado layoutOpcoes para os novos botões
    private LinearLayout layoutOpcoes, layoutTipos, layoutClasses;

    // Removido btnClasses
    private Button btnJogar, btnJogarPorTipo, btnJogarPorClasse;
    private Button btnFrutas, btnAnimais, btnObjetos, btnMonossilabas, btnDissilabas, btnTrissilabas, btnPolissilabas, btnSobre;

    private TextView txtMaxFrutas, txtMaxAnimais, txtMaxObjetos;
    private TextView txtMaxPolissilabas, txtMaxTrissilabas, txtMaxDissilabas, txtMaxMonossilabas;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        dbHelper = new DatabaseHelper(this);
        try {
            dbHelper.copyDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }

        SQLiteDatabase db = dbHelper.openDatabase();
        db.close();

        // 1. Inicialização dos botões e layouts
        btnJogar = findViewById(R.id.btnJogar);

        // Novos botões do menu principal
        layoutOpcoes = findViewById(R.id.layoutOpcoes);
        btnJogarPorTipo = findViewById(R.id.btnJogarPorTipo);
        btnJogarPorClasse = findViewById(R.id.btnJogarPorClasse);

        // Botões e layouts existentes
        btnFrutas = findViewById(R.id.btnFrutas);
        btnAnimais = findViewById(R.id.btnAnimais);
        btnObjetos = findViewById(R.id.btnObjetos);
        btnMonossilabas = findViewById(R.id.btnMonossilabas);
        btnDissilabas = findViewById(R.id.btnDissilabas);
        btnTrissilabas = findViewById(R.id.btnTrissilabas);
        btnPolissilabas = findViewById(R.id.btnPolissilabas);
        btnSobre = findViewById(R.id.btnSobre);
        txtMaxFrutas = findViewById(R.id.txtMaxFrutas);
        txtMaxAnimais = findViewById(R.id.txtMaxAnimais);
        txtMaxObjetos = findViewById(R.id.txtMaxObjetos);

        txtMaxPolissilabas = findViewById(R.id.txtMaxPolissilabas);
        txtMaxTrissilabas = findViewById(R.id.txtMaxTrissilabas);
        txtMaxDissilabas = findViewById(R.id.txtMaxDissilabas);
        txtMaxMonossilabas = findViewById(R.id.txtMaxMonossilabas);

        layoutTipos = findViewById(R.id.layoutTipos);
        layoutClasses = findViewById(R.id.layoutClasses);

        // Carrega as pontuações máximas ao iniciar
        carregarPontuacoesMaximas();

        // 2. Configuração dos Listeners

        // Botão principal: Toggla o menu de opções
        btnJogar.setOnClickListener(v -> {
            boolean isOpcoesVisible = layoutOpcoes.getVisibility() == View.VISIBLE;

            if (!isOpcoesVisible) {
                // Se o layout de opções estiver fechado, abra
                layoutOpcoes.setVisibility(View.VISIBLE);
                btnJogar.setText("❌ FECHAR MENU");
                // Atualiza as pontuações ao abrir o menu
                carregarPontuacoesMaximas();
            } else {
                // Se o layout de opções estiver aberto, feche tudo
                layoutOpcoes.setVisibility(View.GONE);
                layoutTipos.setVisibility(View.GONE);
                layoutClasses.setVisibility(View.GONE);
                btnJogar.setText("▶️ JO-GAR");
            }
        });

        // Novo botão: Jogar por Categoria (Tipo)
        btnJogarPorTipo.setOnClickListener(v -> {
            if (layoutTipos.getVisibility() == View.GONE) {
                // Abre layoutTipos e fecha layoutClasses
                layoutTipos.setVisibility(View.VISIBLE);
                layoutClasses.setVisibility(View.GONE);
            } else {
                // Fecha layoutTipos (toggle)
                layoutTipos.setVisibility(View.GONE);
            }
        });

        // Novo botão: Jogar por Classe Silábica
        btnJogarPorClasse.setOnClickListener(v -> {
            if (layoutClasses.getVisibility() == View.GONE) {
                // Abre layoutClasses e fecha layoutTipos
                layoutClasses.setVisibility(View.VISIBLE);
                layoutTipos.setVisibility(View.GONE);
            } else {
                // Fecha layoutClasses (toggle)
                layoutClasses.setVisibility(View.GONE);
            }
        });


        // 3. Navegação dos botões de sub-menu (permanece a mesma)
        btnFrutas.setOnClickListener(v -> abrirTelaFrutas());
        btnAnimais.setOnClickListener(v -> abrirTelaAnimais());
        btnObjetos.setOnClickListener(v -> abrirTelaObjetos());
        btnMonossilabas.setOnClickListener(v -> abrirTelaMonossilabas());
        btnDissilabas.setOnClickListener(v -> abrirTelaDissilabas());
        btnTrissilabas.setOnClickListener(v -> abrirTelaTrissilabas());
        btnPolissilabas.setOnClickListener(v -> abrirTelaPolissilabas());
        btnSobre.setOnClickListener(v -> abrirSobre());
    }

    private void carregarPontuacoesMaximas() {
        int maxFrutas = dbHelper.getMaxScoreByGameType("fruta");
        int maxAnimais = dbHelper.getMaxScoreByGameType("animal");
        int maxObjetos = dbHelper.getMaxScoreByGameType("objeto");

        int maxPolissilabas = dbHelper.getMaxScoreByGameType("polissilaba");
        int maxTrissilabas = dbHelper.getMaxScoreByGameType("trissilaba");
        int maxDissilabas = dbHelper.getMaxScoreByGameType("dissilaba");
        int maxMonossilabas = dbHelper.getMaxScoreByGameType("monossilaba");

        txtMaxFrutas.setText("Max: " + maxFrutas);
        txtMaxAnimais.setText("Max: " + maxAnimais);
        txtMaxObjetos.setText("Max: " + maxObjetos);

        txtMaxPolissilabas.setText("Max: " + maxPolissilabas);
        txtMaxTrissilabas.setText("Max: " + maxTrissilabas);
        txtMaxDissilabas.setText("Max: " + maxDissilabas);
        txtMaxMonossilabas.setText("Max: " + maxMonossilabas);
    }

    private void abrirTelaFrutas() {
        Intent intent = new Intent(MenuActivity.this, FrutasActivity.class);
        startActivity(intent);
    }

    private void abrirTelaAnimais() {
        Intent intent = new Intent(MenuActivity.this, AnimaisActivity.class);
        startActivity(intent);
    }

    private void abrirTelaObjetos() {
        Intent intent = new Intent(MenuActivity.this, ObjetosActivity.class);
        startActivity(intent);
    }

    private void abrirTelaMonossilabas() {
        Intent intent = new Intent(MenuActivity.this, MonossilabaActivity.class);
        startActivity(intent);
    }

    private void abrirTelaDissilabas() {
        Intent intent = new Intent(MenuActivity.this, DissilabaActivity.class);
        startActivity(intent);
    }

    private void abrirTelaTrissilabas() {
        Intent intent = new Intent(MenuActivity.this, TrissilabaActivity.class);
        startActivity(intent);
    }

    private void abrirTelaPolissilabas() {
        Intent intent = new Intent(MenuActivity.this, PolissilabaActivity.class);
        startActivity(intent);
    }

    private void abrirSobre() {
        String mensagem =   "Silabrain foi um aplicativo criado no contexto acadêmico da disciplina de Projeto - Desafio Computacional " +
                            "ministrada por Rodrigo Rafael Villarreal Goulart. O objetivo do aplicativo era entregar uma experiência simples e divertida de 'brincar enquanto aprende', " +
                            "ao projeto Brincando e Aprendendo, do qual a professora Simone Moreira é encarregada.\n" +
                            "\nDesenvolvedores: Lucas de Oliveira Michaelsen, Eric Arruda Dias, Guilherme Lemmertz e Pedro Augusto de Campos Maurer";


        new AlertDialog.Builder(this)
                .setTitle("Sobre o Silabrain")
                .setMessage(mensagem)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Atualiza as pontuações quando retorna para o menu
        carregarPontuacoesMaximas();
    }
}