package com.example.signin;

import java.util.List;

/**
 * Model class representing an establishment.
 */
public class EstablishmentItem {

    private String id;
    private String name;
    private String hours;
    private String contactInfo;
    private String email;
    private String contactPerson;
    private List<String> departments;
    private List<String> services;

    // Default constructor required for Firebase deserialization
    public EstablishmentItem() {}

    public EstablishmentItem(String id, String name, String hours, String contactInfo,
                             String email, String contactPerson,
                             List<String> departments, List<String> services) {
        this.id = id;
        this.name = name;
        this.hours = hours;
        this.contactInfo = contactInfo;
        this.email = email;
        this.contactPerson = contactPerson;
        this.departments = departments;
        this.services = services;
    }

    // --- Getters ---
    public String getId() { return id; }
    public String getName() { return name; }
    public String getHours() { return hours; }
    public String getContactInfo() { return contactInfo; }
    public String getEmail() { return email; }
    public String getContactPerson() { return contactPerson; }
    public List<String> getDepartments() { return departments; }
    public List<String> getServices() { return services; }
}
