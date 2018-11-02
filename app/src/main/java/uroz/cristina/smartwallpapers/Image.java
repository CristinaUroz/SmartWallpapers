package uroz.cristina.smartwallpapers;

public class Image {
    private String src;
    private int imageId;
    private String title;
    private String autor;
    private boolean liked;
    private boolean deleted;

    public Image(String src, String title, String autor , boolean liked, boolean deleted) {
        this.src=src;
        this.imageId = imageId;
        this.title = title;
        this.autor = autor;
        this.liked = liked;
        this.deleted = deleted;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
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

    public String getAutor() {
        return autor;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked() {
        this.liked = true;
    }

    public void setAutor(String descriptor) {
        this.autor = autor;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted() {
        this.deleted = true;
    }


}
