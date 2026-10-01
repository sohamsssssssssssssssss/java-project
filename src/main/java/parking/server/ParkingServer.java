package parking.server;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class ParkingServer {

    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServer.create(
                new InetSocketAddress(8080), 0
        );

        server.createContext("/api/test", exchange -> {

            String response =
                    "{\"message\":\"Smart Parking backend is working\"}";

            exchange.getResponseHeaders().add(
                    "Content-Type",
                    "application/json"
            );

            exchange.getResponseHeaders().add(
                    "Access-Control-Allow-Origin",
                    "*"
            );

            byte[] data =
                    response.getBytes(StandardCharsets.UTF_8);

            exchange.sendResponseHeaders(200, data.length);

            try (OutputStream output =
                         exchange.getResponseBody()) {

                output.write(data);
            }
        });

        server.start();

        System.out.println("==============================");
        System.out.println("   SMART PARKING SERVER");
        System.out.println("==============================");
        System.out.println();
        System.out.println("Server running on port 8080");
        System.out.println("API ready: /api/test");
        System.out.println();
        System.out.println("Press Ctrl+C to stop.");
    }
}
