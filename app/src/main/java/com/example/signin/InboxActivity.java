package com.example.signin;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class InboxActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private InboxAdapter adapter;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        recyclerView = findViewById(R.id.rvInbox);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InboxAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        loadInbox();
    }

    private void loadInbox() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        firestore.collection("patients").document(uid).collection("inbox")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(InboxActivity.this, "Failed to load inbox", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        adapter.clear();
                        if (snapshots == null) return;
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            InboxItem item = dc.getDocument().toObject(InboxItem.class);
                            if (item != null && item.getFileUrl() != null && !item.getFileUrl().isEmpty()
                                    && item.getFileName() != null && !item.getFileName().isEmpty()) {
                                item.setId(dc.getDocument().getId());
                                adapter.addInboxItem(item);
                            }
                        }
                    }
                });
    }
}
