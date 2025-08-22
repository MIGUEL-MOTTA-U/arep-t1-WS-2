package escuelaing.edu.co.framework.models;

public class HTTPFrameworkResponse {
    private int status;
    private String body;

    public HTTPFrameworkResponse() {
        this.status = 200;
        this.body = "";
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
