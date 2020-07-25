package kit.http;

public class Response {

    int status;
    String content;

    public Response(int status, String content) {
        this.status = status;
        this.content = content;
    }

    public String getBody() {
        return content;
    }

    public int getStatus() {
        return status;
    }
}
