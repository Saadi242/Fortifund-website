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

public class AdminFormsHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Set CORS headers
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/api/admin/contact-messages")) {
                handleGetContactMessages(exchange);
            } else if (path.equals("/api/admin/demo-requests")) {
                handleGetDemoRequests(exchange);
            } else {
                sendResponse(exchange, 404, "Not Found");
            }
        } else {
            sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    private void handleGetContactMessages(HttpExchange exchange) throws IOException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        JSONArray jsonArray = new JSONArray();

        try {
            conn = DBManager.getConnection();
            String sql = "SELECT id, name, email, message, submission_time FROM contact_messages ORDER BY submission_time DESC";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                JSONObject jsonItem = new JSONObject();
                jsonItem.put("id", rs.getInt("id"));
                jsonItem.put("name", rs.getString("name"));
                jsonItem.put("email", rs.getString("email"));
                jsonItem.put("message", rs.getString("message"));
                jsonItem.put("submissionTime", rs.getTimestamp("submission_time").toString());
                jsonArray.put(jsonItem);
            }

            sendResponse(exchange, 200, jsonArray.toString());

        } catch (SQLException e) {
            System.err.println("Database error fetching contact messages: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal Server Error: Database access failed.");
        } finally {
            DBManager.close(rs, ps, conn);
        }
    }

    private void handleGetDemoRequests(HttpExchange exchange) throws IOException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        JSONArray jsonArray = new JSONArray();

        try {
            conn = DBManager.getConnection();
            String sql = "SELECT id, full_name, phone_number, company, email, comments, demo_date, demo_time, submission_time FROM demo_requests ORDER BY submission_time DESC";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                JSONObject jsonItem = new JSONObject();
                jsonItem.put("id", rs.getInt("id"));
                jsonItem.put("fullName", rs.getString("full_name"));
                jsonItem.put("phoneNumber", rs.getString("phone_number"));
                jsonItem.put("company", rs.getString("company"));
                jsonItem.put("email", rs.getString("email"));
                jsonItem.put("comments", rs.getString("comments"));
                jsonItem.put("demoDate", rs.getDate("demo_date") != null ? rs.getDate("demo_date").toString() : null);
                jsonItem.put("demoTime", rs.getTime("demo_time") != null ? rs.getTime("demo_time").toString() : null);
                jsonItem.put("submissionTime", rs.getTimestamp("submission_time").toString());
                jsonArray.put(jsonItem);
            }

            sendResponse(exchange, 200, jsonArray.toString());

        } catch (SQLException e) {
            System.err.println("Database error fetching demo requests: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal Server Error: Database access failed.");
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
