package es.uvigo.esei.xcs.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Clase utilitaria para enviar correos electrónicos.
 * 
 * Configuración esperada en email-config.txt:
 * 
 * smtp.host=smtp.example.com
 * smtp.port=587
 * smtp.user=usuario@example.com
 * smtp.password=contraseña
 * smtp.auth=true
 * smtp.starttls.enable=true
 */
public class EmailSender {

    private final Properties config = new Properties();
    private final Session session;

    public EmailSender(String configFilePath) throws IOException {
        // Cargar configuración desde el archivo .txt
        try (FileInputStream fis = new FileInputStream(configFilePath)) {
            config.load(fis);
        }

        // Crear sesión de email autenticada
        this.session = Session.getInstance(config, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                    config.getProperty("smtp.user"),
                    config.getProperty("smtp.password")
                );
            }
        });
    }

    /**
     * Envía un correo electrónico.
     *
     * @param toEmail dirección del destinatario
     * @param subject asunto del correo
     * @param body contenido del mensaje
     */
    public void sendEmail(String toEmail, String subject, String body) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(config.getProperty("smtp.user")));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            System.out.println("✅ Email enviado correctamente a " + toEmail);
        } catch (MessagingException e) {
            System.err.println("❌ Error enviando correo: " + e.getMessage());
            throw new RuntimeException("Error enviando email", e);
        }
    }
}
