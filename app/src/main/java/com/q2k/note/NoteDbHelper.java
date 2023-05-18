package com.q2k.note;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NoteDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "notes.db";
    private static final int DATABASE_VERSION = 1;

    public NoteDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + NoteContract.NoteEntry.TABLE_NAME + " (" +
                NoteContract.NoteEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NoteContract.NoteEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +
                NoteContract.NoteEntry.COLUMN_CREATED_AT + " TEXT NOT NULL, " +
                NoteContract.NoteEntry.COLUMN_UPDATED_AT + " TEXT NOT NULL, " +
                NoteContract.NoteEntry.COLUMN_IS_SYNCED + " INTEGER NOT NULL DEFAULT 0);";


        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Xóa bảng cũ nếu tồn tại và tạo lại
        db.execSQL("DROP TABLE IF EXISTS " + NoteContract.NoteEntry.TABLE_NAME);
        onCreate(db);
    }
}
