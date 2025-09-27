package com.example.projeto_desafio_computacional;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static String DB_NAME = "palavras.db";
    private static final int DB_VERSION = 1;
    private Context context;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void createAndOpenDatabase() throws IOException {
        // 1. Força o SQLiteOpenHelper a criar o banco de dados vazio no destino
        //    (Seu onCreate() será chamado APENAS se o arquivo não existir.
        //    Mas como você está usando um arquivo .db, seu onCreate deve estar vazio.)
        this.getReadableDatabase().close();

        // 2. Chama o método de cópia
        copyDatabase();
    }

    public void copyDatabase() throws IOException {
        String outFileName = context.getDatabasePath(DB_NAME).getPath();

        // Se o banco já existir, não precisa copiar
        java.io.File dbFile = new java.io.File(outFileName);
        if (dbFile.exists()) return;

        // Cria diretório se não existir
        dbFile.getParentFile().mkdirs();

        InputStream input = context.getAssets().open(DB_NAME);
        OutputStream output = new FileOutputStream(outFileName);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = input.read(buffer)) > 0) {
            output.write(buffer, 0, length);
        }

        output.flush();
        output.close();
        input.close();
    }

    public SQLiteDatabase openDatabase() {
        return SQLiteDatabase.openDatabase(
                context.getDatabasePath(DB_NAME).getPath(),
                null,
                SQLiteDatabase.OPEN_READWRITE
        );
    }

    public String[] getRandomDataByCategory(String category) {
        SQLiteDatabase db = this.openDatabase();
        Cursor cursor = null;
        String[] resultArray = null;

        try {
            // Consulta SQL: Usa JOIN nas tabelas 'palavras' e 'categoria',
            // filtra pela categoria passada e seleciona 1 registro aleatório.
            String query =
                    "SELECT p.*, c.categoria FROM palavras p " + // Seleciona todas as colunas de 'palavras' e a coluna 'categoria' de 'c'
                            "JOIN categoria c ON p.id_categoria = c.id " +
                            "WHERE c.categoria = ? " +
                            "ORDER BY RANDOM() LIMIT 1";

            String[] selectionArgs = { category };

            // Executa a consulta
            cursor = db.rawQuery(query, selectionArgs);

            // Verifica se encontrou um resultado
            if (cursor != null && cursor.moveToFirst()) {
                int columnCount = cursor.getColumnCount();
                resultArray = new String[columnCount];

                // Itera por todas as colunas da linha e extrai os valores como String
                for (int i = 0; i < columnCount; i++) {
                    resultArray[i] = cursor.getString(i);
                }
            }
        } catch (Exception e) {
            Log.e("DB_ERROR", "Erro ao executar query de random com JOIN: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // SEMPRE feche o cursor
            if (cursor != null) {
                cursor.close();
            }
        }

        return resultArray;
    }

}
