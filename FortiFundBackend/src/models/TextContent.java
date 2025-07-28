// models/TextContent.java
// POJO for text_content table

package models;

public class TextContent {
    private int id;
    private String sectionName;
    private String contentKey;
    private String contentValue;

    public TextContent(int id, String sectionName, String contentKey, String contentValue) {
        this.id = id;
        this.sectionName = sectionName;
        this.contentKey = contentKey;
        this.contentValue = contentValue;
    }

    public TextContent(String sectionName, String contentKey, String contentValue) {
        this.sectionName = sectionName;
        this.contentKey = contentKey;
        this.contentValue = contentValue;
    }

    // Getters
    public int getId() { return id; }
    public String getSectionName() { return sectionName; }
    public String getContentKey() { return contentKey; }
    public String getContentValue() { return contentValue; }

    // Setters (for updates, if needed)
    public void setId(int id) { this.id = id; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }
    public void setContentKey(String contentKey) { this.contentKey = contentKey; }
    public void setContentValue(String contentValue) { this.contentValue = contentValue; }
}