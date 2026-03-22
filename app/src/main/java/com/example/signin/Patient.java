package com.example.signin;

/**
 * Model class representing a patient.
 */
public class Patient {
    public String name;
    public String age;
    public String address;
    public String contact;
    public String email;

    public boolean isPwd;                // Person with disability
    public boolean isElderly;            // Senior citizen
    public boolean hasSevereCondition;   // Severe medical condition

    // Required empty constructor for Firebase
    public Patient() { }

    public Patient(String name, String age, String address, String contact, String email,
                   boolean isPwd, boolean isElderly, boolean hasSevereCondition) {
        this.name = name;
        this.age = age;
        this.address = address;
        this.contact = contact;
        this.email = email;
        this.isPwd = isPwd;
        this.isElderly = isElderly;
        this.hasSevereCondition = hasSevereCondition;
    }
}
