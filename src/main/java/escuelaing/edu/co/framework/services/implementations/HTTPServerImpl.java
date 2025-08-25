package escuelaing.edu.co.framework.services.implementations;

import escuelaing.edu.co.framework.errors.HttpServerErrors;
import escuelaing.edu.co.framework.models.HTTPFrameworkRequest;
import escuelaing.edu.co.framework.models.HTTPFrameworkResponse;
import escuelaing.edu.co.framework.services.interfaces.HTTPServerHandler;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A simple HTTP server that handles basic requests and serves files.
 * @author Miguel Angel Motta
 * @version 1.1
 * @since 2025-08-22
 */
public class HTTPServerImpl {
    private static String RESOURCES_PATH = "src/main/resources";
    private static final Logger logger = Logger.getLogger(HTTPServerImpl.class.getName());
    private static final Map<String, HTTPServerHandler> routes = new HashMap<>();
    private static boolean running = false;

    /**
     * Registers a GET route with the specified URL and callback handler.
     * @param url the URL path for the GET request
     * @param callback the handler to process the GET request
     */
    public static void get(String url, HTTPServerHandler callback) {
        routes.put(url, callback);
    }

    public static void staticFiles(String path) {
        File file = new File(RESOURCES_PATH + path);
        if (!file.exists() || !file.isDirectory()) {
            try {
                file.mkdirs();
            } catch (SecurityException e) {
                logger.warning("Could not create directory: " + path + " due to security restrictions.");
            }
        }
        RESOURCES_PATH += path;
    }

    // TODO: Implement POST, PUT, DELETE methods
    public static void post(String url, HTTPServerHandler callback) {
        logger.warning("POST method is not implemented yet.");
    }

    public static void put(String url, HTTPServerHandler callback) {
        logger.warning("PUT method is not implemented yet.");
    }

    public static void delete(String url, HTTPServerHandler callback) {
        logger.warning("DELETE method is not implemented yet.");
    }

    /**
     * Main method to start the HTTP server.
     * The server listens on given port and handles incoming requests.
     * @param port the server port to listen on
     */
    public static void start(int port) {
        ServerSocket serverSocket = null;
        running = true;
        try {
            serverSocket = new ServerSocket(port);
            logger.info("Running Server... on port: " + port);
        } catch (IOException e) {
            logger.warning("Could not listen on port: " + port);
            System.exit(1);
        }
        Socket clientSocket = null;
        while (running){
            try {
                clientSocket = acceptClient(serverSocket);
                handleRequest(clientSocket);
            } catch (IOException e) {
                logger.severe("Error handling request: " + e.getMessage());
            }
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            logger.warning("Could not close the server socket.");
        }
    }

    /**
     * Stops the HTTP server.
     * It sets the running flag to false, which will cause the server loop to exit.
     */
    public static void stop() {
        stopServer();
    }

    /**
     * Accepts a client connection and returns the connected socket.
     * @param serverSocket the server socket to accept connections from
     * @return the socket connected to the client
     * @throws IOException if an I/O error occurs when accepting the connection
     */
    private static Socket acceptClient(ServerSocket serverSocket) throws IOException {
        logger.info("Waiting for a client connection...");
        Socket clientSocket;
        clientSocket = serverSocket.accept();
        logger.info("New connection accepted");
        return clientSocket;
    }

    /**
     * Handles the incoming request from the client.
     * It reads the request line, validates it, and processes the request based on the URI.
     * @param clientSocket the socket connected to the client
     * @throws IOException if an I/O error occurs when reading from or writing to the socket
     */
    private static void handleRequest(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader( new InputStreamReader( clientSocket.getInputStream()));
        String line = in.readLine();
        if (line == null || line.split(" ").length <= 1) {
            handleErrorRequest(clientSocket, HttpServerErrors.BAD_REQUEST_400);
            clientSocket.close();
            return;
        }
        String[] parts = line.split(" ");
        String uri = parts[1]; // images/logo.png
        String path = obtainFilePath(uri);

        try {
            if (routes.containsKey(path)) {
                HTTPFrameworkResponse response = routes.get(path).handleRequest(new HTTPFrameworkRequest(uri), new HTTPFrameworkResponse());
                handleDynamicRoute(response, clientSocket);
            }
            handleStaticRoute(path, clientSocket);
        } catch (HttpServerErrors e) {
            logger.warning("Error handling request: " + e.getMessage());
            handleErrorRequest(clientSocket, e);
            clientSocket.close();
        } catch (Exception e) {
            BufferedOutputStream outData = new BufferedOutputStream(clientSocket.getOutputStream());
            logger.severe("Unexpected error: " + e.getMessage());
            logger.severe("Stack trace: ");
            e.printStackTrace();
            sendResponse(new PrintWriter(clientSocket.getOutputStream(), true), outData,"500 Internal Server Error");
            outData.close();
        }
        in.close();
        clientSocket.close();
    }

    /**
     * Handles error requests by sending an appropriate HTTP error response.
     * It constructs the response based on the provided HttpServerErrors instance.
     * @param clientSocket the socket connected to the client
     * @param error the HttpServerErrors instance representing the error
     * @throws IOException if an I/O error occurs when writing to the socket
     */
    private static void handleErrorRequest(Socket clientSocket, HttpServerErrors error) throws IOException {
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedOutputStream outData = new BufferedOutputStream(clientSocket.getOutputStream());
        byte[] body = error.getMessage().getBytes(StandardCharsets.UTF_8);

        out.println("HTTP/1.1 " + error.CODE + " " + error.getMessage());
        out.println("Content-Type: text/plain; charset=UTF-8");
        addCORSHeaders(out);
        out.println("Content-Length: " + body.length);
        out.println();
        out.flush();

        outData.write(body);
        outData.flush();

        outData.close();
        out.close();
    }



    /**
     * Handles valid routes based on the URI.
     * It serves files or processes specific requests like "/stop", "/name", and "/books".
     * @param uri the requested URI
     * @param clientSocket the socket connected to the client
     * @throws IOException if an I/O error occurs when reading from or writing to the socket
     */
    private static void handleStaticRoute(String uri, Socket clientSocket) throws IOException {
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedOutputStream outData = new BufferedOutputStream(clientSocket.getOutputStream());
        sendAnyFile(out, outData,uri);
        outData.close();
        out.close();
    }

    /**
     * Handles dynamic routes by sending the response generated by the route handler.
     * It writes the response body to the client socket.
     * @param response the HTTPFrameworkResponse containing the response body
     * @param clientSocket the socket connected to the client
     * @throws IOException if an I/O error occurs when writing to the socket
     */
    private static void handleDynamicRoute(HTTPFrameworkResponse response, Socket clientSocket) throws IOException {
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedOutputStream outData = new BufferedOutputStream(clientSocket.getOutputStream());
        String responseRaw = response.getBody();
        sendResponse(out, outData, responseRaw);
        outData.close();
        out.close();
    }

    /**
     * Sends a file response to the client.
     * It reads the file from the resources directory and sends it back with the appropriate content type.
     * @param out the PrintWriter to write the response
     * @param outData the BufferedOutputStream to write the file data
     * @param filePath the path of the file to be sent
     * @param contentType the content type of the file
     */
    private static void sendFileResponse(PrintWriter out, BufferedOutputStream outData, String filePath, String contentType) throws IOException {
        File file = new File(RESOURCES_PATH + filePath);
        if (!file.exists()) {
            throw HttpServerErrors.NOT_FOUND_404;
        }
        try {
            byte[] fileData = new byte[(int) file.length()];
            FileInputStream fileIn = new FileInputStream(file);
            fileIn.read(fileData);
            fileIn.close();

            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: " + contentType);
            out.println("Content-Length: " + fileData.length);
            out.println();

            outData.write(fileData, 0, fileData.length);
        } catch (IOException e) {
            logger.warning("File not found: " + filePath);
            sendResponse(out, outData,"404 Not Found");
        }
    }

    /**
     * Sends any file based on the provided file path.
     * It determines the content type based on the file extension and sends the file response.
     * @param out the PrintWriter to write the response
     * @param outData the BufferedOutputStream to write the file data
     * @param filePath the path of the file to be sent
     * @throws IOException if an I/O error occurs when reading from or writing to the socket
     */
    private static void sendAnyFile(PrintWriter out, BufferedOutputStream outData, String filePath) throws IOException {
        File file = new File(RESOURCES_PATH+filePath);
        if (!file.exists()) {
            throw HttpServerErrors.NOT_FOUND_404;
        }
        if(filePath.contains(".png") || filePath.contains(".jpg") || filePath.contains(".jpeg")) {
            sendFileResponse(out, outData, filePath, "image/png");
        } else if(filePath.contains(".css")) {
            sendFileResponse(out, outData, filePath, "text/css");
        } else if(filePath.contains(".js")) {
            sendFileResponse(out, outData, filePath, "application/javascript");
        } else if(filePath.contains(".html")) {
            sendFileResponse(out, outData, filePath, "text/html");
        } else {
            sendFileResponse(out, outData, filePath, "application/octet-stream");
        }
    }



    /**
     * Sends a JSON response to the client.
     * It constructs a JSON string and sends it back with the appropriate content type.
     * @param out the PrintWriter to write the response
     * @param outData the BufferedOutputStream to write the JSON data
     * @param jsonContent the JSON content to be sent
     * @throws IOException if an I/O error occurs when writing to the socket
     */
    private static void sendJSONResponse(PrintWriter out, BufferedOutputStream outData, String jsonContent) throws IOException {
        byte[] body = jsonContent.getBytes(StandardCharsets.UTF_8);
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: application/json; charset=UTF-8");
        addCORSHeaders(out);
        out.println("Content-Length: " + body.length);
        out.println();
        out.flush();
        outData.write(body);
        outData.flush();
    }


    /**
     * Sends a plain text response to the client.
     * It constructs a response with the given content and sends it back with the appropriate headers.
     * @param out the PrintWriter to write the response
     * @param outData the BufferedOutputStream to write the response data
     * @param content the content to be sent in the response
     * @throws IOException if an I/O error occurs when writing to the socket
     */
    private static void sendResponse(PrintWriter out, BufferedOutputStream outData, String content) throws IOException {
        byte[] body = content.getBytes(StandardCharsets.UTF_8);
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: text/plain; charset=UTF-8");
        addCORSHeaders(out);
        out.println("Content-Length: " + body.length);
        out.println();
        out.flush();
        outData.write(body);
        outData.flush();
    }


    private static void stopServer() {
        running = false;
        logger.info("Server is stopping...");
    }

    private static String obtainFilePath(String uri) {
        String path = uri.split("\\?")[0];
        if (path.equals("/")) {
            return "/index.html";
        }
        return path;
    }

    /**
     * Adds CORS headers to the response.
     * @param out the PrintWriter to write the headers
     */
    private static void addCORSHeaders(PrintWriter out) {
        out.println("Access-Control-Allow-Origin: *");
        out.println("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS");
        out.println("Access-Control-Allow-Headers: Content-Type");
    }
}
