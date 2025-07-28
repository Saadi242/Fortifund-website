package controllers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import db.DBManager;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class NavbarHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Set CORS headers
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().add("Content-Type", "application/json");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1); // No Content for preflight
            return;
        }

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            handleGetNavbarItems(exchange);
        } else {
            sendResponse(exchange, 405, "{\"message\": \"Method Not Allowed\"}");
        }
    }

    private void handleGetNavbarItems(HttpExchange exchange) throws IOException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        JSONArray jsonArray = new JSONArray();

        try {
            conn = DBManager.getConnection();
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
                // Handle NULL parent_id correctly
                jsonItem.put("parentId", rs.getObject("parent_id") != null ? rs.getInt("parent_id") : JSONObject.NULL);
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
            sendResponse(exchange, 500, "{\"message\": \"Internal Server Error: Database access failed.\"}");
        } finally {
            DBManager.close(rs, ps, conn);
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
