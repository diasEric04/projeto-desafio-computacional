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
import java.util.Locale;

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
        // Criar tabela de pontuações se não existir
        String createScoreTable = "CREATE TABLE IF NOT EXISTS scores (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "game_type TEXT NOT NULL, " +
                "total_score INTEGER DEFAULT 0, " +
                "max_score INTEGER DEFAULT 0, " +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)";
        db.execSQL(createScoreTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Implementar upgrade se necessário
    }

    public void createAndOpenDatabase() throws IOException {
        this.getReadableDatabase().close();
        copyDatabase();
    }

    public void copyDatabase() throws IOException {
        String outFileName = context.getDatabasePath(DB_NAME).getPath();

        java.io.File dbFile = new java.io.File(outFileName);
        if (dbFile.exists()) return;

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
            String query =
                    "SELECT p.*, c.categoria FROM palavras p " +
                            "JOIN categoria c ON p.id_categoria = c.id " +
                            "WHERE c.categoria = ? " +
                            "ORDER BY RANDOM() LIMIT 1";

            String[] selectionArgs = { category };

            cursor = db.rawQuery(query, selectionArgs);

            if (cursor != null && cursor.moveToFirst()) {
                int columnCount = cursor.getColumnCount();
                resultArray = new String[columnCount];

                for (int i = 0; i < columnCount; i++) {
                    resultArray[i] = cursor.getString(i);
                }
            }
        } catch (Exception e) {
            Log.e("DB_ERROR", "Erro ao executar query de random com JOIN: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return resultArray;
    }

    public String getClass(String palavra) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        String classificacao = "N/A"; // Valor padrão se a palavra não for encontrada

        try {
            // Abre o banco de dados COPIADO
            db = this.openDatabase();

            // Sua consulta SQL. Note que ela SÓ retorna a coluna 'classe'
            String query =
                    "SELECT c.classe FROM classe c " +
                            "INNER JOIN palavras p ON c.id = p.id_classe " +
                            "WHERE p.palavra = ?";

            String[] selectionArgs = { palavra.toLowerCase(Locale.getDefault()) };

            cursor = db.rawQuery(query, selectionArgs);

            // Verifica se encontrou um resultado
            if (cursor != null && cursor.moveToFirst()) {

                // CORREÇÃO: Busca a coluna pelo nome 'classe' (o nome que o SELECT retorna)
                // Se o SELECT retorna apenas uma coluna, ela tem índice 0.
                // O getColumnIndexOrThrow é mais seguro do que contar.
                int classeIndex = cursor.getColumnIndexOrThrow("classe");

                classificacao = cursor.getString(classeIndex);
            }
        } catch (IllegalArgumentException e) {
            // Erro se a coluna 'classe' não for encontrada (checar ortografia do SELECT)
            Log.e("DB_ERROR", "Coluna 'classe' não encontrada. Erro: " + e.getMessage());
        } catch (Exception e) {
            Log.e("DB_ERROR", "Erro ao buscar classificação da palavra: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return classificacao;
    }

    // NOVOS MÉTODOS PARA PONTUAÇÃO
    public void saveGameScore(String gameType, int totalScore, int maxScore) {
        SQLiteDatabase db = this.openDatabase();
        try {
            String query = "INSERT INTO scores (game_type, total_score, max_score) VALUES (?, ?, ?)";
            db.execSQL(query, new Object[]{gameType, totalScore, maxScore});
            Log.i("DB_SCORE", "Pontuação salva: " + gameType + " - " + totalScore + " pts");
        } catch (Exception e) {
            Log.e("DB_ERROR", "Erro ao salvar pontuação: " + e.getMessage());
        }
    }

    public int getMaxScoreByGameType(String gameType) {
        SQLiteDatabase db = this.openDatabase();
        Cursor cursor = null;
        int maxScore = 0;

        try {
            String query = "SELECT MAX(total_score) FROM scores WHERE game_type = ?";
            cursor = db.rawQuery(query, new String[]{gameType});

            if (cursor != null && cursor.moveToFirst()) {
                maxScore = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e("DB_ERROR", "Erro ao buscar pontuação máxima: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return maxScore;
    }
}