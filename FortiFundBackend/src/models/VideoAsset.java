// models/VideoAsset.java
// POJO for video_assets table

package models;

public class VideoAsset {
    private int id;
    private String sectionName;
    private String assetKey;
    private String videoUrl;
    private String posterUrl;

    public VideoAsset(int id, String sectionName, String assetKey, String videoUrl, String posterUrl) {
        this.id = id;
        this.sectionName = sectionName;
        this.assetKey = assetKey;
        this.videoUrl = videoUrl;
        this.posterUrl = posterUrl;
    }

    public VideoAsset(String sectionName, String assetKey, String videoUrl, String posterUrl) {
        this.sectionName = sectionName;
        this.assetKey = assetKey;
        this.videoUrl = videoUrl;
        this.posterUrl = posterUrl;
    }

    // Getters
    public int getId() { return id; }
    public String getSectionName() { return sectionName; }
    public String getAssetKey() { return assetKey; }
    public String getVideoUrl() { return videoUrl; }
    public String getPosterUrl() { return posterUrl; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }
    public void setAssetKey(String assetKey) { this.assetKey = assetKey; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
}