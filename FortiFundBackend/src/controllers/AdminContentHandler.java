package controllers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import db.DBManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminContentHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Set CORS headers
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, PUT, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.getResponseHeaders().add("Content-Type", "application/json"); // Ensure JSON content type is sent

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            handleGetAllTextContent(exchange);
        } else if ("PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
            handleUpdateTextContent(exchange);
        } else {
            sendResponse(exchange, 405, "{\"message\": \"Method Not Allowed\"}");
        }
    }

    private void handleGetAllTextContent(HttpExchange exchange) throws IOException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        JSONArray jsonArray = new JSONArray();

        try {
            conn = DBManager.getConnection();
            String sql = "SELECT section_name, content_key, content_value FROM text_content";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                JSONObject jsonItem = new JSONObject();
                jsonItem.put("sectionName", rs.getString("section_name"));
                jsonItem.put("contentKey", rs.getString("content_key"));
                jsonItem.put("contentValue", rs.getString("content_value"));
                jsonArray.put(jsonItem);
            }

            sendResponse(exchange, 200, jsonArray.toString());

        } catch (SQLException e) {
            System.err.println("Database error fetching all text content: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"message\": \"Internal Server Error: Database access failed.\"}");
        } finally {
            DBManager.close(rs, ps, conn);
        }
    }

    private void handleUpdateTextContent(HttpExchange exchange) throws IOException {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            isr.close();

            // Expecting a JSONArray of updates
            JSONArray updatesArray = new JSONArray(sb.toString());

            conn = DBManager.getConnection();
            conn.setAutoCommit(false); // Start transaction

            String sql = "INSERT INTO text_content (section_name, content_key, content_value) VALUES (?, ?, ?) " +
                         "ON DUPLICATE KEY UPDATE content_value = VALUES(content_value)";
            ps = conn.prepareStatement(sql);

            for (int i = 0; i < updatesArray.length(); i++) {
                JSONObject update = updatesArray.getJSONObject(i);
                String sectionName = update.optString("sectionName");
                String contentKey = update.optString("contentKey");
                String contentValue = update.optString("contentValue");

                if (sectionName.isEmpty() || contentKey.isEmpty() || contentValue == null) {
                    // Log warning but continue, or throw specific error if any single item is invalid
                    System.err.println("Warning: Skipping invalid content update - Missing sectionName, contentKey, or contentValue.");
                    continue; // Skip this invalid update
                }

                ps.setString(1, sectionName);
                ps.setString(2, contentKey);
                ps.setString(3, contentValue);
                ps.addBatch(); // Add to batch for efficient execution
            }

            int[] rowsAffected = ps.executeBatch(); // Execute all updates in batch
            conn.commit(); // Commit transaction

            sendResponse(exchange, 200, "{\"message\": \"All text content changes saved successfully.\"}");

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback(); // Rollback on error
            } catch (SQLException rollbackEx) {
                System.err.println("Error during rollback: " + rollbackEx.getMessage());
            }
            System.err.println("Database error updating text content: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"message\": \"Internal Server Error: Database access failed during batch update.\"}");
        } catch (Exception e) {
            System.err.println("Error processing update content request: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 400, "{\"message\": \"Invalid JSON format or request body for batch update.\"}");
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true); // Reset auto-commit
            } catch (SQLException autoCommitEx) {
                System.err.println("Error resetting auto-commit: " + autoCommitEx.getMessage());
            }
            DBManager.close(null, ps, conn);
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
