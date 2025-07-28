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
import java.util.HashMap;
import java.util.Map;

public class AdminNavbarHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Set CORS headers
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, PUT, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.getResponseHeaders().add("Content-Type", "application/json");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1); // No Content for preflight
            return;
        }

        switch (exchange.getRequestMethod()) {
            case "GET":
                handleGetNavbarItems(exchange);
                break;
            case "PUT":
                handleUpdateNavbarItem(exchange);
                break;
            default:
                sendResponse(exchange, 405, "{\"message\": \"Method Not Allowed\"}");
                break;
        }
    }

    private void handleGetNavbarItems(HttpExchange exchange) throws IOException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        JSONArray jsonArray = new JSONArray();

        try {
            conn = DBManager.getConnection();
            // Fetch all navbar items, ordered by display_order
            String sql = "SELECT id, item_text, item_href, is_dropdown, parent_id, display_order FROM navbar_items ORDER BY display_order ASC";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            // Store items in a map for easy lookup by ID
            Map<Integer, JSONObject> itemMap = new HashMap<>();
            while (rs.next()) {
                JSONObject jsonItem = new JSONObject();
                jsonItem.put("id", rs.getInt("id"));
                jsonItem.put("itemText", rs.getString("item_text"));
                jsonItem.put("itemHref", rs.getString("item_href"));
                jsonItem.put("isDropdown", rs.getBoolean("is_dropdown"));
                jsonItem.put("parentId", rs.getObject("parent_id") != null ? rs.getInt("parent_id") : JSONObject.NULL); // Handle NULL parent_id
                jsonItem.put("displayOrder", rs.getInt("display_order"));
                jsonItem.put("children", new JSONArray()); // Initialize children array for dropdowns
                itemMap.put(rs.getInt("id"), jsonItem);
            }

            // Build the hierarchical structure
            for (JSONObject item : itemMap.values()) {
                if (item.get("parentId") != JSONObject.NULL) {
                    JSONObject parent = itemMap.get(item.getInt("parentId"));
                    if (parent != null) {
                        parent.getJSONArray("children").put(item);
                    }
                } else {
                    jsonArray.put(item); // Add top-level items to the main array
                }
            }

            sendResponse(exchange, 200, jsonArray.toString());

        } catch (SQLException e) {
            System.err.println("Database error fetching navbar items: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"message\": \"Internal Server Error: Database access failed for navbar items.\"}");
        } finally {
            DBManager.close(rs, ps, conn);
        }
    }

    private void handleUpdateNavbarItem(HttpExchange exchange) throws IOException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            JSONObject requestBody = parseRequestBody(exchange);
            int id = requestBody.optInt("id");
            String itemText = requestBody.optString("itemText");
            String itemHref = requestBody.optString("itemHref");
            boolean isDropdown = requestBody.optBoolean("isDropdown");
            int displayOrder = requestBody.optInt("displayOrder", 0);

            if (id == 0 || itemText.isEmpty() || itemHref.isEmpty() || displayOrder == 0) {
                sendResponse(exchange, 400, "{\"message\": \"Missing required fields: id, itemText, itemHref, or displayOrder.\"}");
                return;
            }

            conn = DBManager.getConnection();
            String sql = "UPDATE navbar_items SET item_text = ?, item_href = ?, is_dropdown = ?, display_order = ? WHERE id = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, itemText);
            ps.setString(2, itemHref);
            ps.setBoolean(3, isDropdown);
            ps.setInt(4, displayOrder);
            ps.setInt(5, id);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                sendResponse(exchange, 200, "{\"message\": \"Navbar item updated successfully.\"}");
            } else {
                sendResponse(exchange, 404, "{\"message\": \"Navbar item not found or no changes made.\"}");
            }

        } catch (SQLException e) {
            System.err.println("Database error updating navbar item: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"message\": \"Internal Server Error: Database access failed.\"}");
        } catch (Exception e) {
            System.err.println("Error processing update navbar item request: " + e.getMessage());
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
