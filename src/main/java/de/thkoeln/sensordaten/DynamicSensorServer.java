package de.thkoeln.sensordaten;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;

import org.eclipse.jetty.server.Server;


public class DynamicSensorServer {
    private static Server server;

    public static void startServer(List<RESTSensor> sensorList) throws Exception {

        System.out.println("Number of sensors to register: " + sensorList.size());

        stopServer();

        ResourceConfig config = new ResourceConfig();

        for (RESTSensor sensor : sensorList) {


            for (SubSensor subSensor : sensor.getSubSensors()) {
                String fullPath = "/" + sensor.getSensorName() + "/" + subSensor.getUnterPfad();


                final DynamicSensorResource sensorResource = new DynamicSensorResource(
                        subSensor.getDatenTyp(),
                        subSensor.getWertIntervalMin(),
                        subSensor.getWertIntervalMax(),
                        subSensor.isZeitStempel()
                );

                Resource.Builder resourceBuilder = Resource.builder();
                String registrationPath = fullPath.substring(1);


                resourceBuilder.path(registrationPath)
                        .addMethod("GET")
                        .produces(MediaType.APPLICATION_JSON)
                        .handledBy(context -> {
                            System.out.println("\n>>> GET request received for: " + fullPath);
                            Object value = sensorResource.getValue();
                            System.out.println("<<< Returning value: " + value);
                            return value;
                        });

                config.registerResources(resourceBuilder.build());

                String fullUrl = sensor.getRestPath();
                if (!fullUrl.endsWith("/")) {
                    fullUrl += "/";
                }
                fullUrl += registrationPath;
                System.out.println("- Registered full URL: " + fullUrl);
            }
        }

        try {
            URI uri = UriBuilder.fromUri(sensorList.get(0).getRestPath()).build();
            System.out.println("\n=== Server Startup ===");
            System.out.println("Starting server at URI: " + uri);

            server = JettyHttpContainerFactory.createServer(uri, config, false);
            server.start();

            System.out.println("Server started successfully!");
            System.out.println("Base URI: " + uri);
            System.out.println("Server state: " + server.getState());
        } catch (Exception e) {
            System.err.println("\n!!! SERVER STARTUP FAILED !!!");
            System.err.println("Error message: " + e.getMessage());
            System.err.println("Stack trace:");
            e.printStackTrace();
            throw e;
        }
    }

    public static void stopServer() {
        System.out.println("\n=== Server Shutdown ===");
        if (server != null && server.isRunning()) {
            try {
                System.out.println("Server state before stopping: " + server.getState());
                server.stop();
                System.out.println("Server stopped successfully");
                System.out.println("Server state after stopping: " + server.getState());
            } catch (Exception e) {
                System.err.println("!!! ERROR STOPPING SERVER !!!");
                System.err.println("Error message: " + e.getMessage());
                System.err.println("Stack trace:");
                e.printStackTrace();
            }
        } else {
            System.out.println("No running server instance found");
        }
    }
}