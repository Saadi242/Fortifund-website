package controllers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;
import db.DBManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class FormHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Set CORS headers
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().add("Content-Type", "application/json"); // Ensure JSON content type is sent

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1); // No Content for preflight
            return;
        }

        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/api/submit/contact")) {
                handleContactSubmission(exchange);
            } else if (path.equals("/api/submit/demo")) {
                handleDemoSubmission(exchange);
            } else {
                sendResponse(exchange, 404, "{\"message\": \"Endpoint not found.\"}");
            }
        } else {
            sendResponse(exchange, 405, "{\"message\": \"Method Not Allowed\"}");
        }
    }

    private void handleContactSubmission(HttpExchange exchange) throws IOException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            JSONObject requestBody = parseRequestBody(exchange);
            String name = requestBody.optString("name");
            String email = requestBody.optString("email");
            String message = requestBody.optString("message");

            if (name.isEmpty() || email.isEmpty() || message.isEmpty()) {
                sendResponse(exchange, 400, "{\"message\": \"All fields are required for contact form.\"}");
                return;
            }

            conn = DBManager.getConnection();
            String sql = "INSERT INTO contact_messages (name, email, message) VALUES (?, ?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, message);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                sendResponse(exchange, 200, "{\"message\": \"Message sent successfully!\"}");
            } else {
                sendResponse(exchange, 500, "{\"message\": \"Failed to send message.\"}");
            }

        } catch (SQLException e) {
            System.err.println("Database error submitting contact form: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"message\": \"Internal Server Error: Database access failed.\"}");
        } catch (Exception e) {
            System.err.println("Error processing contact form request: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 400, "{\"message\": \"Invalid JSON or request body.\"}");
        } finally {
            DBManager.close(null, ps, conn);
        }
    }

    private void handleDemoSubmission(HttpExchange exchange) throws IOException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            JSONObject requestBody = parseRequestBody(exchange);
            String fullName = requestBody.optString("fullName");
            String phoneNumber = requestBody.optString("phoneNumber");
            String company = requestBody.optString("company");
            String email = requestBody.optString("email");
            String comments = requestBody.optString("comments");
            String demoDateStr = requestBody.optString("demoDate");
            String demoTimeStr = requestBody.optString("demoTime");

            // Basic validation for required fields
            if (fullName.isEmpty() || company.isEmpty() || email.isEmpty()) {
                sendResponse(exchange, 400, "{\"message\": \"Full Name, Company, and Email are required for demo request.\"}");
                return;
            }

            // Parse date and time, handle optional fields gracefully
            LocalDate demoDate = null;
            if (!demoDateStr.isEmpty()) {
                try {
                    demoDate = LocalDate.parse(demoDateStr);
                } catch (DateTimeParseException e) {
                    sendResponse(exchange, 400, "{\"message\": \"Invalid demo date format. Please use YYYY-MM-DD.\"}");
                    return;
                }
            }

            LocalTime demoTime = null;
            if (!demoTimeStr.isEmpty()) {
                try {
                    demoTime = LocalTime.parse(demoTimeStr);
                } catch (DateTimeParseException e) {
                    sendResponse(exchange, 400, "{\"message\": \"Invalid demo time format. Please use HH:MM.\"}");
                    return;
                }
            }

            conn = DBManager.getConnection();
            String sql = "INSERT INTO demo_requests (full_name, phone_number, company, email, comments, demo_date, demo_time) VALUES (?, ?, ?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, fullName);
            ps.setString(2, phoneNumber.isEmpty() ? null : phoneNumber); // Store null if empty
            ps.setString(3, company);
            ps.setString(4, email);
            ps.setString(5, comments.isEmpty() ? null : comments); // Store null if empty
            ps.setObject(6, demoDate); // Use setObject for LocalDate
            ps.setObject(7, demoTime); // Use setObject for LocalTime

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                sendResponse(exchange, 200, "{\"message\": \"Demo request scheduled successfully!\"}");
            } else {
                sendResponse(exchange, 500, "{\"message\": \"Failed to schedule demo request.\"}");
            }

        } catch (SQLException e) {
            System.err.println("Database error submitting demo request: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"message\": \"Internal Server Error: Database access failed.\"}");
        } catch (Exception e) {
            System.err.println("Error processing demo request: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 400, "{\"message\": \"Invalid JSON or request body.\"}");
        } finally {
            DBManager.close(null, ps, conn);
        }
    }

    // Helper to parse request body JSON
    private JSONObject parseRequestBody(HttpExchange exchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        isr.close();
        return new JSONObject(sb.toString());
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}
