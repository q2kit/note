package com.q2k.note;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NoteAdapter.OnDeleteListener {
    private List<Note> noteList;
    private NoteAdapter noteAdapter;
    private RecyclerView recyclerView;
    private FloatingActionButton fab_add, fab_sync, fab_account;
    private NoteDataSource dataSource;
    ImageButton btn_sort, btn_search, close_search;
    boolean sort_by_created_at = true;
    SearchView searchView;
    TextView name;
    RelativeLayout header_layout;
    CircleImageView avatar;
    Toast toast_sort_by_created_at, toast_sort_by_updated_at;
    SignInClient oneTapClient;
    BeginSignInRequest signUpRequest;
    private int REQ_PROFILE = 98765;

    protected boolean is_internet_available(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            Network network = connectivityManager.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                return networkCapabilities != null && (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // check language
        SharedPreferences sharedPreferences = getSharedPreferences("language", MODE_PRIVATE);
        String language = sharedPreferences.getString("language", "");
        if (language.equals("")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("language", "en");
            editor.apply();
        }

        // Khởi tạo NoteDataSource và mở kết nối với cơ sở dữ liệu
        dataSource = new NoteDataSource(this);
        dataSource.open();

        // Khởi tạo toast
        toast_sort_by_created_at = Toast.makeText(this, getString(this, "sort_by_created_at"), Toast.LENGTH_SHORT);
        toast_sort_by_updated_at = Toast.makeText(this, getString(this, "sort_by_updated_at"), Toast.LENGTH_SHORT);

        noteList = new ArrayList<>();
        // Lấy dữ liệu note từ cơ sở dữ liệu
        noteList = dataSource.getAllNotes();
        // Create the NoteAdapter
        noteAdapter = new NoteAdapter(this);
        noteAdapter.setOnDeleteListener(this);
        header_layout = findViewById(R.id.header_layout);

        // Set up the RecyclerView
        recyclerView = findViewById(R.id.notes_recycler_view);
        fab_add = findViewById(R.id.fab_add);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(noteAdapter);
        btn_search = findViewById(R.id.btn_search);
        btn_sort = findViewById(R.id.btn_sort);
        searchView = findViewById(R.id.search_view);
        close_search = findViewById(R.id.btn_close_search);
        fab_account = findViewById(R.id.fab_account);
        fab_sync = findViewById(R.id.fab_sync);
        name = findViewById(R.id.name);
        name.setText(getString(this, "guest"));
        avatar = findViewById(R.id.avatar);
        if (is_logged_in()) {
            // Lấy thông tin người dùng từ SharedPreferences
            sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
            String name = sharedPreferences.getString("name", "");
            String avatar_url = sharedPreferences.getString("avatar", "");
            // Hiển thị thông tin người dùng
            this.name.setText(name);
            Glide.with(this).load(avatar_url).into(avatar);
        }
        btn_search.setOnClickListener(v -> {
            // ẩn name, avatar, button sort, button search
            name.setVisibility(TextView.GONE);
            avatar.setVisibility(ImageView.GONE);
            btn_sort.setVisibility(ImageButton.GONE);
            btn_search.setVisibility(ImageButton.GONE);
            // hiện search view, button close search
            searchView.setVisibility(SearchView.VISIBLE);
            close_search.setVisibility(ImageButton.VISIBLE);
        });
        close_search.setOnClickListener(view -> {
            // ẩn search view, button close search
            searchView.setVisibility(SearchView.GONE);
            close_search.setVisibility(ImageButton.GONE);
            // hiện name, avatar, button sort, button search
            name.setVisibility(TextView.VISIBLE);
            avatar.setVisibility(ImageView.VISIBLE);
            btn_sort.setVisibility(ImageButton.VISIBLE);
            btn_search.setVisibility(ImageButton.VISIBLE);
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String queryText) {
                noteList = dataSource.searchNotes(queryText);
                noteAdapter.setNoteList(noteList);
                noteAdapter.notifyDataSetChanged();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                noteList = dataSource.searchNotes(newText);
                noteAdapter.setNoteList(noteList);
                return false;
            }
        });

        btn_sort.setOnClickListener(v -> {
            noteList = dataSource.getAllNotes();
            if (noteList.isEmpty()) {
                Toast.makeText(this, getString(this, "no_note"), Toast.LENGTH_SHORT).show();
                return;
            }
            if (sort_by_created_at) {
                Collections.sort(noteList, new Note.SortByUpdatedAt());
                sort_by_created_at = false;
                toast_sort_by_created_at.cancel();
                toast_sort_by_updated_at.show();
            } else {
                Collections.sort(noteList, new Note.SortByCreatedAt());
                sort_by_created_at = true;
                toast_sort_by_updated_at.cancel();
                toast_sort_by_created_at.show();
            }
            noteAdapter.setNoteList(noteList);
        });

        // Set up item touch helper for swipe-to-delete functionality
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(noteAdapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // Set the noteList to the adapter
        noteAdapter.setNoteList(noteList);
        fab_add.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditNoteActivity.class);
            intent.putExtra("note", new Note("", LocalDateTime.now(), LocalDateTime.now(), false));
            startActivityForResult(intent, 37);
        });
        fab_add.setOnLongClickListener(v -> {
            if (!is_internet_available(this)) {
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
                return false;
            } else {
                // show fab_sync, fab_account
                if (is_logged_in()) {
                    fab_sync.setVisibility(FloatingActionButton.VISIBLE);
                }
                fab_account.setVisibility(FloatingActionButton.VISIBLE);
                // auto hide after 5 seconds
                new Handler().postDelayed(() -> {
                    fab_sync.setVisibility(FloatingActionButton.GONE);
                    fab_account.setVisibility(FloatingActionButton.GONE);
                }, 5000);
                return true;
            }
        });
        View.OnTouchListener onTouchListener = (v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (v != fab_sync && v != fab_account) {
                    // hide fab_sync, fab_account
                    fab_sync.setVisibility(FloatingActionButton.GONE);
                    fab_account.setVisibility(FloatingActionButton.GONE);
                }
            }
            return false;
        };
        // config GoogleSignIn
        oneTapClient = Identity.getSignInClient(this);
        signUpRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(getString(R.string.web_client_id))
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .build();

        // set onTouchListener for all views to hide fab_sync, fab_account
        header_layout.setOnTouchListener(onTouchListener);
        recyclerView.setOnTouchListener(onTouchListener);
        btn_search.setOnTouchListener(onTouchListener);
        btn_sort.setOnTouchListener(onTouchListener);
        searchView.setOnTouchListener(onTouchListener);
        close_search.setOnTouchListener(onTouchListener);
        name.setOnTouchListener(onTouchListener);
        avatar.setOnTouchListener(onTouchListener);
        fab_sync.setOnClickListener(v -> {
            try {
                syncNotes();
            } catch (JSONException e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        fab_account.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivityForResult(intent, REQ_PROFILE);
        });
    }
    protected boolean is_logged_in() {
        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        return sharedPreferences.getBoolean("is_logged_in", false);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 37) {
            if (resultCode == 37) {
                updateNoteList();
            }
        }
        if (requestCode == REQ_PROFILE) { // back from ProfileActivity
            updateNoteList();
            if (is_logged_in()) {
                String name = getSharedPreferences("user", MODE_PRIVATE).getString("name", "Guest");
                String avatar = getSharedPreferences("user", MODE_PRIVATE).getString("avatar", "");
                this.name.setText(name);
                Glide.with(this).load(avatar).into(this.avatar);
            } else {
                this.avatar.setImageResource(R.drawable.ic_avatar);
                this.name.setText("Guest");
                fab_sync.setVisibility(FloatingActionButton.GONE);
            }
        }
    }

    @Override
    public void onItemDeleted(Note deletedNote) {
        // Handle the deleted item, such as showing the undo option
        // You can display a Snackbar or any other UI element here
        String note_deleted = getString(this, "note_deleted");
        Toast.makeText(this, note_deleted + ": " + deletedNote.getTitle(), Toast.LENGTH_LONG).show();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Đóng kết nối cơ sở dữ liệu khi activity bị hủy
        dataSource.close();
    }
    public void updateNoteList() {
        // Lấy danh sách ghi chú từ NoteDataSource
        List<Note> noteList = dataSource.getAllNotes();

        // Cập nhật danh sách ghi chú trong Adapter
        noteAdapter.setNoteList(noteList);

        // Thông báo cho Adapter rằng dữ liệu đã thay đổi
        noteAdapter.notifyDataSetChanged();
    }

    protected void syncNotes() throws JSONException {
        NoteDataSource dataSource;
        dataSource = new NoteDataSource(this);
        dataSource.open();
        List<Note> notes = dataSource.getAllNotes();
        JSONArray jsonArray = new JSONArray();
        String email = getSharedPreferences("user", MODE_PRIVATE).getString("email", "");
        for (Note note : notes) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("id", note.getId());
                jsonObject.put("content", note.getContent());
                jsonObject.put("is_synced", note.isSynced());
                jsonObject.put("created_at", note.getCreatedAt().format(Note.DATE_TIME_FORMATTER_WITH_SEC));
                jsonObject.put("updated_at", note.getUpdatedAt().format(Note.DATE_TIME_FORMATTER_WITH_SEC));
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        String url = "https://quan.q2k.dev/api/sync/";
        JSONObject requestBody = new JSONObject();
        requestBody.put("email", email);
        requestBody.put("notes", jsonArray);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestBody,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if (!response.getBoolean("success")) {
                            Toast.makeText(MainActivity.this, response.getString("message"), Toast.LENGTH_LONG).show();
                        } else {
                            JSONArray notes = response.getJSONArray("notes");
                            int push = response.getInt("count_push");
                            int pull = response.getInt("count_pull");
                            for (int i = 0; i < notes.length(); i++) {
                                JSONObject note = notes.getJSONObject(i);
                                long id = note.getLong("id");
                                String content = note.getString("content");
                                String created_at = note.getString("created_at");
                                String updated_at = note.getString("updated_at");
                                dataSource.updateOrCreateNote(
                                    new Note(
                                        id,
                                        content,
                                        LocalDateTime.parse(created_at, Note.DATE_TIME_FORMATTER_WITH_SEC),
                                        LocalDateTime.parse(updated_at, Note.DATE_TIME_FORMATTER_WITH_SEC),
                                        true
                                    )
                                );
                            }
                            dataSource.syncDone();
                            dataSource.close();
                            updateNoteList();
                            String push_str = MainActivity.getString(MainActivity.this, "push");
                            String pull_str = MainActivity.getString(MainActivity.this, "pull");
                            Toast.makeText(MainActivity.this, push_str + ": " + push + "\n" + pull_str + ": " + pull, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // Xử lý lỗi (nếu có)
                }
            });
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
    public static String getString(Context context, String code) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("language", MODE_PRIVATE);
        String language = sharedPreferences.getString("language", "en");
        code += "_" + language;
        return context.getString(context.getResources().getIdentifier(code, "string", context.getPackageName()));
    }
}
