FortiFund Website: How to Run Locally

This guide provides concise instructions on how to get the FortiFund website running on your local machine, focusing on the execution steps.

Table of ContentsProject

1.Overview
2.Prerequisites
3.Setup Summary
3.1. Database Preparation
3.2. Java Backend Preparation (VS Code)
3.3. Frontend Files
4.Running the Project
4.1. Starting the Java Backend (Recommended: VS Code)
4.2. Starting the Java Backend (Alternative: Command Line)
4.3. Accessing the Frontend
5.Troubleshooting Tips

1. Project OverviewThe FortiFund website is a dynamic web application. It uses a Java backend to serve content from a MySQL database to a static HTML/CSS/JavaScript frontend. It includes a public-facing website and an administrative panel for content management.

2. PrerequisitesBefore you begin, ensure you have the following installed on your system:Java Development Kit (JDK) 11 or higherMySQL ServerMySQL Workbench (or another MySQL client like phpMyAdmin)Visual Studio Code (VS Code)VS Code Extension Pack for Java: Install this from the VS Code Marketplace.

3. Setup Summary
Before running, ensure your project is prepared:
3.1. Database PreparationStart your MySQL Server.Create the database: Ensure a database named fortifund_db exists.Populate the database: You must run the full SQL script (containing CREATE TABLE and INSERT statements for all tables like text_content, image_assets, faq_items, navbar_items, etc.) in your MySQL client. This script sets up all tables and initial content.Ensure your DBManager.java is configured with the correct MySQL username (root) and password :(12345)for your database.
3.2. Java Backend Preparation (VS Code)Open the FortiFundBackend folder in VS Code.Configure Java Project Libraries:In VS Code, go to the "Java Projects" view (usually the Java coffee cup icon on the left sidebar).Expand your FortiFundBackend project.Under "Referenced Libraries" (or "Libraries"), click the + icon to "Add JARs".Navigate to your FortiFundBackend/lib folder.Select both json-x.x.x.jar and mysql-connector-j-x.x.x.jar and add them.Click "Apply Settings" if prompted.Clean Java Language Server Workspace: (Optional, but good for refreshing)Open Command Palette (Ctrl+Shift+P or Cmd+Shift+P).Type Java: Clean Java Language Server Workspace and select it.
3.3. Frontend FilesLocate the FortiFundFrontend folder.No special server setup is required for the frontend; these are static HTML, CSS, and JavaScript files.

4. Running the Project
Follow these steps in order to run the FortiFund website:
4.1. Starting the Java Backend (Recommended: VS Code)In VS Code, open the FortiFundBackend folder.
Navigate to the src/WebServer.java file in the Explorer.
Locate the main method within WebServer.
java.Click the green "Run" (Play) button that appears next to the main method declaration.The "TERMINAL" panel in VS Code will open and display the server's output. 
You should see:DBManager initialized. MySQL JDBC Driver loaded.
Server started on port 8080
To stop the server, go to the terminal panel and press Ctrl + C.
4.2. Starting the Java Backend (Alternative: Command Line)If you prefer to run from your system's terminal (e.g., PowerShell, Command Prompt, Bash):

Open your terminal.Compile all Java files:

javac -d FortiFundBackend/out -cp "FortiFundBackend/lib/*" FortiFundBackend/src/WebServer.java FortiFundBackend/src/db/DBManager.java FortiFundBackend/src/controllers/*.java FortiFundBackend/src/models/*.java

Note for Windows: Ensure lib/* is correctly interpreted. If you face issues, you might need to list each JAR file explicitly or ensure your shell handles wildcards correctly.

Run the WebServer:

java -cp "FortiFundBackend/out;FortiFundBackend/lib/*" WebServer

Note for Linux/macOS: Use a colon : instead of a semicolon ; for the classpath separator: java -cp "out:lib/*" WebServerTo stop the server, press Ctrl + C in the terminal.
4.3. Accessing the FrontendOnce your Java backend server is running (either via VS Code or command line):Open your web browser (Chrome, Firefox, Edge, etc.).Navigate directly to your HTML files:Main Website: file:///path/to/your/FortiFundFrontend/index.htmlAdmin Panel: file:///path/to/your/FortiFundFrontend/admin.html(Replace file:///path/to/your/FortiFundFrontend/ with the actual file path on your system.)5. Troubleshooting Tips"Server started on port 8080" but content is missing on website:Perform a hard refresh in your browser (Ctrl + Shift + R or Cmd + Shift + R).Check your browser's Developer Tools (F12) -> Console tab for any red JavaScript errors.Check your browser's Developer Tools (F12) -> Network tab for any failed requests (non-200 status codes) to http://localhost:8080.Verify the content in your MySQL database using MySQL Workbench; sometimes data might be missing or empty.Admin Panel Login:Username: adminPassword: 12345Java compilation errors in VS Code (e.g., "package org.json does not exist"):Ensure you have added the json-x.x.x.jar and mysql-connector-j-x.x.x.jar to your VS Code Java project's "Referenced Libraries" as described in Section 3.2.Try "Java: Clean Java Language Server Workspace" from the VS Code Command Palette."file not found" errors when compiling from command line:Ensure your terminal's current directory is FortiFundBackend when running the javac command, or adjust the paths in the command to be relative to your current directory.