// models/DemoRequest.java
// POJO for demo_requests table

package models;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class DemoRequest {
    private int id;
    private String fullName;
    private String phoneNumber;
    private String company;
    private String email;
    private String comments;
    private Date demoDate;
    private Time demoTime;
    private Timestamp submissionTime;

    public DemoRequest(int id, String fullName, String phoneNumber, String company, String email, String comments, Date demoDate, Time demoTime, Timestamp submissionTime) {
        this.id = id;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.company = company;
        this.email = email;
        this.comments = comments;
        this.demoDate = demoDate;
        this.demoTime = demoTime;
        this.submissionTime = submissionTime;
    }

    public DemoRequest(String fullName, String phoneNumber, String company, String email, String comments, Date demoDate, Time demoTime) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.company = company;
        this.email = email;
        this.comments = comments;
        this.demoDate = demoDate;
        this.demoTime = demoTime;
    }

    // Getters
    public int getId() { return id; }
    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getCompany() { return company; }
    public String getEmail() { return email; }
    public String getComments() { return comments; }
    public Date getDemoDate() { return demoDate; }
    public Time getDemoTime() { return demoTime; }
    public Timestamp getSubmissionTime() { return submissionTime; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setCompany(String company) { this.company = company; }
    public void setEmail(String email) { this.email = email; }
    public void setComments(String comments) { this.comments = comments; }
    public void setDemoDate(Date demoDate) { this.demoDate = demoDate; }
    public void setDemoTime(Time demoTime) { this.demoTime = demoTime; }
    public void setSubmissionTime(Timestamp submissionTime) { this.submissionTime = submissionTime; }
}