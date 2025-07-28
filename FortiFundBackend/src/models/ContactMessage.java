// models/ContactMessage.java
// POJO for contact_messages table

package models;

import java.sql.Timestamp;

public class ContactMessage {
    private int id;
    private String name;
    private String email;
    private String message;
    private Timestamp submissionTime;

    public ContactMessage(int id, String name, String email, String message, Timestamp submissionTime) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.message = message;
        this.submissionTime = submissionTime;
    }

    public ContactMessage(String name, String email, String message) {
        this.name = name;
        this.email = email;
        this.message = message;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getMessage() { return message; }
    public Timestamp getSubmissionTime() { return submissionTime; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setMessage(String message) { this.message = message; }
    public void setSubmissionTime(Timestamp submissionTime) { this.submissionTime = submissionTime; }
}