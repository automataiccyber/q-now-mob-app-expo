package com.example.signin;

/**
 * Represents a patient's recent visit to an establishment/counter.
 */
public class VisitedItem {
    public String estId;
    public String estName;
    public String counterId;
    public String counterName;
    public String reason;

    public long joinTime;        // Epoch millis when patient joined queue
    public long serviceStartTime; // Epoch millis when service started
    public long serviceEndTime;   // Epoch millis when service ended
    public long waitMs;           // Optional precomputed wait time
    public long serviceMs;        // Optional service duration
    public long timestamp;        // General timestamp of visit

    public VisitedItem() {}

    public String getEstName() { return estName; }
    public String getCounterName() { return counterName; }
    public String getReason() { return reason; }

    /** Returns the effective wait time in milliseconds */
    public long getWaitMillis() {
        if (waitMs > 0) return waitMs;
        if (serviceStartTime > 0 && joinTime > 0) return serviceStartTime - joinTime;
        return 0;
    }

    /** Returns the timestamp of when the patient completed the visit */
    public long getVisitedAt() {
        return serviceEndTime > 0 ? serviceEndTime : timestamp;
    }
}
