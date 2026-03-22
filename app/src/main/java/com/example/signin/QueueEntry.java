package com.example.signin;

/**
 * Represents a single patient entry in a queue.
 */
public class QueueEntry {
    private String userId;
    private String status;       // "waiting", "in_service", "done", etc.
    private long joinedAt;       // epoch millis
    private String displayName;  // Patient's display name
    private String reason;       // Reason for joining the queue

    // Required empty constructor for Firebase
    public QueueEntry() {}

    public QueueEntry(String userId, String status, long joinedAt, String displayName, String reason) {
        this.userId = userId;
        this.status = status;
        this.joinedAt = joinedAt;
        this.displayName = displayName;
        this.reason = reason;
    }

    // Getters
    public String getUserId() { return userId; }
    public String getStatus() { return status; }
    public long getJoinedAt() { return joinedAt; }
    public String getDisplayName() { return displayName; }
    public String getReason() { return reason; }

    // Setters
    public void setUserId(String userId) { this.userId = userId; }
    public void setStatus(String status) { this.status = status; }
    public void setJoinedAt(long joinedAt) { this.joinedAt = joinedAt; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setReason(String reason) { this.reason = reason; }
}
