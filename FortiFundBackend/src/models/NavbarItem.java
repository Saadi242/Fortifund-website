// models/NavbarItem.java
// POJO for navbar_items table

package models;

public class NavbarItem {
    private int id;
    private String itemText;
    private String itemHref;
    private boolean isDropdown;
    private Integer parentId; // Use Integer for nullable foreign key
    private int displayOrder;

    public NavbarItem(int id, String itemText, String itemHref, boolean isDropdown, Integer parentId, int displayOrder) {
        this.id = id;
        this.itemText = itemText;
        this.itemHref = itemHref;
        this.isDropdown = isDropdown;
        this.parentId = parentId;
        this.displayOrder = displayOrder;
    }

    public NavbarItem(String itemText, String itemHref, boolean isDropdown, Integer parentId, int displayOrder) {
        this.itemText = itemText;
        this.itemHref = itemHref;
        this.isDropdown = isDropdown;
        this.parentId = parentId;
        this.displayOrder = displayOrder;
    }

    // Getters
    public int getId() { return id; }
    public String getItemText() { return itemText; }
    public String getItemHref() { return itemHref; }
    public boolean isDropdown() { return isDropdown; }
    public Integer getParentId() { return parentId; }
    public int getDisplayOrder() { return displayOrder; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setItemText(String itemText) { this.itemText = itemText; }
    public void setItemHref(String itemHref) { this.itemHref = itemHref; }
    public void setDropdown(boolean dropdown) { isDropdown = dropdown; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
}