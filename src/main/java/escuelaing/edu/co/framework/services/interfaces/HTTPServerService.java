package escuelaing.edu.co.framework.services.interfaces;

public interface HTTPServerService {
    void get(String url, HTTPServerHandler callback);
    void post(String url, HTTPServerHandler callback);
    void put(String url, HTTPServerHandler callback);
    void delete(String url, HTTPServerHandler callback);
    void start(int port);
    void stop();
    void staticFiles(String path);
}
