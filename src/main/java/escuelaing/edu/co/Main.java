package escuelaing.edu.co;

import escuelaing.edu.co.framework.services.implementations.HTTPServerImpl;
import escuelaing.edu.co.framework.services.interfaces.HTTPServerService;

public class Main {
    public static void main(String[] args) {
        HTTPServerService server = HTTPServerImpl.getInstance();
        server.get("/hello", (request, response) -> {
            response.setStatus(200);
            response.setBody("Hello, World!");
            return response;
        });

        server.get("/hello2", (request, response) -> {
            response.setBody("Hello " + request.getValue("name"));
            System.out.println("The name is: " + request.getValue("name"));
            return response;
        });

        server.start(8080);
    }
}
