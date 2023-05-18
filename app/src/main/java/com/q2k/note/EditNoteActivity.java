package com.q2k.note;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class EditNoteActivity extends AppCompatActivity {
    private NoteDataSource dataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_note_activity);
        dataSource = new NoteDataSource(this);
        dataSource.open();
        // Lấy dữ liệu note từ Intent
        Note note = getIntent().getParcelableExtra("note");

        // Hiển thị dữ liệu note trong giao diện
        EditText editNoteContent = findViewById(R.id.edit_note_content);
        editNoteContent.setText(note.getContent());

        // Xử lý sự kiện khi người dùng nhấn nút Save/Cancel
        Button btnSave = findViewById(R.id.btn_save);
        Button btnCancel = findViewById(R.id.btn_cancel);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lấy dữ liệu từ giao diện
                String content = editNoteContent.getText().toString().trim();
                if (note.getId() == -1) { // tạo mới
                    if (!content.isBlank()) {
                        dataSource.addNote(
                                new Note(
                                        content,
                                        LocalDateTime.now(),
                                        LocalDateTime.now(),
                                        false
                                )
                        );
                    }
                } else { // cập nhật
                    if (content.isBlank()) {
                        dataSource.deleteNote(note);
                    } else {
                        note.setContent(content);
                        note.setUpdatedAt(LocalDateTime.now());
                        note.setSynced(false);
                        dataSource.updateNote(note);
                    }
                }
                Intent returnIntent = new Intent();
                setResult(37, returnIntent);
                finish();
            }
        });
        btnCancel.setOnClickListener(v -> {
            Intent returnIntent = new Intent();
            setResult(-1, returnIntent);
            finish();
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Đóng kết nối cơ sở dữ liệu khi activity bị hủy
        dataSource.close();
    }
}

