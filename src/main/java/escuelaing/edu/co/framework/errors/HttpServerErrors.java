package escuelaing.edu.co.framework.errors;

/**
 * Custom error class for HTTP server errors.
 * This class extends the Error class and provides specific HTTP error codes and messages.
 * @author Miguel Angel Motta
 * @version 1.0
 * @since 2025-08-15
 */
public class HttpServerErrors extends Error {
    public static final HttpServerErrors NOT_FOUND_404 = new HttpServerErrors("404 Not Found", 404);
    public static final HttpServerErrors INTERNAL_SERVER_ERROR_500 = new HttpServerErrors("500 Internal Server Error", 500);
    public static final HttpServerErrors BAD_REQUEST_400 = new HttpServerErrors("400 Bad Request", 400);
    public static final HttpServerErrors FORBIDDEN_403 = new HttpServerErrors("403 Forbidden", 403);
    public static final HttpServerErrors METHOD_NOT_ALLOWED_405 = new HttpServerErrors("405 Method Not Allowed", 405);

    public final int CODE;

    /*
     * Constructor for HttpServerErrors.
     * @param message the error message
     * @param code the HTTP status code associated with the error
     */
    private HttpServerErrors(String message, int code) {
        super(message);
        CODE = code;
    }


}
