package com.techshop.service;

import com.techshop.model.Order;

public interface EmailService {
    void sendOrderConfirmation(String toEmail, Order order);
}
