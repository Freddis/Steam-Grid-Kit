package kit.griddb;

public class GridImage {

    private final int id;
    private final String url;
    private final String author;
    private final String style;

    public GridImage(int id, String style, String url, String author) {
        this.id = id;
        this.url = url;
        this.author = author;
        this.style = style;
    }

    public int getId()
    {
        return id;
    }
    public String getAuthor()
    {
        return author;
    }

    public String getUrl()
    {
        return url;
    }

    public String getStyle() {
        return style;
    }
}
