package com.example.signin;

/**
 * Represents a counter within an establishment.
 */
public class CounterItem {
    private String estId;
    private String counterId;
    private String estName;
    private String counterName;
    private String avgQueueTime; // e.g., "15 mins"

    public CounterItem() {} // Needed for Firebase or serialization

    public CounterItem(String estId, String counterId, String estName, String counterName, String avgQueueTime) {
        this.estId = estId;
        this.counterId = counterId;
        this.estName = estName;
        this.counterName = counterName;
        this.avgQueueTime = avgQueueTime;
    }

    public String getEstId() { return estId; }
    public void setEstId(String estId) { this.estId = estId; }

    public String getCounterId() { return counterId; }
    public void setCounterId(String counterId) { this.counterId = counterId; }

    public String getEstName() { return estName; }
    public void setEstName(String estName) { this.estName = estName; }

    public String getCounterName() { return counterName; }
    public void setCounterName(String counterName) { this.counterName = counterName; }

    public String getAvgQueueTime() { return avgQueueTime; }
    public void setAvgQueueTime(String avgQueueTime) { this.avgQueueTime = avgQueueTime; }
}
