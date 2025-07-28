FortiFund Website:
 Local Setup GuideThis guide provides concise instructions on how to get the FortiFund website up and running on your local machine. The project consists of a Java backend, a MySQL database, and a static HTML/CSS/JavaScript frontend.
 Table of Contents
 1.Project Overview
 2.PrerequisitesSetup 
 3.Instructions
 3.1. Database Setup (MySQL)
 3.2. Backend Setup (Java)
 3.3. Frontend Setup (HTML/CSS/JS)
 4.Running the Project
 5.Troubleshooting 
 Tips1. Project OverviewThe FortiFund website is a dynamic web application. It uses a Java backend to serve content from a MySQL database to a static HTML/CSS/JavaScript frontend. It includes a public-facing website and an administrative panel for content management.
 2. PrerequisitesBefore you begin, ensure you have the following installed on your system:Java Development Kit (JDK) 11 or higherMySQL ServerMySQL Workbench (or another MySQL client like phpMyAdmin)IntelliJ IDEA (or your preferred Java IDE)
 3. Setup InstructionsFollow these steps to prepare your environment.
 3.1. Database Setup (MySQL)Start your MySQL Server.Open MySQL Workbench and connect to your server.Create the database: Execute CREATE DATABASE IF NOT EXISTS fortifund_db;Populate the database: You will need to run the full SQL script (containing CREATE TABLE and INSERT statements for text_content, image_assets, video_assets, faq_items, navbar_items, contact_messages, demo_requests) that was previously provided. This script sets up all tables and initial content.Self-note: Ensure your DBManager.java is configured with the correct 
 MySQL username: root
 password :12345 
 3.2. Backend Setup (Java)Open the FortiFundBackend project in IntelliJ IDEA.Add necessary libraries (JARs):Ensure mysql-connector-java-x.x.x.jar (MySQL JDBC Driver) is in your project's lib folder and added to the module dependencies.Ensure json-x.x.x.jar (for JSON handling) is also in your project's lib folder and added to the module dependencies.Build the project. This compiles all your Java code.3.3. Frontend Setup (HTML/CSS/JS)Locate the FortiFundFrontend folder.No special setup is required here; these are static files that will be opened directly in your browser.
 4. Running the ProjectFollow these steps in order to run the FortiFund website:Start the Java Backend:In IntelliJ IDEA, navigate to src/WebServer.java.Run the main method (usually by clicking the green play button next to the main method or Run -> Run 'WebServer').You should see console output indicating "Server started on port 8080".Open the Frontend:Navigate to your FortiFundFrontend folder on your computer.Open index.html in your web browser (e.g., Chrome, Firefox).You can also open admin.html to access the admin panel.
 5. website link: http://localhost:63342/fortifund%20website/FortiFundFrontend/index.html#
6. Troubleshooting Tips"Server started on port 8080" but nothing shows on website:Perform a hard refresh in your browser (Ctrl + Shift + R or Cmd + Shift + R).Check your browser's Developer Tools (F12) -> Console tab for any red JavaScript errors.Check your browser's Developer Tools (F12) -> Network tab for any failed requests (non-200 status codes) to http://localhost:8080.Check your IntelliJ IDEA console for any red Java errors (SQL exceptions, etc.).
 Admin Panel Login:
 Username: admin
 Password:12345
6. link for admin: http://localhost:63342/fortifund%20website/FortiFundFrontend/admin.html?_ijt=4fku4hqbk7udfia7qrjaek4h1p&_ij_reload=RELOAD_ON_SAVE
Content not updating after saving in Admin Panel:Ensure you clicked "Save All Changes" in the admin panel.Perform a hard refresh on the public website page you expect to see changes on.Restart your Java Backend server.Verify the data is correctly in your MySQL database using MySQL Workbench.
7. Command Line (More Advanced - if you want to run without IDE):

First, you need to compile your Java project. Navigate to your FortiFundBackend directory in your terminal.

Compile:

terminal

javac -cp "lib/*" src/WebServer.java src/db/DBManager.java src/controllers/*.java -d out

(Make sure lib contains mysql-connector-j-x.x.x.jar and json-x.x.x.jar)

Run:

terminal

java -cp "out:lib/*" WebServer

(On Windows, use out;lib/* instead of out:lib/*)

You should see output like "Server started on port 8080" in your console.

2. Open the Frontend Website
   Once your Java backend server is running, you can access the website:

Open your web browser (Chrome, Firefox, Edge, etc.).

Navigate to your FortiFundFrontend directory on your computer.

Open the HTML files directly:

For the main website: file:///path/to/your/FortiFundFrontend/index.html

For the admin panel: file:///path/to/your/FortiFundFrontend/admin.html

(Replace file:///path/to/your/FortiFundFrontend/ with the actual file path on your system.)