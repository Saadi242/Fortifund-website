package controllers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;
import db.DBManager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.nio.charset.StandardCharsets;

public class ContentHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Set CORS headers
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.getResponseHeaders().add("Content-Type", "application/json"); // Ensure JSON content type is sent

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1); // No Content
            return;
        }

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            handleGetContent(exchange);
        } else {
            sendResponse(exchange, 405, "{\"message\": \"Method Not Allowed\"}");
        }
    }

    private void handleGetContent(HttpExchange exchange) throws IOException {
        URI requestURI = exchange.getRequestURI();
        String path = requestURI.getPath();
        String query = requestURI.getQuery();

        Map<String, String> queryParams = parseQueryParams(query);
        String sectionName = queryParams.get("section_name");

        if (sectionName == null || sectionName.isEmpty()) {
            sendResponse(exchange, 400, "{\"message\": \"Bad Request: Missing or empty section_name query parameter.\"}");
            return;
        }

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        JSONObject responseJson = new JSONObject();

        try {
            conn = DBManager.getConnection();

            // Handle /api/content/text-content
            if (path.equals("/api/content/text-content")) {
                String sql = "SELECT content_key, content_value FROM text_content WHERE section_name = ?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, sectionName);
                rs = ps.executeQuery();

                JSONObject textContent = new JSONObject();
                while (rs.next()) {
                    textContent.put(rs.getString("content_key"), rs.getString("content_value"));
                }
                responseJson.put("textContent", textContent);

            }
            // Handle /api/content/image-assets
            else if (path.equals("/api/content/image-assets")) {
                String sql = "SELECT asset_key, image_url, alt_text FROM image_assets WHERE section_name = ?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, sectionName);
                rs = ps.executeQuery();

                JSONObject imageAssets = new JSONObject();
                while (rs.next()) {
                    JSONObject assetDetails = new JSONObject();
                    assetDetails.put("url", rs.getString("image_url"));
                    assetDetails.put("altText", rs.getString("alt_text"));
                    imageAssets.put(rs.getString("asset_key"), assetDetails);
                }
                responseJson.put("imageAssets", imageAssets);
            }
            // Handle /api/content/video-assets
            else if (path.equals("/api/content/video-assets")) {
                String sql = "SELECT asset_key, video_url, poster_url FROM video_assets WHERE section_name = ?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, sectionName);
                rs = ps.executeQuery();

                JSONObject videoAssets = new JSONObject();
                while (rs.next()) {
                    JSONObject assetDetails = new JSONObject();
                    assetDetails.put("url", rs.getString("video_url"));
                    assetDetails.put("posterUrl", rs.getString("poster_url"));
                    videoAssets.put(rs.getString("asset_key"), assetDetails);
                }
                responseJson.put("videoAssets", videoAssets);
            }
            else {
                sendResponse(exchange, 404, "{\"message\": \"Not Found: Invalid content API path.\"}");
                return;
            }

            sendResponse(exchange, 200, responseJson.toString());

        } catch (SQLException e) {
            System.err.println("Database error fetching content for section " + sectionName + " at path " + path + ": " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"message\": \"Internal Server Error: Database access failed for " + sectionName + ".\"}");
        } catch (Exception e) {
            System.err.println("Error processing content request for section " + sectionName + " at path " + path + ": " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"message\": \"Internal Server Error: " + e.getMessage() + "\"}");
        } finally {
            DBManager.close(rs, ps, conn);
        }
    }

    // Helper method to parse query parameters
    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> queryParams = new HashMap<>();
        if (query != null && !query.isEmpty()) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx > 0) {
                    String key = pair.substring(0, idx);
                    String value = pair.substring(idx + 1);
                    queryParams.put(key, value);
                }
            }
        }
        return queryParams;
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}
