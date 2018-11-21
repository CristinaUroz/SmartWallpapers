package uroz.cristina.smartwallpapers;

public class Quote {

    private String quotation;
    private String author;
    private String category;


    public Quote ( String quotation, String author, String category) {


        this.quotation = quotation;
        this.author = author;
        this.category = category;
    }


    public String getQuotation() {
        return quotation;
    }

    public String getAuthor() {
        return author;
    }

    public String getCategory() {return category;}

}
