package controllers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class AuthHandler implements HttpHandler {

    // Hardcoded admin credentials for demonstration purposes
    // In a real application, these would be stored securely in a database
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "12345"; // Use strong, hashed passwords in production!

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Set CORS headers to allow requests from your frontend
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            // Handle preflight requests for CORS
            exchange.sendResponseHeaders(204, -1); // No Content
            return;
        }

        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            handleLogin(exchange);
        } else {
            sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        try {
            // Read the request body (JSON payload)
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            isr.close();

            JSONObject requestBody = new JSONObject(sb.toString());
            String username = requestBody.optString("username");
            String password = requestBody.optString("password");

            // Simple authentication check
            if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
                JSONObject responseJson = new JSONObject();
                responseJson.put("success", true);
                responseJson.put("message", "Login successful");
                // In a real app, you'd generate and return a JWT token here
                sendResponse(exchange, 200, responseJson.toString());
            } else {
                JSONObject responseJson = new JSONObject();
                responseJson.put("success", false);
                responseJson.put("message", "Invalid username or password");
                sendResponse(exchange, 401, responseJson.toString()); // Unauthorized
            }

        } catch (Exception e) {
            System.err.println("Error during admin login: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}
