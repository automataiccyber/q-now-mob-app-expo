package com.example.signin;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.Date;

@IgnoreExtraProperties
public class InboxItem {
    private String id;
    private String sender;
    private String fileName;
    private String fileUrl;
    private String message;
    private Timestamp timestamp;  // Firestore Timestamp (was long - caused deserialize crash)
    private long size;
    private String type;

    // Firestore arrival/queue notification fields
    private String redirectUrl;
    private String estId;
    private String estName;
    private String counterId;
    private String counterName;
    private int chainOrder;
    private String status;

    public InboxItem() {
        // Required for Firebase
    }

    public InboxItem(String id, String sender, String fileName, String fileUrl, String message, long timestamp, long size, String type) {
        this.id = id;
        this.sender = sender;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.message = message;
        this.timestamp = timestamp > 0 ? new Timestamp(new Date(timestamp)) : null;
        this.size = size;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /** For Firestore deserialization - Timestamp type matches Firestore. */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /** Use this for display/sorting - returns milliseconds. */
    @Exclude
    public long getTimestampMillis() {
        return timestamp != null ? timestamp.toDate().getTime() : 0L;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
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

    public int getChainOrder() {
        return chainOrder;
    }

    public void setChainOrder(int chainOrder) {
        this.chainOrder = chainOrder;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
