package uroz.cristina.smartwallpapers;

public class Category {
    private int imageId;
    private String title;
    private boolean liked;
    private boolean deleted;

    public Category(int imageId, String title, boolean liked, boolean deleted) {
        this.imageId = imageId;
        this.title = title;
        this.liked = liked;
        this.deleted = deleted;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked() {
        this.liked = true;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted() {
        this.deleted = true;
    }

}
