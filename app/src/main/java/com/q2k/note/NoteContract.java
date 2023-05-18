package com.q2k.note;

import android.provider.BaseColumns;

public final class NoteContract {
    private NoteContract() {} // Private constructor to prevent instantiation

    public static class NoteEntry implements BaseColumns {
        public static final String TABLE_NAME = "notes";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String COLUMN_UPDATED_AT = "updated_at";
        public static final String COLUMN_IS_SYNCED = "is_synced";
    }
}
