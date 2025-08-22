package escuelaing.edu.co.framework.services.implementations;

import escuelaing.edu.co.framework.errors.HttpServerErrors;
import escuelaing.edu.co.framework.models.HTTPFrameworkRequest;
import escuelaing.edu.co.framework.models.HTTPFrameworkResponse;
import escuelaing.edu.co.framework.services.interfaces.HTTPServerHandler;
import escuelaing.edu.co.framework.services.interfaces.HTTPServerService;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A simple HTTP server that handles basic requests and serves files.
 * @author Miguel Angel Motta
 * @version 1.1
 * @since 2025-08-22
 */
public class HTTPServerImpl implements HTTPServerService {
    private static HTTPServerImpl instance;
    private final String RESOURCES_PATH = "src/main/resources";
    private final Logger logger = Logger.getLogger(HTTPServerImpl.class.getName());
    private final Map<String, HTTPServerHandler> routes;
    private boolean running = false;
    private final List<String> box;


    public static HTTPServerImpl getInstance() {
        if (instance == null) {
            instance = new HTTPServerImpl();
        }
        return instance;
    }

    private HTTPServerImpl() {
        routes = new HashMap<>();
        box = new ArrayList<>();
    }

    @Override
    public void get(String url, HTTPServerHandler callback) {
        this.routes.put(url, callback);
        logger.warning("GET method is not implemented yet.");
    }

    @Override
    public void post(String url, HTTPServerHandler callback) {

    }

    @Override
    public void put(String url, HTTPServerHandler callback) {

    }

    @Override
    public void delete(String url, HTTPServerHandler callback) {

    }

    /**
     * Main method to start the HTTP server.
     * The server listens on given port and handles incoming requests.
     * @param port the server port to listen on
     */
    public void start(int port) {
        ServerSocket serverSocket = null;
        running = true;
        try {
            serverSocket = new ServerSocket(port);
            logger.info("Running Server... on port 35000");
        } catch (IOException e) {
            logger.warning("Could not listen on port: 35000.");
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

    @Override
    public void stop() {
        stopServer();

    }

    /**
     * Prints the trace of the input stream from the client.
     * This method reads lines from the input stream and logs them until no more data is available.
     * @param in the BufferedReader to read from
     * @throws IOException if an I/O error occurs when reading from the input stream
     */
    public void printTrace(BufferedReader in) throws IOException {
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            logger.info("----> Request received: " + inputLine);
            if (!in.ready()) {
                break;
            }
        }
    }

    /**
     * Accepts a client connection and returns the connected socket.
     * @param serverSocket the server socket to accept connections from
     * @return the socket connected to the client
     * @throws IOException if an I/O error occurs when accepting the connection
     */
    public Socket acceptClient(ServerSocket serverSocket) throws IOException {
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
    public void handleRequest(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader( new InputStreamReader( clientSocket.getInputStream()));
        String line = in.readLine();
        if (line == null || line.split(" ").length <= 1) {
            handleErrorRequest(clientSocket, HttpServerErrors.BAD_REQUEST_400);
            clientSocket.close();
            return;
        }
        String[] parts = line.split(" ");
        String uri = parts[1];
        String path = obtainFilePath(uri);

        try {

            if (routes.containsKey(path)) {
                HTTPFrameworkResponse response = routes.get(path).handleRequest(new HTTPFrameworkRequest(uri), new HTTPFrameworkResponse());
                handleDinamicRoute(response, clientSocket);
            } else {
                handleValidRoute(uri, clientSocket);
            }
        } catch (HttpServerErrors e) {
            logger.warning("Error handling request: " + e.getMessage());
            handleErrorRequest(clientSocket, e);
            clientSocket.close();
        } catch (Exception e) {
            BufferedOutputStream outData = new BufferedOutputStream(clientSocket.getOutputStream());
            logger.severe("Unexpected error: " + e.getMessage());
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
    public void handleErrorRequest(Socket clientSocket, HttpServerErrors error) throws IOException {
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
    public void handleValidRoute(String uri, Socket clientSocket) throws IOException {
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedOutputStream outData = new BufferedOutputStream(clientSocket.getOutputStream());
        String path = obtainFilePath(uri);
        switch (path) {
            case "/":
                sendFileResponse(out, outData, "/index.html", "text/html");
                break;
            case "/stop":
                stopServer();
                sendResponse(out,outData, "Server is stopping...");
                break;
            case "/name":
                sendResponse(out,outData, "The Server name is: TEST SERVER VALUE");
                break;
            case "/about":
                sendFileResponse(out, outData, "/about/about.html", "text/html");
                break;
            case "/api":
                sendFileResponse(out, outData, "/api/api.html", "text/html");
                break;
            case "/books":
                if (uri.split("\\?").length > 1 && uri.split("\\?")[1].startsWith("name=")) {
                    String bookName = uri.split("\\?")[1].split("=")[1];
                    saveData(bookName);
                }
                sendJSONResponse(out, outData, "{\"message\": \"Books saved: " + box.toString() + "\"}");
                break;

            default:
            sendAnyFile(out, outData, path);
                break;
        }
        outData.close();
        out.close();
    }

    public void handleDinamicRoute(HTTPFrameworkResponse response, Socket clientSocket) throws IOException {
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
    public void sendFileResponse(PrintWriter out, BufferedOutputStream outData, String filePath, String contentType) throws IOException {
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
    public void sendAnyFile(PrintWriter out, BufferedOutputStream outData, String filePath) throws IOException {
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
     * Saves the data received from the client.
     * It decodes the value and adds it to the box list, logging the action.
     * @param value the value to be saved
     */
    public void saveData(String value) {
        String parsedData = URLDecoder.decode(value, StandardCharsets.UTF_8);
        box.add(parsedData);
        logger.info("Data saved: " + parsedData);
    }


    /**
     * Sends a JSON response to the client.
     * It constructs a JSON string and sends it back with the appropriate content type.
     * @param out the PrintWriter to write the response
     * @param outData the BufferedOutputStream to write the JSON data
     * @param jsonContent the JSON content to be sent
     * @throws IOException if an I/O error occurs when writing to the socket
     */
    public void sendJSONResponse(PrintWriter out, BufferedOutputStream outData, String jsonContent) throws IOException {
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
    public void sendResponse(PrintWriter out, BufferedOutputStream outData, String content) throws IOException {
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


    /**
     * Stops the server by setting the running flag to false.
     * This will cause the main loop to exit and the server to shut down gracefully.
     */
    public void stopServer() {
        running = false;
        logger.info("Server is stopping...");
    }

    private String obtainFilePath(String uri) {
        String path = uri.split("\\?")[0];
        if (path.equals("/")) {
            return "/index.html";
        }
        return path;
    }

    private void addCORSHeaders(PrintWriter out) {
        out.println("Access-Control-Allow-Origin: *");
        out.println("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS");
        out.println("Access-Control-Allow-Headers: Content-Type");
    }
}
