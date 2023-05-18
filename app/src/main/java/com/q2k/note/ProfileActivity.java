package com.q2k.note;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView avatar;
    private TextView name;
    private Button btn;
    SignInClient oneTapClient;
    BeginSignInRequest signUpRequest;
    protected boolean is_logged_in() {
        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        return sharedPreferences.getBoolean("is_logged_in", false);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 60063) {
            try {
                SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
                String idToken = credential.getGoogleIdToken();
                // Send ID token to server and validate
                sendTokenToServer(idToken);
                // save login status
                SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("is_logged_in", true);
                editor.apply();
            } catch (ApiException e) {
                // ...
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Ánh xạ các thành phần từ layout
        avatar = findViewById(R.id.avatar);
        name = findViewById(R.id.name);
        btn = findViewById(R.id.btn);

        // config GoogleSignIn
        oneTapClient = Identity.getSignInClient(this);
        signUpRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(getString(R.string.web_client_id))
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .build();


        if (is_logged_in()) {
            btn.setText("Logout");
            SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
            name.setText(sharedPreferences.getString("name", ""));

            Glide.with(this).load(sharedPreferences.getString("avatar", "")).into(avatar);
        }

        // Cấu hình hành động khi nhấn nút logout
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (is_logged_in()) { // logout
                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                    builder.setTitle("Warning!")
                        .setMessage("Sync your notes before logging out!\nOr you will lose all your notes.")
                        .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean("is_logged_in", false);
                                editor.apply();
                                btn.setText("Login");
                                avatar.setImageResource(R.drawable.ic_avatar);
                                name.setText("Guest");
                                // clear all notes
                                NoteDataSource dataSource;
                                dataSource = new NoteDataSource(ProfileActivity.this);
                                dataSource.open();
                                dataSource.deleteAllNotes();
                                dataSource.close();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Hủy bỏ hộp thoại và không thực hiện đăng xuất
                            }
                        })
                        .show();
                } else { // login
                    oneTapClient.beginSignIn(signUpRequest)
                            .addOnSuccessListener(ProfileActivity.this, new OnSuccessListener<BeginSignInResult>() {
                                @Override
                                public void onSuccess(BeginSignInResult result) {
                                    try {
                                        startIntentSenderForResult(
                                                result.getPendingIntent().getIntentSender(), 60063,
                                                null, 0, 0, 0);
                                    } catch (IntentSender.SendIntentException e) {
                                    }
                                }
                            })
                            .addOnFailureListener(ProfileActivity.this, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                }
                            });

                }

            }
        });
    }

    private void sendTokenToServer(String idToken) {
        String url = "https://quan.q2k.dev/api/google-auth/";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        saveUser(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ProfileActivity.this, "Error: " + error.toString(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("g_token", idToken);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    protected void saveUser(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            String email = jsonObject.getString("email");
            String name = jsonObject.getString("name");
            String avatar = jsonObject.getString("avatar");
            SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("name", name);
            editor.putString("email", email);
            editor.putString("avatar", avatar);
            this.name.setText(name);
            Glide.with(this).load(avatar).into(this.avatar);
            btn.setText("Logout");
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
