FortiFund Website: Local Setup Guide

This guide provides step-by-step instructions on how to set up and run the FortiFund website on your local machine using both IntelliJ IDEA and Visual Studio Code.

Table of Contents:

1.Prerequisites
2.Setup Instructions
 2.1. Database Setup (MySQL)
 2.2. Backend Setup (Java in IntelliJ IDEA)
 2.3. Backend Setup (Java in VS Code)
 2.4. Frontend Files
3.Running the Project
 3.1. Starting the Java Backend (IntelliJ IDEA)
 3.2. Starting the Java Backend (VS Code)
 3.3. Accessing the Frontend
4.Troubleshooting Tips

1. PrerequisitesBefore you begin, ensure you have the following installed on your system:Java Development Kit (JDK) 11 or higherMySQL ServerMySQL Workbench (or any other MySQL client like phpMyAdmin)IntelliJ IDEA (Community Edition is sufficient)Visual Studio Code (VS Code)VS Code Extension Pack for Java: Install this from the VS Code Marketplace.

2. Setup InstructionsFollow these steps to prepare your environment.
 2.1. Database Setup (MySQL)Start your MySQL Server.Open MySQL Workbench and connect to your MySQL server.Create the database: Execute the command to create a database named fortifund_db.Populate the database: Run the comprehensive SQL script (which contains all CREATE TABLE and INSERT statements for your website's data) that was provided to you previously. This script will set up all necessary tables and populate them with initial content.Note: Ensure the DBManager.java file in your backend project is configured with the correct MySQL username (e.g., root) and password for your database connection.

 2.2. Backend Setup (Java in IntelliJ IDEA)Open the FortiFundBackend project in IntelliJ IDEA.Add necessary libraries (JARs):Ensure the mysql-connector-j-x.x.x.jar (MySQL JDBC Driver) is located in your project's lib folder.Ensure the json-x.x.x.jar (for JSON handling) is also in your project's lib folder.In IntelliJ IDEA, go to File > Project Structure > Modules. Select your backend module, go to the Dependencies tab, and add these JARs to the module classpath.Build the project. Go to Build > Rebuild Project.
 
 2.3. Backend Setup (Java in VS Code)Open the FortiFundBackend folder in VS Code.Configure Java Project Libraries:In VS Code, open the "Java Projects" view (the Java coffee cup icon on the left sidebar).Expand your FortiFundBackend project.Under "Referenced Libraries" (or "Libraries"), click the + icon to "Add JARs".Navigate to your FortiFundBackend/lib folder.Select both json-x.x.x.jar and mysql-connector-j-x.x.x.jar and add them.Click "Apply Settings" if prompted.Clean Java Language Server Workspace: (Optional, but good for refreshing the language server)Open the Command Palette (Ctrl+Shift+P or Cmd+Shift+P).Type Java: Clean Java Language Server Workspace and select it.

 2.4. Frontend FilesLocate the FortiFundFrontend folder on your computer.No special server setup is required for the frontend; these are static HTML, CSS, and JavaScript files that will be opened directly in your web browser.

3. Running the Project:

 Follow these steps in order to run the FortiFund website:

 3.1. Starting the Java Backend (IntelliJ IDEA)In IntelliJ IDEA,
 firstly build the project press: ctrl+shift+B
 navigate to the src/WebServer.java file.Locate the main method within WebServer.java.Click the green "Run" (Play) button that appears next to the main method declaration or in the top right corner of the IDE.The "Run" tool window at the bottom will display the server's output. 

 You should see:

 DBManager initialized. MySQL JDBC Driver loaded.
 Server started on port 8080

 To stop the server, click the red square "Stop" button in the "Run" tool window.

 3.2. Starting the Java Backend (VS Code)In VS Code,
 firstly build the project press: ctrl+shift+B
 open the FortiFundBackend folder.Navigate to the src/WebServer.java file in the Explorer.Locate the main method within WebServer.java.Click the green "Run" (Play) button that appears next to the main method declaration.The "TERMINAL" panel in VS Code will open and display the server's output. 

 You should see:

 DBManager initialized. MySQL JDBC Driver loaded.
 Server started on port 8080

 To stop the server, go to the terminal panel and press Ctrl + C.

 3.3. Accessing the FrontendOnce your Java backend server is running (either via IntelliJ IDEA or VS Code):Open your web browser (e.g., Chrome, Firefox, Edge).Navigate directly to your HTML files:Main Website: Open the index.html file located in your FortiFundFrontend folder.Example path: file:///C:/Users/YourUser/Desktop/fortifund%20website/FortiFundFrontend/index.htmlAdmin Panel: Open the admin.html file located in your FortiFundFrontend folder.Example path: file:///C:/Users/YourUser/Desktop/fortifund%20website/FortiFundFrontend/admin.html(Adjust the file:///path/to/your/ part to your actual file location.)
 
 4. Troubleshooting Tips"Server started on port 8080" but content is missing on website:Perform a hard refresh in your browser (Ctrl + Shift + R or Cmd + Shift + R).Check your browser's Developer Tools (F12) -> Console tab for any red JavaScript errors.Check your browser's Developer Tools (F12) -> Network tab for any failed requests (non-200 status codes) to http://localhost:8080.

 Admin Panel Login:Username: admin / Password: 12345 

 Java compilation errors in VS Code (e.g., "package org.json does not exist"):Ensure you have added the necessary JARs (json-x.x.x.jar and mysql-connector-j-x.x.x.jar) to your VS Code Java project's "Referenced Libraries" as described in Section 2.3.Try "Java: Clean Java Language Server Workspace" from the VS Code Command Palette.Server not starting or database connection issues:Verify your MySQL server is running.Double-check the database credentials in DBManager.java.Ensure the MySQL JDBC driver JAR is correctly added to your project's dependencies.