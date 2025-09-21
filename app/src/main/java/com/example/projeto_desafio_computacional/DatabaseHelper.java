package com.example.projeto_desafio_computacional;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class DatabaseHelper {

    private static String DB_NAME = "";
    private Context context;

    public DatabaseHelper(Context context) {
        this.context = context;
    }

    public void copyDatabase() throws IOException {
        String outFileName = context.getDatabasePath(DB_NAME).getPath();

        // ✅ Verifica se o banco já existe
        if (!context.getDatabasePath(DB_NAME).exists()) {
            InputStream myInput = context.getAssets().open(DB_NAME);
            OutputStream myOutput = new FileOutputStream(outFileName);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }

            myOutput.flush();
            myOutput.close();
            myInput.close();
        }
        // Se o banco já existir, nada é feito
    }

    public SQLiteDatabase openDatabase() {
        return SQLiteDatabase.openDatabase(
                context.getDatabasePath(DB_NAME).getPath(),
                null,
                SQLiteDatabase.OPEN_READWRITE
        );
    }
}
