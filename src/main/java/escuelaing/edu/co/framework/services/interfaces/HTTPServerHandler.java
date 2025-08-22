package escuelaing.edu.co.framework.services.interfaces;

import escuelaing.edu.co.framework.models.HTTPFrameworkRequest;
import escuelaing.edu.co.framework.models.HTTPFrameworkResponse;

public interface HTTPServerHandler {
    HTTPFrameworkResponse handleRequest(HTTPFrameworkRequest request, HTTPFrameworkResponse response);
}
