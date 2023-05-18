package com.q2k.note;

import android.os.Parcel;
import android.os.Parcelable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Note implements Parcelable {
    public static final DateTimeFormatter DATE_TIME_FORMATTER_WITH_SEC = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private long id;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isSynced;
    public Note(String content, LocalDateTime createdAt, LocalDateTime updatedAt, boolean isSynced) {
        this.id = -1;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isSynced = isSynced;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Note(long id, String content, LocalDateTime createdAt, LocalDateTime updatedAt, boolean isSynced) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isSynced = isSynced;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }

    public String getTitle() {
        String title = content.split("\n")[0];
        if (title.length() > 20) {
            title = title.substring(0, 20) + "...";
        }
        return title;
    }
    protected Note(Parcel in) {
        id = in.readLong();
        content = in.readString();
        createdAt = LocalDateTime.parse(in.readString(), Note.DATE_TIME_FORMATTER_WITH_SEC);
        updatedAt = LocalDateTime.parse(in.readString(), Note.DATE_TIME_FORMATTER_WITH_SEC);
        isSynced = in.readByte() != 0;
    }

    public static final Creator<Note> CREATOR = new Creator<Note>() {
        @Override
        public Note createFromParcel(Parcel in) {
            return new Note(in);
        }

        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(content);
        dest.writeString(createdAt.format(Note.DATE_TIME_FORMATTER_WITH_SEC));
        dest.writeString(updatedAt.format(Note.DATE_TIME_FORMATTER_WITH_SEC));
        dest.writeByte((byte) (isSynced ? 1 : 0));
    }
    // sort notes by createdAt
    public static class SortByCreatedAt implements java.util.Comparator<Note> {
        @Override
        public int compare(Note o1, Note o2) {
            return o2.getCreatedAt().compareTo(o1.getCreatedAt());
        }
    }
    // sort notes by updatedAt
    public static class SortByUpdatedAt implements java.util.Comparator<Note> {
        @Override
        public int compare(Note o1, Note o2) {
            return o2.getUpdatedAt().compareTo(o1.getUpdatedAt());
        }
    }
}
