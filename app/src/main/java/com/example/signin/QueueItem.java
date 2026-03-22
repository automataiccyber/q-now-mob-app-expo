package com.example.signin;

/**
 * Represents a queue under an establishment/counter.
 */
public class QueueItem {
    private String establishmentId;
    private String counterId;
    private String displayName;

    // Required no-arg constructor for Firebase/serializers
    public QueueItem() {}

    public QueueItem(String establishmentId, String counterId, String displayName) {
        this.establishmentId = establishmentId;
        this.counterId = counterId;
        this.displayName = displayName;
    }

    // Getters
    public String getEstablishmentId() { return establishmentId; }
    public String getCounterId() { return counterId; }
    public String getDisplayName() { return displayName; }

    // Setters
    public void setEstablishmentId(String establishmentId) { this.establishmentId = establishmentId; }
    public void setCounterId(String counterId) { this.counterId = counterId; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}
