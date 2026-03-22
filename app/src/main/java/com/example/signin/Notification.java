package com.example.signin;

import com.google.firebase.Timestamp;

/**
 * Represents a notification for a patient related to either a queue, appointment, or arrival update.
 */
public class Notification {

    private String estId;          // Establishment ID
    private String estName;        // Establishment Name

    private String counterId;      // Counter ID
    private String counterName;    // Counter Name

    private String service;        // Service Name
    private String reason;         // Reason for Notification

    private String message;        // Notification message content
    // Store raw value from Firestore to gracefully handle both Long and Timestamp
    private Object timestamp;      // Can be Long, Timestamp, or null

    private String type;           // "queue", "appointment", "arrival_update"
    private String status;         // Queue status like "awaiting_arrival", "waiting", "in_service"

    private int chainOrder;        // Chain order for chained queues (0 if not part of a chain)

    public Notification() {
        // Required by Firebase
    }

    public Notification(String estId, String estName,
                        String counterId, String counterName,
                        String service, String reason,
                        String message, long timestamp, String type,
                        String status, int chainOrder) {
        this.estId = estId;
        this.estName = estName;
        this.counterId = counterId;
        this.counterName = counterName;
        this.service = service;
        this.reason = reason;
        this.message = message;
        this.timestamp = timestamp; // autobox to Long
        this.type = type;
        this.status = status;
        this.chainOrder = chainOrder;
    }

    // ----- GETTERS & SETTERS -----

    /**
     * Returns timestamp as epoch millis. Supports Long and Firestore Timestamp.
     * Falls back to 0L when value is missing or of unexpected type.
     */
    public long getTimestamp() {
        if (timestamp == null) return 0L;
        if (timestamp instanceof Long) return (Long) timestamp;
        if (timestamp instanceof Integer) return ((Integer) timestamp).longValue();
        if (timestamp instanceof Timestamp) {
            return ((Timestamp) timestamp).toDate().getTime();
        }
        // Unknown type, avoid crash
        return 0L;
    }

    // Single, generic setter to avoid Firestore overload issues
    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEstId() {
        return estId;
    }

    public void setEstId(String estId) {
        this.estId = estId;
    }

    public String getEstName() {
        return estName;
    }

    public void setEstName(String estName) {
        this.estName = estName;
    }

    public String getCounterId() {
        return counterId;
    }

    public void setCounterId(String counterId) {
        this.counterId = counterId;
    }

    public String getCounterName() {
        return counterName;
    }

    public void setCounterName(String counterName) {
        this.counterName = counterName;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getChainOrder() {
        return chainOrder;
    }

    public void setChainOrder(int chainOrder) {
        this.chainOrder = chainOrder;
    }
}