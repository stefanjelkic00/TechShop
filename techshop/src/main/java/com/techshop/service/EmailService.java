package com.techshop.service;

import com.techshop.model.Order;

public interface EmailService {
    void sendOrderConfirmation(String toEmail, Order order);
    void sendVerificationEmail(String toEmail, String firstName, String token);
    void sendRegistrationConfirmation(String toEmail, String firstName);
    void sendOrderCancellationEmail(String toEmail, Order order); // Nova metoda
}