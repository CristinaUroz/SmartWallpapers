package uroz.cristina.smartwallpapers;

public class Quote {
    private String title;
    private String autor;
    private boolean liked;
    private boolean deleted;

    public Quote ( String title, String autor, boolean liked, boolean deleted) {

        this.title = title;
        this.autor = autor;
        this.liked = liked;
        this.deleted = deleted;
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

    public void setAutor(String descriptor) {
        this.autor = autor;
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
