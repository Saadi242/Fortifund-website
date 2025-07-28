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

public class AssetHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Set CORS headers
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.getResponseHeaders().add("Content-Type", "application/json");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1); // No Content for preflight
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String assetType = null; // "image" or "video"

        if (path.startsWith("/api/admin/assets/image")) {
            assetType = "image";
        } else if (path.startsWith("/api/admin/assets/video")) {
            assetType = "video";
        } else {
            sendResponse(exchange, 404, "{\"message\": \"Asset type not found.\"}");
            return;
        }

        switch (exchange.getRequestMethod()) {
            case "GET":
                handleGetAssets(exchange, assetType);
                break;
            case "POST":
                handleAddAsset(exchange, assetType);
                break;
            case "PUT":
                handleUpdateAsset(exchange, assetType);
                break;
            case "DELETE":
                handleDeleteAsset(exchange, assetType);
                break;
            default:
                sendResponse(exchange, 405, "{\"message\": \"Method Not Allowed\"}");
                break;
        }
    }

    private void handleGetAssets(HttpExchange exchange, String assetType) throws IOException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        JSONArray jsonArray = new JSONArray();
        String tableName = assetType.equals("image") ? "image_assets" : "video_assets";

        // --- MODIFIED SQL QUERY CONSTRUCTION ---
        String sql;
        if (assetType.equals("image")) {
            sql = "SELECT id, section_name, asset_key, image_url AS url, alt_text FROM " + tableName;
        } else { // video
            sql = "SELECT id, section_name, asset_key, video_url AS url, poster_url FROM " + tableName;
        }
        // --- END MODIFIED SQL QUERY CONSTRUCTION ---

        try {
            conn = DBManager.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                JSONObject jsonItem = new JSONObject();
                jsonItem.put("id", rs.getInt("id"));
                jsonItem.put("sectionName", rs.getString("section_name"));
                jsonItem.put("assetKey", rs.getString("asset_key"));
                jsonItem.put("url", rs.getString("url")); // 'url' is the alias from the SELECT statement
                if (assetType.equals("image")) {
                    jsonItem.put("altText", rs.getString("alt_text"));
                } else { // video
                    jsonItem.put("posterUrl", rs.getString("poster_url"));
                }
                jsonArray.put(jsonItem);
            }
            sendResponse(exchange, 200, jsonArray.toString());

        } catch (SQLException e) {
            System.err.println("Database error fetching " + assetType + " assets: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"message\": \"Internal Server Error: Database access failed for " + assetType + " assets.\"}");
        } finally {
            DBManager.close(rs, ps, conn);
        }
    }

    private void handleAddAsset(HttpExchange exchange, String assetType) throws IOException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            JSONObject requestBody = parseRequestBody(exchange);
            String sectionName = requestBody.optString("sectionName");
            String assetKey = requestBody.optString("assetKey");
            String url = requestBody.optString("url");
            String altText = requestBody.optString("altText");
            String posterUrl = requestBody.optString("posterUrl"); // Only for video

            if (sectionName.isEmpty() || assetKey.isEmpty() || url.isEmpty()) {
                sendResponse(exchange, 400, "{\"message\": \"Missing required fields: sectionName, assetKey, or url.\"}");
                return;
            }

            conn = DBManager.getConnection();
            String sql;
            if (assetType.equals("image")) {
                sql = "INSERT INTO image_assets (section_name, asset_key, image_url, alt_text) VALUES (?, ?, ?, ?)";
                ps = conn.prepareStatement(sql);
                ps.setString(1, sectionName);
                ps.setString(2, assetKey);
                ps.setString(3, url);
                ps.setString(4, altText);
            } else { // video
                sql = "INSERT INTO video_assets (section_name, asset_key, video_url, poster_url) VALUES (?, ?, ?, ?)";
                ps = conn.prepareStatement(sql);
                ps.setString(1, sectionName);
                ps.setString(2, assetKey);
                ps.setString(3, url);
                ps.setString(4, posterUrl);
            }

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                sendResponse(exchange, 201, "{\"message\": \"" + assetType + " asset added successfully.\"}");
            } else {
                sendResponse(exchange, 500, "{\"message\": \"Failed to add " + assetType + " asset.\"}");
            }

        } catch (SQLException e) {
            System.err.println("Database error adding " + assetType + " asset: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"message\": \"Internal Server Error: Database access failed.\"}");
        } catch (Exception e) {
            System.err.println("Error processing add " + assetType + " asset request: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 400, "{\"message\": \"Invalid JSON or request body.\"}");
        } finally {
            DBManager.close(null, ps, conn);
        }
    }

    private void handleUpdateAsset(HttpExchange exchange, String assetType) throws IOException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            JSONObject requestBody = parseRequestBody(exchange);
            int id = requestBody.optInt("id");
            String sectionName = requestBody.optString("sectionName");
            String assetKey = requestBody.optString("assetKey");
            String url = requestBody.optString("url");
            String altText = requestBody.optString("altText");
            String posterUrl = requestBody.optString("posterUrl"); // Only for video

            if (id == 0 || sectionName.isEmpty() || assetKey.isEmpty() || url.isEmpty()) {
                sendResponse(exchange, 400, "{\"message\": \"Missing required fields: id, sectionName, assetKey, or url.\"}");
                return;
            }

            conn = DBManager.getConnection();
            String sql;
            if (assetType.equals("image")) {
                sql = "UPDATE image_assets SET section_name = ?, asset_key = ?, image_url = ?, alt_text = ? WHERE id = ?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, sectionName);
                ps.setString(2, assetKey);
                ps.setString(3, url);
                ps.setString(4, altText);
                ps.setInt(5, id);
            } else { // video
                sql = "UPDATE video_assets SET section_name = ?, asset_key = ?, video_url = ?, poster_url = ? WHERE id = ?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, sectionName);
                ps.setString(2, assetKey);
                ps.setString(3, url);
                ps.setString(4, posterUrl);
                ps.setInt(5, id);
            }

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                sendResponse(exchange, 200, "{\"message\": \"" + assetType + " asset updated successfully.\"}");
            } else {
                sendResponse(exchange, 404, "{\"message\": \"" + assetType + " asset not found or no changes made.\"}");
            }

        } catch (SQLException e) {
            System.err.println("Database error updating " + assetType + " asset: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"message\": \"Internal Server Error: Database access failed.\"}");
        } catch (Exception e) {
            System.err.println("Error processing update " + assetType + " asset request: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 400, "{\"message\": \"Invalid JSON or request body.\"}");
        } finally {
            DBManager.close(null, ps, conn);
        }
    }

    private void handleDeleteAsset(HttpExchange exchange, String assetType) throws IOException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            JSONObject requestBody = parseRequestBody(exchange);
            int id = requestBody.optInt("id");

            if (id == 0) {
                sendResponse(exchange, 400, "{\"message\": \"Missing required field: id.\"}");
                return;
            }

            conn = DBManager.getConnection();
            String sql = "DELETE FROM " + (assetType.equals("image") ? "image_assets" : "video_assets") + " WHERE id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                sendResponse(exchange, 200, "{\"message\": \"" + assetType + " asset deleted successfully.\"}");
            } else {
                sendResponse(exchange, 404, "{\"message\": \"" + assetType + " asset not found.\"}");
            }

        } catch (SQLException e) {
            System.err.println("Database error deleting " + assetType + " asset: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"message\": \"Internal Server Error: Database access failed.\"}");
        } catch (Exception e) {
            System.err.println("Error processing delete " + assetType + " asset request: " + e.getMessage());
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
