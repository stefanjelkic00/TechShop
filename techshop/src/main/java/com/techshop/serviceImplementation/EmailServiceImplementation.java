package com.techshop.serviceImplementation;

import com.techshop.model.Order;
import com.techshop.service.EmailService;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImplementation implements EmailService {

    private final JavaMailSender mailSender;

    public EmailServiceImplementation(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendOrderConfirmation(String toEmail, Order order) {
        String subject = "Potvrda porudžbine - Porudžbina #" + order.getId();
        String body = "Zdravo, vaša porudžbina je uspešno poslata!\n\n" +
                      "ID Porudžbine: " + order.getId() + "\n" +
                      "Ukupna cena: " + order.getTotalPrice() + " USD\n" +
                      "Hvala vam što kupujete kod nas!";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
            System.out.println("✅ Potvrdni mejl za porudžbinu uspešno poslat na: " + toEmail);
        } catch (MessagingException e) {
            System.err.println("❌ Greška pri slanju mejla na " + toEmail + ": " + e.getMessage());
            throw new RuntimeException("Greška pri slanju mejla", e);
        }
    }

    @Override
    public void sendRegistrationConfirmation(String toEmail, String firstName) {
        String subject = "Uspešna registracija na TechShop";
        String body = "<h3>Zdravo " + firstName + ",</h3>" +
                      "<p>Uspešno ste se registrovali na TechShop!</p>" +
                      "<p>Možete se prijaviti i početi sa kupovinom.</p>" +
                      "<p>Hvala vam što ste izabrali TechShop!</p>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
            System.out.println("✅ Potvrdni mejl za registraciju uspešno poslat na: " + toEmail);
        } catch (MessagingException e) {
            System.err.println("❌ Greška pri slanju potvrdnog mejla na " + toEmail + ": " + e.getMessage());
            throw new RuntimeException("Greška pri slanju potvrdnog mejla", e);
        }
    }

    @Override
    public void sendVerificationEmail(String toEmail, String firstName, String token) {
        String subject = "Potvrda registracije na TechShop";
        String verificationUrl = "http://localhost:8001/api/users/verify?token=" + token;
        String body = "<h3>Zdravo " + firstName + ",</h3>" +
                      "<p>Hvala vam što ste se registrovali na TechShop!</p>" +
                      "<p>Kliknite na dugme ispod da potvrdite vašu registraciju. Bićete preusmereni na stranicu za prijavu:</p>" +
                      "<a href='" + verificationUrl + "' style='display: inline-block; padding: 10px 20px; " +
                      "background-color: #ff4500; color: white; text-decoration: none; border-radius: 5px;'>Potvrdi registraciju</a>" +
                      "<p>Ako dugme ne radi, kopirajte i nalepite sledeći link u vaš pretraživač:</p>" +
                      "<p>" + verificationUrl + "</p>" +
                      "<p>Hvala vam što ste izabrali TechShop!</p>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
            System.out.println("✅ Verifikacioni mejl uspešno poslat na: " + toEmail);
        } catch (MessagingException e) {
            System.err.println("❌ Greška pri slanju verifikacionog mejla na " + toEmail + ": " + e.getMessage());
            throw new RuntimeException("Greška pri slanju verifikacionog mejla", e);
        }
    }
    
    @Override
    public void sendOrderCancellationEmail(String toEmail, Order order) {
        String subject = "Porudžbina #" + order.getId() + " je otkazana";
        String body = "<h3>Zdravo,</h3>" +
                      "<p>Obaveštavamo vas da je vaša porudžbina sa ID-jem #" + order.getId() + " otkazana.</p>" +
                      "<p>Ukupna cena: " + order.getTotalPrice() + " USD</p>" +
                      "<p>Ako imate bilo kakvih pitanja, slobodno nas kontaktirajte.</p>" +
                      "<p>Hvala vam što koristite TechShop!</p>"+
                      "Kontakt: +381 69 333555";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
            System.out.println("✅ Mej otkazivanja porudžbine uspešno poslat na: " + toEmail);
        } catch (MessagingException e) {
            System.err.println("❌ Greška pri slanju mejla o otkazivanju na " + toEmail + ": " + e.getMessage());
            throw new RuntimeException("Greška pri slanju mejla o otkazivanju", e);
        }
    }
    
    
}