package escuelaing.edu.co.framework.services.interfaces;

public interface HTTPServerService {
    void get(String url, HTTPServerHandler callback);
}
