package com.q2k.note;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private List<Note> noteList;
    private OnDeleteListener onDeleteListener;
    private NoteDataSource dataSource;
    private Context context;

    public NoteAdapter(Context context) {
        this.context = context;
        dataSource = new NoteDataSource(context);
        dataSource.open();
    }
    public void setNoteList(List<Note> noteList) {
        this.noteList = noteList;
        notifyDataSetChanged();
    }

    public void setOnDeleteListener(OnDeleteListener listener) {
        this.onDeleteListener = listener;
    }

    @Override
    public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_item, parent, false);
        NoteViewHolder viewHolder = new NoteViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final NoteViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        Note note = noteList.get(position);
        holder.noteText.setText(note.getTitle());
        holder.noteCreatedAt.setText(note.getCreatedAt().format(Note.DATE_TIME_FORMATTER));
        holder.noteUpdatedAt.setText(note.getUpdatedAt().format(Note.DATE_TIME_FORMATTER));
        if (note.isSynced()) {
            holder.noteSyncIcon.setImageResource(R.drawable.ic_sync_green);
        } else {
            holder.noteSyncIcon.setImageResource(R.drawable.ic_sync_red);
        }

        // Xử lý sự kiện khi người dùng nhấp vào item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển dữ liệu của note sang màn hình chỉnh sửa
                Intent intent = new Intent(context, EditNoteActivity.class);
                intent.putExtra("note", note);
                ((Activity)context).startActivityForResult(intent, 37);
            }
        });
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public void deleteItem(int position) {
        if (position == RecyclerView.NO_POSITION) {
            return;
        }
        Note deletedNote = noteList.get(position);
        noteList.remove(position);
        notifyItemRemoved(position);

        if (onDeleteListener != null) {
            onDeleteListener.onItemDeleted(deletedNote);
            dataSource.deleteNote(deletedNote);
        }
    }


    public Context getContext() {
        return context;
    }

    public Note getNoteAt(int position) {
        return noteList.get(position);
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView noteText;
        TextView noteCreatedAt;
        TextView noteUpdatedAt;
        ImageView noteSyncIcon;

        public NoteViewHolder(View itemView) {
            super(itemView);
            noteText = itemView.findViewById(R.id.note_title);
            noteCreatedAt = itemView.findViewById(R.id.note_created_date);
            noteUpdatedAt = itemView.findViewById(R.id.note_updated_date);
            noteSyncIcon = itemView.findViewById(R.id.note_sync_icon);
        }
    }


    public interface OnDeleteListener {
        void onItemDeleted(Note deletedNote);
    }
}
