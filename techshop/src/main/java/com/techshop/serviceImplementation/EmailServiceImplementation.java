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
        String subject = "Order Confirmation - Order #" + order.getId();
        String body = "Hello, your order has been successfully placed!\n\n" +
                      "Order ID: " + order.getId() + "\n" +
                      "Total Price: " + order.getTotalPrice() + "\n" +
                      "Thank you for shopping with us!";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
            System.out.println("✅ Order confirmation email sent successfully!");
        } catch (MessagingException e) {
            throw new RuntimeException("❌ Failed to send email", e);
        }
    }
}
