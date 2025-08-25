package escuelaing.edu.co;

import escuelaing.edu.co.framework.services.implementations.HTTPServerImpl;
import escuelaing.edu.co.framework.services.interfaces.HTTPServerService;

public class Main {
    public static void main(String[] args) {
        HTTPServerService server = HTTPServerImpl.getInstance();

        server.get("/health", (request, response) -> {
            response.setStatus(200);
            response.setBody("The Server is working correctly!");
            return response;
        });

        server.get("/hello", (request, response) -> {
            response.setBody("Hello " + request.getValue("name"));
            return response;
        });

        server.get("/pi", (request, response) -> {
            response.setBody("Pi is approximately "+ Math.PI);
            return response;
        });

        server.staticFiles("/webroot");

        server.start(8080);
    }
}
