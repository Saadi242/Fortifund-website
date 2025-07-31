package controllers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import db.DBManager; // Corrected: Use DBManager for database interaction
import org.json.JSONObject; // Corrected: Use JSONObject for request body parsing
import utils.EmailService; // Import the EmailService for sending emails

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp; // For java.sql.Timestamp
import java.time.LocalDateTime; // For modern date/time API
import java.time.format.DateTimeParseException; // For parsing exceptions

/**
 * FormHandler class implements HttpHandler to process form submissions from the public website.
 * It handles contact messages and demo requests, including parsing date/time for demos
 * and sending confirmation emails.
 */
public class FormHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Set CORS headers for public forms
        // Assuming the frontend is running on http://localhost:63342
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "http://localhost:63342");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().add("Content-Type", "application/json");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            // Handle preflight requests for CORS
            exchange.sendResponseHeaders(204, -1); // No content for OPTIONS
            return;
        }

        String path = exchange.getRequestURI().getPath();
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            if (path.endsWith("/submit/contact")) {
                handleSubmitContactForm(exchange);
            } else if (path.endsWith("/submit/demo")) {
                handleSubmitDemoForm(exchange);
            } else {
                sendResponse(exchange, 404, "{\"message\": \"Not Found\"}");
            }
        } else {
            sendResponse(exchange, 405, "{\"message\": \"Method Not Allowed\"}");
        }
    }

    /**
     * Handles submission of the contact form.
     * Extracts name, email, and message, then saves them to the database.
     *
     * @param exchange The HttpExchange object.
     * @throws IOException If an I/O error occurs.
     */
    private void handleSubmitContactForm(HttpExchange exchange) throws IOException {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            JSONObject requestBody = parseRequestBody(exchange);
            String name = requestBody.optString("fullName", "").trim(); // Assuming fullName from frontend
            String email = requestBody.optString("email", "").trim();
            String message = requestBody.optString("comments", "").trim(); // Assuming comments for message

            if (name.isEmpty() || email.isEmpty() || message.isEmpty()) {
                sendResponse(exchange, 400, "{\"message\": \"Bad Request: Name, Email, and Message are required.\"}");
                return;
            }

            conn = DBManager.getConnection(); // Corrected: Use DBManager
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
            DBManager.close(null, ps, conn); // Corrected: Use DBManager
        }
    }

    /**
     * Handles submission of the demo request form.
     * Extracts details including formatted date/time, saves to database,
     * and sends a confirmation email.
     *
     * @param exchange The HttpExchange object.
     * @throws IOException If an I/O error occurs.
     */
    private void handleSubmitDemoForm(HttpExchange exchange) throws IOException {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            JSONObject requestBody = parseRequestBody(exchange);
            String fullName = requestBody.optString("fullName", "").trim();
            String phoneNumber = requestBody.optString("phoneNumber", "").trim();
            String company = requestBody.optString("company", "").trim();
            String email = requestBody.optString("email", "").trim();
            String comments = requestBody.optString("comments", "").trim();
            // The frontend now sends demoDateTime as a single string
            String demoDateTimeString = requestBody.optString("demoDateTime", null);

            // Basic validation
            if (fullName.isEmpty() || company.isEmpty() || email.isEmpty() || demoDateTimeString == null || demoDateTimeString.isEmpty()) {
                sendResponse(exchange, 400, "{\"message\": \"Bad Request: Full Name, Company, Email, and Demo Date/Time are required.\"}");
                return;
            }

            LocalDateTime demoLocalDateTime = null;
            Timestamp sqlTimestamp = null;

            try {
                // Parse the combined date-time string from the frontend
                // Expected format: YYYY-MM-DDTHH:MM:SS.SSS (e.g., "2025-07-31T12:00:00.000")
                // LocalDateTime.parse can handle this ISO_LOCAL_DATE_TIME format by default.
                demoLocalDateTime = LocalDateTime.parse(demoDateTimeString);
                // Convert LocalDateTime to java.sql.Timestamp for database storage
                sqlTimestamp = Timestamp.valueOf(demoLocalDateTime);
                System.out.println("Backend: Successfully parsed demoDateTime as LocalDateTime: " + demoLocalDateTime);
                System.out.println("Backend: Converted to java.sql.Timestamp: " + sqlTimestamp);

            } catch (DateTimeParseException e) {
                System.err.println("Backend: Date/Time parsing error in demo form for string: '" + demoDateTimeString + "' - " + e.getMessage());
                e.printStackTrace();
                sendResponse(exchange, 400, "{\"message\": \"Bad Request: Invalid date or time format. Expected YYYY-MM-DDTHH:MM:SS.SSS\"}");
                return;
            }

            conn = DBManager.getConnection(); // Corrected: Use DBManager
            String sql = "INSERT INTO demo_requests (full_name, phone_number, company, email, comments, submission_time) VALUES (?, ?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, fullName);
            ps.setString(2, phoneNumber.isEmpty() ? null : phoneNumber); // Store null if empty
            ps.setString(3, company);
            ps.setString(4, email);
            ps.setString(5, comments.isEmpty() ? null : comments); // Store null if empty
            ps.setTimestamp(6, sqlTimestamp); // Use the parsed Timestamp

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                // Send confirmation email after successful database insertion
                // Pass the original string values for date/time for the email content
                EmailService.sendDemoConfirmationEmail(email, fullName, phoneNumber, company, comments,
                        demoLocalDateTime.toLocalDate().toString(), // YYYY-MM-DD
                        demoLocalDateTime.toLocalTime().toString()); // HH:MM:SS (or HH:MM if seconds are 00)

                sendResponse(exchange, 200, "{\"message\": \"Demo request submitted successfully and confirmation email sent!\"}");
            } else {
                sendResponse(exchange, 500, "{\"message\": \"Failed to submit demo request.\"}");
            }

        } catch (SQLException e) {
            System.err.println("Database error submitting demo form: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"message\": \"Internal Server Error: Database access failed.\"}");
        } catch (Exception e) { // Catch any other unexpected errors
            System.err.println("Error processing demo form request: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"message\": \"Internal Server Error: An unexpected error occurred.\"}");
        } finally {
            DBManager.close(null, ps, conn); // Corrected: Use DBManager
        }
    }

    /**
     * Parses the JSON request body from the HttpExchange.
     *
     * @param exchange The HttpExchange object.
     * @return A JSONObject parsed from the request body.
     * @throws IOException If an I/O error occurs.
     */
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

    /**
     * Sends an HTTP response with the given status code and message.
     *
     * @param exchange The HttpExchange object.
     * @param statusCode The HTTP status code to send.
     * @param response The response message (JSON string).
     * @throws IOException If an I/O error occurs.
     */
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}
