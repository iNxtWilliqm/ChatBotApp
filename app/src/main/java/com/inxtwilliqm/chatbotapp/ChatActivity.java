package com.inxtwilliqm.chatbotapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private EditText promptText;
    private Button sendButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);
        chatRecyclerView.setAdapter(chatAdapter);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        promptText = findViewById(R.id.promptEditText);
        sendButton = findViewById(R.id.sendButton);
        progressBar = findViewById(R.id.progressBar);

        String username = getIntent().getStringExtra("username");

        chatMessages.add(new ChatMessage("Welcome " + username + "!", false));
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String userMessage = promptText.getText().toString().trim();
        if (userMessage.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show ProgressBar and clear previous response
        progressBar.setVisibility(View.VISIBLE);
        promptText.setText("");
        chatMessages.add(new ChatMessage(userMessage, true));
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        chatRecyclerView.scrollToPosition(chatMessages.size() - 1);

        String url = "http://10.0.2.2:5000/chat"; // Update to machine IP for physical device
        StringRequest request = new StringRequest(Request.Method.POST, url,
            response -> {
                // Hide ProgressBar and show response
                progressBar.setVisibility(View.GONE);
                String botMessage = response.trim();
                chatMessages.add(new ChatMessage(botMessage, false));
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
            },
            error -> {
                // Hide ProgressBar and show error
                progressBar.setVisibility(View.GONE);
                String errorMessage = "Error connecting to server";
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userMessage", userMessage);
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
            30000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        Volley.newRequestQueue(this).add(request);
    }
}
