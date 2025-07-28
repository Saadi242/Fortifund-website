package controllers;// controllers.ContentUpdateHandler.java
// Handles PUT requests for updating various content types.
// These endpoints are for administrative purposes to update content in the DB.

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import db.DBManager;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import controllers.*; // <--- This imports all classes from the 'controllers' package

public class ContentUpdateHandler {

    // Base handler for common update functionality
    private static abstract class BaseUpdateHandler implements HttpHandler {
        protected void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
            exchange.sendResponseHeaders(statusCode, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        protected String getRequestBody(HttpExchange exchange) throws IOException {
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            isr.close();
            return sb.toString();
        }
    }

    public static class TextContentUpdateHandler extends BaseUpdateHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "PUT, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleUpdateTextContent(exchange);
            } else {
                sendResponse(exchange, 405, "Method Not Allowed");
            }
        }

        private void handleUpdateTextContent(HttpExchange exchange) throws IOException {
            Connection conn = null;
            PreparedStatement ps = null;
            try {
                String requestBody = getRequestBody(exchange);
                JSONObject json = new JSONObject(requestBody);

                String sectionName = json.getString("sectionName");
                String contentKey = json.getString("contentKey");
                String contentValue = json.getString("contentValue");

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
                    sendResponse(exchange, 200, "Text content updated successfully!");
                } else {
                    sendResponse(exchange, 500, "Failed to update text content.");
                }

            } catch (IOException | SQLException e) {
                System.err.println("Error updating text content: " + e.getMessage());
                e.printStackTrace();
                sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
            } finally {
                DBManager.close(null, ps, conn);
            }
        }
    }

    public static class ImageAssetUpdateHandler extends BaseUpdateHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "PUT, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleUpdateImageAsset(exchange);
            } else {
                sendResponse(exchange, 405, "Method Not Allowed");
            }
        }

        private void handleUpdateImageAsset(HttpExchange exchange) throws IOException {
            Connection conn = null;
            PreparedStatement ps = null;
            try {
                String requestBody = getRequestBody(exchange);
                JSONObject json = new JSONObject(requestBody);

                String sectionName = json.getString("sectionName");
                String assetKey = json.getString("assetKey");
                String imageUrl = json.getString("imageUrl");
                String altText = json.optString("altText", null);

                conn = DBManager.getConnection();
                String sql = "INSERT INTO image_assets (section_name, asset_key, image_url, alt_text) VALUES (?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE image_url = VALUES(image_url), alt_text = VALUES(alt_text)";
                ps = conn.prepareStatement(sql);
                ps.setString(1, sectionName);
                ps.setString(2, assetKey);
                ps.setString(3, imageUrl);
                ps.setString(4, altText);

                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    sendResponse(exchange, 200, "Image asset updated successfully!");
                } else {
                    sendResponse(exchange, 500, "Failed to update image asset.");
                }

            } catch (IOException | SQLException e) {
                System.err.println("Error updating image asset: " + e.getMessage());
                e.printStackTrace();
                sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
            } finally {
                DBManager.close(null, ps, conn);
            }
        }
    }

    public static class VideoAssetUpdateHandler extends BaseUpdateHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "PUT, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleUpdateVideoAsset(exchange);
            } else {
                sendResponse(exchange, 405, "Method Not Allowed");
            }
        }

        private void handleUpdateVideoAsset(HttpExchange exchange) throws IOException {
            Connection conn = null;
            PreparedStatement ps = null;
            try {
                String requestBody = getRequestBody(exchange);
                JSONObject json = new JSONObject(requestBody);

                String sectionName = json.getString("sectionName");
                String assetKey = json.getString("assetKey");
                String videoUrl = json.getString("videoUrl");
                String posterUrl = json.optString("posterUrl", null);

                conn = DBManager.getConnection();
                String sql = "INSERT INTO video_assets (section_name, asset_key, video_url, poster_url) VALUES (?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE video_url = VALUES(video_url), poster_url = VALUES(poster_url)";
                ps = conn.prepareStatement(sql);
                ps.setString(1, sectionName);
                ps.setString(2, assetKey);
                ps.setString(3, videoUrl);
                ps.setString(4, posterUrl);

                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    sendResponse(exchange, 200, "Video asset updated successfully!");
                } else {
                    sendResponse(exchange, 500, "Failed to update video asset.");
                }

            } catch (IOException | SQLException e) {
                System.err.println("Error updating video asset: " + e.getMessage());
                e.printStackTrace();
                sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
            } finally {
                DBManager.close(null, ps, conn);
            }
        }
    }

    public static class FaqUpdateHandler extends BaseUpdateHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "PUT, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleUpdateFaq(exchange);
            } else {
                sendResponse(exchange, 405, "Method Not Allowed");
            }
        }

        private void handleUpdateFaq(HttpExchange exchange) throws IOException {
            Connection conn = null;
            PreparedStatement ps = null;
            try {
                String requestBody = getRequestBody(exchange);
                JSONObject json = new JSONObject(requestBody);

                int id = json.getInt("id"); // ID is required for update
                String question = json.getString("question");
                String answer = json.getString("answer");
                int displayOrder = json.getInt("displayOrder");

                conn = DBManager.getConnection();
                String sql = "UPDATE faq_items SET question = ?, answer = ?, display_order = ? WHERE id = ?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, question);
                ps.setString(2, answer);
                ps.setInt(3, displayOrder);
                ps.setInt(4, id);

                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    sendResponse(exchange, 200, "FAQ item updated successfully!");
                } else {
                    sendResponse(exchange, 404, "FAQ item not found with ID: " + id);
                }

            } catch (IOException | SQLException e) {
                System.err.println("Error updating FAQ item: " + e.getMessage());
                e.printStackTrace();
                sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
            } finally {
                DBManager.close(null, ps, conn);
            }
        }
    }

    public static class NavbarUpdateHandler extends BaseUpdateHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "PUT, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleUpdateNavbar(exchange);
            } else {
                sendResponse(exchange, 405, "Method Not Allowed");
            }
        }

        private void handleUpdateNavbar(HttpExchange exchange) throws IOException {
            Connection conn = null;
            PreparedStatement ps = null;
            try {
                String requestBody = getRequestBody(exchange);
                JSONObject json = new JSONObject(requestBody);

                int id = json.getInt("id"); // ID is required for update
                String itemText = json.getString("itemText");
                String itemHref = json.getString("itemHref");
                boolean isDropdown = json.getBoolean("isDropdown");
                Integer parentId = json.has("parentId") && !json.isNull("parentId") ? json.getInt("parentId") : null;
                int displayOrder = json.getInt("displayOrder");

                conn = DBManager.getConnection();
                String sql = "UPDATE navbar_items SET item_text = ?, item_href = ?, is_dropdown = ?, parent_id = ?, display_order = ? WHERE id = ?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, itemText);
                ps.setString(2, itemHref);
                ps.setBoolean(3, isDropdown);
                if (parentId != null) {
                    ps.setInt(4, parentId);
                } else {
                    ps.setNull(4, java.sql.Types.INTEGER);
                }
                ps.setInt(5, displayOrder);
                ps.setInt(6, id);

                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    sendResponse(exchange, 200, "Navbar item updated successfully!");
                } else {
                    sendResponse(exchange, 404, "Navbar item not found with ID: " + id);
                }

            } catch (IOException | SQLException e) {
                System.err.println("Error updating navbar item: " + e.getMessage());
                e.printStackTrace();
                sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
            } finally {
                DBManager.close(null, ps, conn);
            }
        }
    }
}
