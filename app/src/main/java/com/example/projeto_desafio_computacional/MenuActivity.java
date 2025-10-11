package com.example.projeto_desafio_computacional;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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
            }
        });

        btnClasses.setOnClickListener(v -> {
            layoutClasses.setVisibility(View.VISIBLE);

            btnFrutas.setVisibility(View.GONE);
            btnAnimais.setVisibility(View.GONE);
            btnObjetos.setVisibility(View.GONE);
            txtMaxAnimais.setVisibility((View.GONE));
            txtMaxObjetos.setVisibility((View.GONE));
            txtMaxFrutas.setVisibility((View.GONE));
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

        txtMaxFrutas.setText("Max: " + maxFrutas);
        txtMaxAnimais.setText("Max: " + maxAnimais);
        txtMaxObjetos.setText("Max: " + maxObjetos);
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