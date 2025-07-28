// models/FaqItem.java
// POJO for faq_items table

package models;

public class FaqItem {
    private int id;
    private String question;
    private String answer;
    private int displayOrder;

    public FaqItem(int id, String question, String answer, int displayOrder) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.displayOrder = displayOrder;
    }

    public FaqItem(String question, String answer, int displayOrder) {
        this.question = question;
        this.answer = answer;
        this.displayOrder = displayOrder;
    }

    // Getters
    public int getId() { return id; }
    public String getQuestion() { return question; }
    public String getAnswer() { return answer; }
    public int getDisplayOrder() { return displayOrder; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setQuestion(String question) { this.question = question; }
    public void setAnswer(String answer) { this.answer = answer; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
}