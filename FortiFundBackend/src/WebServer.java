import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

// Import statements for your handlers
import controllers.NavbarHandler;
import controllers.FaqHandler;
import controllers.ContentHandler;
import controllers.FormHandler;
// import controllers.ContentUpdateHandler; // This is likely redundant now
import controllers.AuthHandler;
import controllers.AdminContentHandler;
import controllers.AdminFormsHandler;
import controllers.AssetHandler;
import controllers.AdminFaqHandler;
import controllers.AdminNavbarHandler; // NEW: Import AdminNavbarHandler

import db.DBManager;

public class WebServer {

    public static void main(String[] args) {
        int port = 8080;
        HttpServer server = null;

        try {
            DBManager.init();
            System.out.println("DBManager initialized. MySQL JDBC Driver loaded.");

            server = HttpServer.create(new InetSocketAddress(port), 0);
            ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
            server.setExecutor(threadPoolExecutor);

            // Existing Public API Endpoints
            server.createContext("/api/content/navbar-items", new NavbarHandler());
            server.createContext("/api/content/faq-items", new FaqHandler());
            server.createContext("/api/content/text-content", new ContentHandler());
            server.createContext("/api/content/image-assets", new ContentHandler());
            server.createContext("/api/content/video-assets", new ContentHandler());
            server.createContext("/api/submit/contact", new FormHandler());
            server.createContext("/api/submit/demo", new FormHandler());

            // Admin API Endpoints
            server.createContext("/api/admin/login", new AuthHandler());
            server.createContext("/api/admin/content", new AdminContentHandler());
            server.createContext("/api/admin/contact-messages", new AdminFormsHandler());
            server.createContext("/api/admin/demo-requests", new AdminFormsHandler());
            server.createContext("/api/admin/assets/image", new AssetHandler());
            server.createContext("/api/admin/assets/video", new AssetHandler());
            server.createContext("/api/admin/faqs", new AdminFaqHandler());
            server.createContext("/api/admin/navbar", new AdminNavbarHandler()); // NEW: Register AdminNavbarHandler


            server.start();
            System.out.println("Server started on port " + port);

        } catch (IOException e) {
            System.err.println("Server failed to start: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error during server initialization or DBManager init: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Server runs continuously
        }
    }
}
