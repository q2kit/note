package com.q2k.note;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NoteDataSource {
    private SQLiteDatabase database;
    private NoteDbHelper dbHelper;

    public NoteDataSource(Context context) {
        dbHelper = new NoteDbHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }
    private long getMaxID() {
        String[] columns = {"MAX(" + NoteContract.NoteEntry.COLUMN_ID + ")"};
        Cursor cursor = database.query(
                NoteContract.NoteEntry.TABLE_NAME,
                columns,
                null,
                null,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") long maxID = cursor.getLong(0);
            cursor.close();
            return maxID;
        }
        return 0;
    }

    public void addNote(Note note) {
        if (note.getId() == -1) {
            note.setId(getMaxID() + 1);
        }
        ContentValues values = new ContentValues();
        values.put(NoteContract.NoteEntry.COLUMN_ID, note.getId());
        values.put(NoteContract.NoteEntry.COLUMN_CONTENT, note.getContent());
        values.put(NoteContract.NoteEntry.COLUMN_CREATED_AT, note.getCreatedAt().format(Note.DATE_TIME_FORMATTER_WITH_SEC));
        values.put(NoteContract.NoteEntry.COLUMN_UPDATED_AT, note.getUpdatedAt().format(Note.DATE_TIME_FORMATTER_WITH_SEC));
        values.put(NoteContract.NoteEntry.COLUMN_IS_SYNCED, note.isSynced());
        database.insert(NoteContract.NoteEntry.TABLE_NAME, null, values);
    }

    public void updateNote(Note note) {
        ContentValues values = new ContentValues();
        values.put(NoteContract.NoteEntry.COLUMN_CONTENT, note.getContent());
        values.put(NoteContract.NoteEntry.COLUMN_UPDATED_AT, note.getUpdatedAt().format(Note.DATE_TIME_FORMATTER_WITH_SEC));
        values.put(NoteContract.NoteEntry.COLUMN_IS_SYNCED, note.isSynced());

        String whereClause = NoteContract.NoteEntry.COLUMN_ID + " = ?";
        String[] whereArgs = {String.valueOf(note.getId())};

        database.update(NoteContract.NoteEntry.TABLE_NAME, values, whereClause, whereArgs);
    }
    public void deleteNote(Note note) {
        String whereClause = NoteContract.NoteEntry.COLUMN_ID + " = ?";
        String[] whereArgs = {String.valueOf(note.getId())};

        database.delete(NoteContract.NoteEntry.TABLE_NAME, whereClause, whereArgs);
    }

    public List<Note> getAllNotes() {
        List<Note> notes = new ArrayList<>();

        Cursor cursor = database.query(
                NoteContract.NoteEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") long id = cursor.getLong(cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_ID));
                @SuppressLint("Range") String content = cursor.getString(cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_CONTENT));
                @SuppressLint("Range") String createdAt = cursor.getString(cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_CREATED_AT));
                @SuppressLint("Range") String updatedAt = cursor.getString(cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_UPDATED_AT));
                @SuppressLint("Range") boolean isSynced = cursor.getInt(cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_IS_SYNCED)) == 1;
                Note note = new Note(
                        id,
                        content,
                        LocalDateTime.parse(createdAt, Note.DATE_TIME_FORMATTER_WITH_SEC),
                        LocalDateTime.parse(updatedAt, Note.DATE_TIME_FORMATTER_WITH_SEC),
                        isSynced
                );
                notes.add(note);
            } while (cursor.moveToNext());

            cursor.close();
        }
        Collections.sort(notes, new Note.SortByCreatedAt());
        return notes;
    }

    public List<Note> searchNotes(String text) {
        List<Note> notes = new ArrayList<>();

        String selection = NoteContract.NoteEntry.COLUMN_CONTENT + " LIKE ?";
        String[] selectionArgs = {"%" + text + "%"};

        Cursor cursor = database.query(
                NoteContract.NoteEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") long id = cursor.getLong(cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_ID));
                @SuppressLint("Range") String content = cursor.getString(cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_CONTENT));
                @SuppressLint("Range") String createdAt = cursor.getString(cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_CREATED_AT));
                @SuppressLint("Range") String updatedAt = cursor.getString(cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_UPDATED_AT));
                @SuppressLint("Range") boolean isSynced = cursor.getInt(cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_IS_SYNCED)) == 1;
                Note note = new Note(
                        id,
                        content,
                        LocalDateTime.parse(createdAt, Note.DATE_TIME_FORMATTER_WITH_SEC),
                        LocalDateTime.parse(updatedAt, Note.DATE_TIME_FORMATTER_WITH_SEC),
                        isSynced
                );
                notes.add(note);
            } while (cursor.moveToNext());

            cursor.close();
        }
        // sort notes by createdAt
        Collections.sort(notes, new Note.SortByCreatedAt());
        return notes;
    }

    public void deleteAllNotes() {
        database.delete(NoteContract.NoteEntry.TABLE_NAME, null, null);
    }

    public void syncDone() {
        ContentValues values = new ContentValues();
        values.put(NoteContract.NoteEntry.COLUMN_IS_SYNCED, true);

        database.update(NoteContract.NoteEntry.TABLE_NAME, values, null, null);
    }

    public void updateOrCreateNote(Note note) {
        // check if note exists
        String selection = NoteContract.NoteEntry.COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(note.getId())};

        Cursor cursor = database.query(
                NoteContract.NoteEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            // note exists, update it
            updateNote(note);
        } else {
            // note doesn't exist, create it
            addNote(note);
        }
    }
}
