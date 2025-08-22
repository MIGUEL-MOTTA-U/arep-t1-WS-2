package escuelaing.edu.co.framework.models;

import java.util.HashMap;
import java.util.Map;

public class HTTPFrameworkRequest {
    private String method;
    private String url;
    private final Map<String, String> params;

    public HTTPFrameworkRequest(String request) {
        this.method = "";
        this.url = "";
        this.params = new HashMap<>();
        parseRequest(request);
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public String getValue(String param) {
        return this.params.get(param);
    }

    private void parseRequest(String request) {
        String[] parts = request.split("\\?");
        if (parts.length > 0) {
            this.url = parts[0];
            parseParams(parts[1]);
        }
    }

    private void parseParams(String params) {
        String[] pairs = params.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                this.params.put(keyValue[0], keyValue[1]);
            }
        }
    }
}
