package escuelaing.edu.co.framework.services.implementations;

import escuelaing.edu.co.framework.models.HTTPFrameworkRequest;
import escuelaing.edu.co.framework.models.HTTPFrameworkResponse;
import escuelaing.edu.co.framework.services.interfaces.HTTPServerHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HTTPServerImplSimpleTest {

    private Path tmpResources;

    @Mock
    private HTTPServerHandler mockHandler;

    @BeforeEach
    void setUp() throws Exception {
        tmpResources = Files.createTempDirectory("httpserver_test_" + UUID.randomUUID());
        Field resPathField = HTTPServerImpl.class.getDeclaredField("RESOURCES_PATH");
        resPathField.setAccessible(true);
        resPathField.set(null, tmpResources.toAbsolutePath().toString());
    }

    @AfterEach
    void tearDown() throws Exception {
        if (tmpResources != null && Files.exists(tmpResources)) {
            Files.walk(tmpResources)
                    .map(Path::toFile)
                    .sorted((a, b) -> -a.compareTo(b))
                    .forEach(File::delete);
        }
    }

    @Test
    void get_registersRoute() throws Exception {
        HTTPServerImpl.get("/miRuta", mockHandler);
        Field routesField = HTTPServerImpl.class.getDeclaredField("routes");
        routesField.setAccessible(true);
        Map<String, HTTPServerHandler> routes = (Map<String, HTTPServerHandler>) routesField.get(null);
        assertTrue(routes.containsKey("/miRuta"), "La ruta registrada debe estar presente.");
        assertSame(mockHandler, routes.get("/miRuta"), "El handler almacenado debe ser el mismo objeto pasado.");
    }

    @Test
    void staticFiles_createsDirectoryAndUpdatesPath() throws Exception {
        String newDir = "/static_test_dir";
        HTTPServerImpl.staticFiles(newDir);
        Field resPathField = HTTPServerImpl.class.getDeclaredField("RESOURCES_PATH");
        resPathField.setAccessible(true);
        String newResPath = (String) resPathField.get(null);
        File dir = new File(newResPath);
        assertTrue(dir.exists() && dir.isDirectory(), "El directorio pasado a staticFiles debe haberse creado.");
        assertTrue(newResPath.endsWith(newDir), "RESOURCES_PATH debe actualizarse con el sufijo indicado.");
    }

    @Test
    void post_put_delete_doNotRegisterRoutes_inCurrentImpl() throws Exception {
        HTTPServerImpl.post("/p", mockHandler);
        HTTPServerImpl.put("/u", mockHandler);
        HTTPServerImpl.delete("/d", mockHandler);
        Field routesField = HTTPServerImpl.class.getDeclaredField("routes");
        routesField.setAccessible(true);
        Map<String, HTTPServerHandler> routes = (Map<String, HTTPServerHandler>) routesField.get(null);
        assertFalse(routes.containsKey("/p"), "POST no debería registrar rutas en la implementación actual.");
        assertFalse(routes.containsKey("/u"), "PUT no debería registrar rutas en la implementación actual.");
        assertFalse(routes.containsKey("/d"), "DELETE no debería registrar rutas en la implementación actual.");
    }

    @Test
    void stop_setsRunningFalse() throws Exception {
        Field runningField = HTTPServerImpl.class.getDeclaredField("running");
        runningField.setAccessible(true);
        runningField.set(null, true);
        HTTPServerImpl.stop();
        boolean runningAfter = (boolean) runningField.get(null);
        assertFalse(runningAfter, "Después de stop(), el flag running debe ser false.");
    }

    @Test
    void get_handlesQueryParameters() throws Exception {
        HTTPServerImpl.get("/hello", (request, response) -> {
            response.setBody("Hello " + request.getValue("name"));
            return response;
        });
        Field routesField = HTTPServerImpl.class.getDeclaredField("routes");
        routesField.setAccessible(true);
        Map<String, HTTPServerHandler> routes = (Map<String, HTTPServerHandler>) routesField.get(null);
        assertTrue(routes.containsKey("/hello"), "La ruta /hello debe estar registrada.");
        HTTPFrameworkRequest mockRequest = new HTTPFrameworkRequest("/hello?name=Miguel");
        HTTPFrameworkResponse mockResponse = new HTTPFrameworkResponse();
        HTTPFrameworkResponse result = routes.get("/hello").handleRequest(mockRequest, mockResponse);
        assertTrue(result.getBody().contains("Hello Miguel"),
                "La respuesta debe contener el nombre extraído del parámetro de consulta.");
    }
}