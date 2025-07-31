package utils; // Create a 'utils' package in your src directory for this file

// CHANGE THESE IMPORTS FROM JAVAX TO JAKARTA
import jakarta.mail.*;
import jakarta.mail.internet.*;
// Keep this one
import java.util.Properties;

public class EmailService {

    // IMPORTANT: Replace with your actual email server details and credentials.
    // For production, consider using environment variables or a secure configuration file.
    private static final String SMTP_HOST = "smtp.gmail.com"; // e.g., "smtp.gmail.com"
    private static final String SMTP_PORT = "587"; // e.g., "587" for TLS, "465" for SSL
    private static final String SMTP_USERNAME = "maliksahabzada242@gmail.com"; // Your email address
    private static final String SMTP_PASSWORD = "pqsl tsxg mjqw ihfg"; // Your email password or app-specific password

    public static void sendDemoConfirmationEmail(String recipientEmail, String fullName, String phoneNumber, String company, String comments, String demoDate, String demoTime) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true"); // Use TLS
        properties.put("mail.smtp.host", SMTP_HOST);
        properties.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SMTP_USERNAME)); // Sender
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail)); // Recipient
            message.setSubject("FortiFund Demo Request Confirmation"); // Email Subject

            // Build the email content
            String emailContent = "Dear " + fullName + ",\n\n"
                    + "Thank you for requesting a demo with FortiFund!\n\n"
                    + "Here are the details of your request:\n"
                    + "Full Name: " + fullName + "\n"
                    + "Email: " + recipientEmail + "\n"
                    + "Phone Number: " + (phoneNumber != null && !phoneNumber.isEmpty() ? phoneNumber : "N/A") + "\n"
                    + "Company: " + company + "\n"
                    + "Requested Demo Date: " + (demoDate != null && !demoDate.isEmpty() ? demoDate : "N/A") + "\n"
                    + "Requested Demo Time: " + (demoTime != null && !demoTime.isEmpty() ? demoTime : "N/A") + "\n"
                    + "Comments: " + (comments != null && !comments.isEmpty() ? comments : "N/A") + "\n\n"
                    + "We will review your request and get back to you shortly to confirm the demo schedule.\n\n"
                    + "Best regards,\n"
                    + "The FortiFund Team";

            message.setText(emailContent); // Set email body as plain text

            Transport.send(message); // Send the email
            System.out.println("Demo confirmation email sent successfully to " + recipientEmail);

        } catch (MessagingException e) {
            System.err.println("Failed to send demo confirmation email to " + recipientEmail + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
