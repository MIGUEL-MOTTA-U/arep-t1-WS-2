package escuelaing.edu.co.framework.services.implementations;

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

    private HTTPServerImpl httpServer;
    private Path tmpResources;

    @Mock
    private HTTPServerHandler mockHandler;

    @BeforeEach
    void setUp() throws Exception {
        Field instanceField = HTTPServerImpl.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);

        httpServer = HTTPServerImpl.getInstance();

        tmpResources = Files.createTempDirectory("httpserver_test_" + UUID.randomUUID());
        Field resPathField = HTTPServerImpl.class.getDeclaredField("RESOURCES_PATH");
        resPathField.setAccessible(true);
        resPathField.set(httpServer, tmpResources.toAbsolutePath().toString());
    }

    @AfterEach
    void tearDown() throws Exception {
        if (tmpResources != null && Files.exists(tmpResources)) {
            Files.walk(tmpResources)
                    .map(Path::toFile)
                    .sorted((a, b) -> -a.compareTo(b))
                    .forEach(File::delete);
        }
        Field instanceField = HTTPServerImpl.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    @Test
    void getInstance_returnsSameSingleton() {
        HTTPServerImpl a = HTTPServerImpl.getInstance();
        HTTPServerImpl b = HTTPServerImpl.getInstance();
        assertSame(a, b, "getInstance() debe devolver la misma instancia (singleton).");
    }

    @Test
    void get_registersRoute() throws Exception {
        httpServer.get("/miRuta", mockHandler);

        Field routesField = HTTPServerImpl.class.getDeclaredField("routes");
        routesField.setAccessible(true);
        Map<String, HTTPServerHandler> routes = (Map<String, HTTPServerHandler>) routesField.get(httpServer);

        assertTrue(routes.containsKey("/miRuta"), "La ruta registrada debe estar presente.");
        assertSame(mockHandler, routes.get("/miRuta"), "El handler almacenado debe ser el mismo objeto pasado.");
    }

    @Test
    void staticFiles_createsDirectoryAndUpdatesPath() throws Exception {
        String newDir = "/static_test_dir";
        httpServer.staticFiles(newDir);

        Field resPathField = HTTPServerImpl.class.getDeclaredField("RESOURCES_PATH");
        resPathField.setAccessible(true);
        String newResPath = (String) resPathField.get(httpServer);

        File dir = new File(newResPath);
        assertTrue(dir.exists() && dir.isDirectory(), "El directorio pasado a staticFiles debe haberse creado.");
        assertTrue(newResPath.endsWith(newDir), "RESOURCES_PATH debe actualizarse con el sufijo indicado.");
    }

    @Test
    void post_put_delete_doNotRegisterRoutes_inCurrentImpl() throws Exception {
        httpServer.post("/p", mockHandler);
        httpServer.put("/u", mockHandler);
        httpServer.delete("/d", mockHandler);

        Field routesField = HTTPServerImpl.class.getDeclaredField("routes");
        routesField.setAccessible(true);
        Map<String, HTTPServerHandler> routes = (Map<String, HTTPServerHandler>) routesField.get(httpServer);

        assertFalse(routes.containsKey("/p"), "POST no debería registrar rutas en la implementación actual.");
        assertFalse(routes.containsKey("/u"), "PUT no debería registrar rutas en la implementación actual.");
        assertFalse(routes.containsKey("/d"), "DELETE no debería registrar rutas en la implementación actual.");
    }

    @Test
    void stop_setsRunningFalse() throws Exception {
        // Forzar running = true
        Field runningField = HTTPServerImpl.class.getDeclaredField("running");
        runningField.setAccessible(true);
        runningField.set(httpServer, true);

        httpServer.stop();

        boolean runningAfter = (boolean) runningField.get(httpServer);
        assertFalse(runningAfter, "Después de stop(), el flag running debe ser false.");
    }
}