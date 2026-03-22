package com.example.signin;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Central helper for all /arrivalIndex/{uid} operations.
 *
 * Android must only touch arrivalIndex for arrivals and MUST NOT read /counters or awaitingArrival.
 */
public class ArrivalIndexRepository {

    private final DatabaseReference rootRef;
    private final FirebaseAuth auth;

    public ArrivalIndexRepository() {
        this(FirebaseDatabase.getInstance().getReference(), FirebaseAuth.getInstance());
    }

    public ArrivalIndexRepository(@NonNull DatabaseReference rootRef,
                                  @NonNull FirebaseAuth auth) {
        this.rootRef = rootRef;
        this.auth = auth;
    }

    private String requireUid() {
        if (auth.getCurrentUser() == null) {
            throw new IllegalStateException("User must be logged in to use ArrivalIndexRepository");
        }
        return auth.getCurrentUser().getUid();
    }

    /**
     * Creates or overwrites the single active arrivalIndex entry for the current user.
     * This enforces the rule: at most ONE active arrival per patient.
     */
    public void createOrOverwriteArrivalIndex(@NonNull String establishmentId,
                                              @NonNull String counterId,
                                              @NonNull String awaitingKey,
                                              @NonNull String requestKey) {
        String uid = requireUid();

        DatabaseReference arrivalRef = rootRef.child("arrivalIndex").child(uid);

        Map<String, Object> payload = new HashMap<>();
        payload.put("uid", uid);
        payload.put("counterId", counterId);
        payload.put("awaitingKey", awaitingKey);
        payload.put("requestKey", requestKey);
        payload.put("establishmentId", establishmentId);
        payload.put("status", "awaiting_arrival");
        payload.put("createdAt", ServerValue.TIMESTAMP);

        arrivalRef.setValue(payload);
    }

    /**
     * Updates status for the current user's arrivalIndex node.
     */
    public void updateStatus(@NonNull String newStatus) {
        String uid = requireUid();
        DatabaseReference statusRef = rootRef.child("arrivalIndex").child(uid).child("status");
        statusRef.setValue(newStatus);
    }

    /**
     * Deletes the current user's arrivalIndex node, fully de-authorizing RFID.
     */
    public void clearArrivalIndex() {
        String uid = requireUid();
        rootRef.child("arrivalIndex").child(uid).removeValue();
    }
}
