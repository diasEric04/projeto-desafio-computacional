package com.example.projeto_desafio_computacional;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class MenuActivity extends AppCompatActivity {

    private LinearLayout layoutTipos, layoutClasses;
    private Button btnFrutas, btnAnimais, btnObjetos, btnClasses, btnDissilabas;
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

        Button btnJogar = findViewById(R.id.btnJogar);
        btnFrutas = findViewById(R.id.btnFrutas);
        btnAnimais = findViewById(R.id.btnAnimais);
        btnObjetos = findViewById(R.id.btnObjetos);
        btnClasses = findViewById(R.id.btnClasses);
        btnDissilabas = findViewById(R.id.btnDissilabas);

        txtMaxFrutas = findViewById(R.id.txtMaxFrutas);
        txtMaxAnimais = findViewById(R.id.txtMaxAnimais);
        txtMaxObjetos = findViewById(R.id.txtMaxObjetos);

        txtMaxPolissilabas = findViewById(R.id.txtMaxPolissilabas);
        txtMaxTrissilabas = findViewById(R.id.txtMaxTrissilabas);
        txtMaxDissilabas = findViewById(R.id.txtMaxDissilabas);
        txtMaxMonossilabas = findViewById(R.id.txtMaxMonossilabas);

        layoutTipos = findViewById(R.id.layoutTipos);
        layoutClasses = findViewById(R.id.layoutClasses);

        // Carrega as pontuações máximas
        carregarPontuacoesMaximas();

        btnJogar.setOnClickListener(v -> {
            if (layoutTipos.getVisibility() == View.GONE) {
                layoutTipos.setVisibility(View.VISIBLE);
                // Atualiza as pontuações quando abre o menu
                carregarPontuacoesMaximas();
            } else {
                layoutTipos.setVisibility(View.GONE);
                layoutClasses.setVisibility(View.GONE);

                btnFrutas.setVisibility(View.VISIBLE);
                btnAnimais.setVisibility(View.VISIBLE);
                btnObjetos.setVisibility(View.VISIBLE);
                txtMaxAnimais.setVisibility((View.VISIBLE));
                txtMaxObjetos.setVisibility((View.VISIBLE));
                txtMaxFrutas.setVisibility((View.VISIBLE));
                txtMaxPolissilabas.setVisibility((View.GONE));
                txtMaxTrissilabas.setVisibility((View.GONE));
                txtMaxDissilabas.setVisibility((View.GONE));
                txtMaxMonossilabas.setVisibility((View.GONE));

            }
        });

        btnClasses.setOnClickListener(v -> {

            int toggleConcordWClasses = layoutClasses.getVisibility() == View.GONE
                    ? View.VISIBLE : View.GONE;

            int toggleDiscordWClasses = layoutClasses.getVisibility() == View.GONE
                    ? View.GONE : View.VISIBLE;

            layoutClasses.setVisibility(toggleConcordWClasses);
            btnFrutas.setVisibility(toggleDiscordWClasses);
            btnAnimais.setVisibility(toggleDiscordWClasses);
            btnObjetos.setVisibility(toggleDiscordWClasses);
            txtMaxAnimais.setVisibility(toggleDiscordWClasses);
            txtMaxObjetos.setVisibility(toggleDiscordWClasses);
            txtMaxFrutas.setVisibility(toggleDiscordWClasses);
            txtMaxPolissilabas.setVisibility(toggleConcordWClasses);
            txtMaxTrissilabas.setVisibility(toggleConcordWClasses);
            txtMaxDissilabas.setVisibility(toggleConcordWClasses);
            txtMaxMonossilabas.setVisibility(toggleConcordWClasses);



        });

        // Navegação dos botões
        btnFrutas.setOnClickListener(v -> abrirTelaFrutas());
        btnAnimais.setOnClickListener(v -> abrirTelaAnimais());
        btnObjetos.setOnClickListener(v -> abrirTelaObjetos());
        btnDissilabas.setOnClickListener(v -> abrirTelaDissilabas());

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

    private void abrirTelaDissilabas() {
        Intent intent = new Intent(MenuActivity.this, DissilabaActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Atualiza as pontuações quando retorna para o menu
        carregarPontuacoesMaximas();
    }
}