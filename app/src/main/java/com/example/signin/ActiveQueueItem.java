package com.example.signin;

/**
 * Model class for an active queue item based on arrivalIndex.
 */
public class ActiveQueueItem {

    public String estId;           // Establishment ID
    public String estName;         // Establishment Name
    public String counterId;       // Counter ID
    public String counterName;     // Counter Name
    public String uid;             // User ID
    public String reason;          // Reason for visit
    public String requestKey;      // Request Key (unique identifier for queue request)
    public String awaitingKey;     // Awaiting Key (authorization for arrival tracking)

    public String status;          // Queue status: "awaiting_arrival", "waiting", "in_service", etc.
    public int chainOrder;         // Specifies order if part of a chain queuing process

    public long joinTime;          // Epoch millis: when the user joined the queue
    public long serviceStartTime;  // Epoch millis: when the user started service
    public long timestamp;         // General timestamp marker in millis

    // Default Firebase constructor
    public ActiveQueueItem() { }

    public ActiveQueueItem(String estId, String estName,
                           String counterId, String counterName,
                           String uid, String reason,
                           String requestKey, String awaitingKey,
                           String status, int chainOrder,
                           long joinTime, long serviceStartTime, long timestamp) {
        this.estId = estId;
        this.estName = estName;
        this.counterId = counterId;
        this.counterName = counterName;
        this.uid = uid;
        this.reason = reason;
        this.requestKey = requestKey;
        this.awaitingKey = awaitingKey;
        this.status = status;
        this.chainOrder = chainOrder;
        this.joinTime = joinTime;
        this.serviceStartTime = serviceStartTime;
        this.timestamp = timestamp;
    }

    // Getters for adapter and Firebase
    public String getEstName() { return estName; }
    public String getCounterName() { return counterName; }
    public String getReason() { return reason; }
    public String getUid() { return uid; }
    public String getStatus() { return status; }
    public String getRequestKey() { return requestKey; }
    public String getAwaitingKey() { return awaitingKey; }
    public int getChainOrder() { return chainOrder; }
    public long getJoinTime() { return joinTime; }
    public long getServiceStartTime() { return serviceStartTime; }
    public long getTimestamp() { return timestamp; }
}