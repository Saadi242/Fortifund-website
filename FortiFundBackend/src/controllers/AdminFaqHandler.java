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

public class AdminFaqHandler implements HttpHandler {

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

        switch (exchange.getRequestMethod()) {
            case "GET":
                handleGetFaqs(exchange);
                break;
            case "POST":
                handleAddFaq(exchange);
                break;
            case "PUT":
                handleUpdateFaq(exchange);
                break;
            case "DELETE":
                handleDeleteFaq(exchange);
                break;
            default:
                sendResponse(exchange, 405, "{\"message\": \"Method Not Allowed\"}");
                break;
        }
    }

    private void handleGetFaqs(HttpExchange exchange) throws IOException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        JSONArray jsonArray = new JSONArray();

        try {
            conn = DBManager.getConnection();
            String sql = "SELECT id, question, answer, display_order FROM faq_items ORDER BY display_order ASC";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                JSONObject jsonItem = new JSONObject();
                jsonItem.put("id", rs.getInt("id"));
                jsonItem.put("question", rs.getString("question"));
                jsonItem.put("answer", rs.getString("answer"));
                jsonItem.put("displayOrder", rs.getInt("display_order"));
                jsonArray.put(jsonItem);
            }
            sendResponse(exchange, 200, jsonArray.toString());

        } catch (SQLException e) {
            System.err.println("Database error fetching FAQ items: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"message\": \"Internal Server Error: Database access failed for FAQs.\"}");
        } finally {
            DBManager.close(rs, ps, conn);
        }
    }

    private void handleAddFaq(HttpExchange exchange) throws IOException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            JSONObject requestBody = parseRequestBody(exchange);
            String question = requestBody.optString("question");
            String answer = requestBody.optString("answer");
            int displayOrder = requestBody.optInt("displayOrder", 0); // Default to 0 if not provided

            if (question.isEmpty() || answer.isEmpty() || displayOrder == 0) {
                sendResponse(exchange, 400, "{\"message\": \"Missing required fields: question, answer, or displayOrder.\"}");
                return;
            }

            conn = DBManager.getConnection();
            String sql = "INSERT INTO faq_items (question, answer, display_order) VALUES (?, ?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, question);
            ps.setString(2, answer);
            ps.setInt(3, displayOrder);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                sendResponse(exchange, 201, "{\"message\": \"FAQ item added successfully.\"}");
            } else {
                sendResponse(exchange, 500, "{\"message\": \"Failed to add FAQ item.\"}");
            }

        } catch (SQLException e) {
            System.err.println("Database error adding FAQ item: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"message\": \"Internal Server Error: Database access failed.\"}");
        } catch (Exception e) {
            System.err.println("Error processing add FAQ request: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 400, "{\"message\": \"Invalid JSON or request body.\"}");
        } finally {
            DBManager.close(null, ps, conn);
        }
    }

    private void handleUpdateFaq(HttpExchange exchange) throws IOException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            JSONObject requestBody = parseRequestBody(exchange);
            int id = requestBody.optInt("id");
            String question = requestBody.optString("question");
            String answer = requestBody.optString("answer");
            int displayOrder = requestBody.optInt("displayOrder", 0);

            if (id == 0 || question.isEmpty() || answer.isEmpty() || displayOrder == 0) {
                sendResponse(exchange, 400, "{\"message\": \"Missing required fields: id, question, answer, or displayOrder.\"}");
                return;
            }

            conn = DBManager.getConnection();
            String sql = "UPDATE faq_items SET question = ?, answer = ?, display_order = ? WHERE id = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, question);
            ps.setString(2, answer);
            ps.setInt(3, displayOrder);
            ps.setInt(4, id);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                sendResponse(exchange, 200, "{\"message\": \"FAQ item updated successfully.\"}");
            } else {
                sendResponse(exchange, 404, "{\"message\": \"FAQ item not found or no changes made.\"}");
            }

        } catch (SQLException e) {
            System.err.println("Database error updating FAQ item: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"message\": \"Internal Server Error: Database access failed.\"}");
        } catch (Exception e) {
            System.err.println("Error processing update FAQ request: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 400, "{\"message\": \"Invalid JSON or request body.\"}");
        } finally {
            DBManager.close(null, ps, conn);
        }
    }

    private void handleDeleteFaq(HttpExchange exchange) throws IOException {
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
            String sql = "DELETE FROM faq_items WHERE id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                sendResponse(exchange, 200, "{\"message\": \"FAQ item deleted successfully.\"}");
            } else {
                sendResponse(exchange, 404, "{\"message\": \"FAQ item not found.\"}");
            }

        } catch (SQLException e) {
            System.err.println("Database error deleting FAQ item: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"message\": \"Internal Server Error: Database access failed.\"}");
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
