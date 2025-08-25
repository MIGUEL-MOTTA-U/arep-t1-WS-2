package escuelaing.edu.co;
import static escuelaing.edu.co.framework.services.implementations.HTTPServerImpl.*;

public class Main {
    public static void main(String[] args) {
        get("/health", (request, response) -> {
            response.setStatus(200);
            response.setBody("The Server is working correctly!");
            return response;
        });
        get("/hello", (request, response) -> {
            response.setBody("Hello " + request.getValue("name"));
            return response;
        });
        get("/pi", (request, response) -> {
            response.setBody("Pi is approximately "+ Math.PI);
            return response;
        });
        staticFiles("/webroot");
        start(8080);
    }
}