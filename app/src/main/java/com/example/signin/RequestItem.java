package com.example.signin;

/**
 * Model class for queue and appointment requests.
 */
public class RequestItem {

    public String type;           // "Queue Request" or "Appointment Request"
    public String estId;          // Establishment ID
    public String estName;        // Establishment name (optional)
    public String counterId;      // Counter ID (for queue requests)
    public String counterName;    // Counter name (optional)
    public String reason;         // Reason for request
    public String status;         // "requested", "pending", "accepted", etc.
    public String requestKey;     // Key for queue requests
    public String appointmentKey; // Key for appointment requests
    public long timestamp;        // Time request was made

    public RequestItem() {} // Default constructor required for Firebase

    @Override
    public String toString() {
        return "RequestItem{" +
                "type='" + type + '\'' +
                ", estId='" + estId + '\'' +
                ", estName='" + estName + '\'' +
                ", counterId='" + counterId + '\'' +
                ", counterName='" + counterName + '\'' +
                ", reason='" + reason + '\'' +
                ", status='" + status + '\'' +
                ", requestKey='" + requestKey + '\'' +
                ", appointmentKey='" + appointmentKey + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
