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

public class AdminNavbarHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Set CORS headers
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, PUT, DELETE, OPTIONS"); // Added DELETE
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.getResponseHeaders().add("Content-Type", "application/json");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            handleGetNavbarItems(exchange);
        } else if ("PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
            handleUpdateNavbarItems(exchange); // Changed to handle multiple items
        } else if ("DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
            handleDeleteNavbarItem(exchange);
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

            // Using a Map to build the hierarchy
            java.util.Map<Integer, JSONObject> itemMap = new java.util.HashMap<>();

            while (rs.next()) {
                JSONObject jsonItem = new JSONObject();
                int id = rs.getInt("id");
                jsonItem.put("id", id);
                jsonItem.put("itemText", rs.getString("item_text"));
                jsonItem.put("itemHref", rs.getString("item_href"));
                jsonItem.put("isDropdown", rs.getBoolean("is_dropdown"));
                // Handle NULL parent_id explicitly for JSON
                jsonItem.put("parentId", rs.getObject("parent_id") != null ? rs.getInt("parent_id") : JSONObject.NULL);
                jsonItem.put("displayOrder", rs.getInt("display_order"));
                jsonItem.put("children", new JSONArray()); // Initialize children array for dropdowns

                itemMap.put(id, jsonItem);
            }

            // Build the hierarchy
            for (JSONObject item : itemMap.values()) {
                if (item.get("parentId") != JSONObject.NULL) {
                    JSONObject parent = itemMap.get(item.getInt("parentId"));
                    if (parent != null) {
                        parent.getJSONArray("children").put(item);
                    }
                }
            }

            // Add only root items to the final array
            for (JSONObject item : itemMap.values()) {
                if (item.get("parentId") == JSONObject.NULL) {
                    jsonArray.put(item);
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

    // Renamed from handleUpdateNavbarItem to handleUpdateNavbarItems (plural)
    private void handleUpdateNavbarItems(HttpExchange exchange) throws IOException {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            // Parse the request body as a JSONArray
            JSONArray updatesArray = parseRequestBodyAsJSONArray(exchange);

            conn = DBManager.getConnection();
            conn.setAutoCommit(false); // Start transaction

            String sql = "INSERT INTO navbar_items (id, item_text, item_href, is_dropdown, parent_id, display_order) " +
                         "VALUES (?, ?, ?, ?, ?, ?) " +
                         "ON DUPLICATE KEY UPDATE item_text = VALUES(item_text), item_href = VALUES(item_href), " +
                         "is_dropdown = VALUES(is_dropdown), parent_id = VALUES(parent_id), display_order = VALUES(display_order)";
            ps = conn.prepareStatement(sql);

            for (int i = 0; i < updatesArray.length(); i++) {
                JSONObject item = updatesArray.getJSONObject(i);
                // Ensure ID exists for updates, or handle new items (though this admin UI only edits existing)
                if (!item.has("id") || item.getInt("id") <= 0) {
                    System.err.println("Warning: Skipping navbar item update due to missing or invalid ID: " + item.toString());
                    continue;
                }

                ps.setInt(1, item.getInt("id"));
                ps.setString(2, item.getString("itemText"));
                ps.setString(3, item.getString("itemHref"));
                ps.setBoolean(4, item.getBoolean("isDropdown"));
                
                // Handle parentId which can be null
                if (item.has("parentId") && !item.get("parentId").equals(JSONObject.NULL)) {
                    ps.setInt(5, item.getInt("parentId"));
                } else {
                    ps.setNull(5, java.sql.Types.INTEGER);
                }
                
                ps.setInt(6, item.getInt("displayOrder"));
                ps.addBatch();
            }

            int[] rowsAffected = ps.executeBatch();
            conn.commit();

            sendResponse(exchange, 200, "{\"message\": \"Navbar items updated successfully.\"}");

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error during rollback: " + rollbackEx.getMessage());
            }
            System.err.println("Database error updating navbar items: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"message\": \"Internal Server Error: Database access failed during batch update.\"}");
        } catch (Exception e) {
            System.err.println("Error processing update navbar items request: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 400, "{\"message\": \"Invalid JSON format or request body for batch update.\"}");
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException autoCommitEx) {
                System.err.println("Error resetting auto-commit: " + autoCommitEx.getMessage());
            }
            DBManager.close(null, ps, conn);
        }
    }

    private void handleDeleteNavbarItem(HttpExchange exchange) throws IOException {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            JSONObject requestBody = parseRequestBodyAsJSONObject(exchange); // Expect single ID for delete
            int id = requestBody.optInt("id", -1);

            if (id == -1) {
                sendResponse(exchange, 400, "{\"message\": \"Bad Request: Missing or invalid ID for deletion.\"}");
                return;
            }

            conn = DBManager.getConnection();
            // Delete the item and any children (due to ON DELETE CASCADE on parent_id)
            String sql = "DELETE FROM navbar_items WHERE id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                sendResponse(exchange, 200, "{\"message\": \"Navbar item and its children deleted successfully.\"}");
            } else {
                sendResponse(exchange, 404, "{\"message\": \"Navbar item not found.\"}");
            }

        } catch (SQLException e) {
            System.err.println("Database error deleting navbar item: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"message\": \"Internal Server Error: Database access failed.\"}");
        } catch (Exception e) {
            System.err.println("Error processing delete navbar item request: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 400, "{\"message\": \"Invalid JSON or request body for deletion.\"}");
        } finally {
            DBManager.close(null, ps, conn);
        }
    }

    // Helper method to parse request body as a JSONObject
    private JSONObject parseRequestBodyAsJSONObject(HttpExchange exchange) throws IOException {
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

    // Helper method to parse request body as a JSONArray
    private JSONArray parseRequestBodyAsJSONArray(HttpExchange exchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        isr.close();
        return new JSONArray(sb.toString());
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}
