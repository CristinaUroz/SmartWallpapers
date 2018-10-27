package uroz.cristina.smartwallpapers;

public class Quote {
    private String title;
    private String autor;
    private boolean liked;
    private boolean favorite;
    private boolean deleted;

    public Quote ( String title, String autor, boolean liked, boolean favorite, boolean deleted) {

        this.title = title;
        this.autor = autor;
        this.liked = liked;
        this.deleted = deleted;
        this.favorite = favorite;
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

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public void toggleLiked() {
        this.liked=!this.liked;
    }

    public void toggleFavorite() {
        this.favorite=!this.favorite;
    }
}
