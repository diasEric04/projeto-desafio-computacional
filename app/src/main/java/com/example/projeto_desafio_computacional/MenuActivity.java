package com.example.projeto_desafio_computacional;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class MenuActivity extends AppCompatActivity {

    private LinearLayout layoutTipos, layoutClasses;
    private Button btnFrutas, btnAnimais, btnObjetos, btnClasses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        DatabaseHelper dbHelper = new DatabaseHelper(this);
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

        layoutTipos = findViewById(R.id.layoutTipos);
        layoutClasses = findViewById(R.id.layoutClasses);

        btnJogar.setOnClickListener(v -> {
            if (layoutTipos.getVisibility() == View.GONE) {
                layoutTipos.setVisibility(View.VISIBLE);
            } else {
                layoutTipos.setVisibility(View.GONE);
                layoutClasses.setVisibility(View.GONE);

                btnFrutas.setVisibility(View.VISIBLE);
                btnAnimais.setVisibility(View.VISIBLE);
                btnObjetos.setVisibility(View.VISIBLE);
            }
        });

        btnClasses.setOnClickListener(v -> {
            layoutClasses.setVisibility(View.VISIBLE);

            btnFrutas.setVisibility(View.GONE);
            btnAnimais.setVisibility(View.GONE);
            btnObjetos.setVisibility(View.GONE);
        });

        // Navegação dos botões
        btnFrutas.setOnClickListener(v -> abrirTelaFrutas());
        btnAnimais.setOnClickListener(v -> abrirTelaAnimais());
        btnObjetos.setOnClickListener(v -> abrirTelaObjetos());

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

}