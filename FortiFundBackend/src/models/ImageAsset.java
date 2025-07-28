// models/ImageAsset.java
// POJO for image_assets table

package models;

public class ImageAsset {
    private int id;
    private String sectionName;
    private String assetKey;
    private String imageUrl;
    private String altText;

    public ImageAsset(int id, String sectionName, String assetKey, String imageUrl, String altText) {
        this.id = id;
        this.sectionName = sectionName;
        this.assetKey = assetKey;
        this.imageUrl = imageUrl;
        this.altText = altText;
    }

    public ImageAsset(String sectionName, String assetKey, String imageUrl, String altText) {
        this.sectionName = sectionName;
        this.assetKey = assetKey;
        this.imageUrl = imageUrl;
        this.altText = altText;
    }

    // Getters
    public int getId() { return id; }
    public String getSectionName() { return sectionName; }
    public String getAssetKey() { return assetKey; }
    public String getImageUrl() { return imageUrl; }
    public String getAltText() { return altText; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }
    public void setAssetKey(String assetKey) { this.assetKey = assetKey; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setAltText(String altText) { this.altText = altText; }
}