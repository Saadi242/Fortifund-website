package controllers;// controllers.FaqHandler.java
// Handles requests for FAQ items.

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import db.DBManager;
import org.json.JSONArray;
import org.json.JSONObject;
import models.FaqItem;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FaqHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Set CORS headers
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1); // No Content
            return;
        }

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            handleGetFaqItems(exchange);
        } else {
            sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    private void handleGetFaqItems(HttpExchange exchange) throws IOException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<FaqItem> faqItems = new ArrayList<>();
        JSONArray jsonArray = new JSONArray();

        try {
            conn = db.DBManager.getConnection();
            String sql = "SELECT id, question, answer, display_order FROM faq_items ORDER BY display_order ASC";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                FaqItem item = new FaqItem(
                        rs.getInt("id"),
                        rs.getString("question"),
                        rs.getString("answer"),
                        rs.getInt("display_order")
                );
                faqItems.add(item);
            }

            for (FaqItem item : faqItems) {
                JSONObject jsonItem = new JSONObject();
                jsonItem.put("id", item.getId());
                jsonItem.put("question", item.getQuestion());
                jsonItem.put("answer", item.getAnswer());
                jsonArray.put(jsonItem);
            }

            sendResponse(exchange, 200, jsonArray.toString());

        } catch (SQLException e) {
            System.err.println("Database error fetching FAQ items: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal Server Error: Database access failed.");
        } finally {
            DBManager.close(rs, ps, conn);
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}