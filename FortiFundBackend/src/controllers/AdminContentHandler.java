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

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            handleGetAllTextContent(exchange);
        } else if ("PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
            handleUpdateTextContent(exchange);
        } else {
            sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    private void handleGetAllTextContent(HttpExchange exchange) throws IOException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        JSONArray jsonArray = new JSONArray();

        try {
            conn = DBManager.getConnection();
            // *** IMPORTANT: Ensure this SQL query is exactly as below, without any WHERE clause ***
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
            sendResponse(exchange, 500, "Internal Server Error: Database access failed.");
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

            JSONObject requestBody = new JSONObject(sb.toString());
            String sectionName = requestBody.optString("sectionName");
            String contentKey = requestBody.optString("contentKey");
            String contentValue = requestBody.optString("contentValue");

            if (sectionName.isEmpty() || contentKey.isEmpty() || contentValue == null) {
                sendResponse(exchange, 400, "Bad Request: Missing sectionName, contentKey, or contentValue.");
                return;
            }

            conn = DBManager.getConnection();
            // Use INSERT ... ON DUPLICATE KEY UPDATE for upsert functionality
            String sql = "INSERT INTO text_content (section_name, content_key, content_value) VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE content_value = VALUES(content_value)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, sectionName);
            ps.setString(2, contentKey);
            ps.setString(3, contentValue);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                sendResponse(exchange, 200, "{\"message\": \"Content updated successfully.\"}");
            } else {
                sendResponse(exchange, 500, "{\"message\": \"Failed to update content.\"}");
            }

        } catch (SQLException e) {
            System.err.println("Database error updating text content: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal Server Error: Database access failed.");
        } catch (Exception e) {
            System.err.println("Error processing update content request: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 400, "Invalid JSON or request body.");
        } finally {
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
