package com.techshop.serviceImplementation;

import com.techshop.dto.AddressDTO;
import com.techshop.dto.AddressUpdateDTO;
import com.techshop.model.Address;
import com.techshop.model.Order;
import com.techshop.repository.AddressRepository;
import com.techshop.service.AddressService;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;


import java.util.List;
import java.util.Optional;

@Service
public class AddressServiceImplementation implements AddressService {

    private final AddressRepository addressRepository;
    private final JavaMailSender mailSender;

    public AddressServiceImplementation(AddressRepository addressRepository, JavaMailSender mailSender) {
        this.addressRepository = addressRepository;
        this.mailSender = mailSender; // Ispravno inicijalizovano
    }

    @Override
    public List<Address> getAllAddresses() {
        return addressRepository.findAll();
    }

    @Override
    public Optional<Address> getAddressById(Long id) {
        return addressRepository.findById(id);
    }

    @Override
    public Address saveAddress(AddressDTO addressDTO) {
        Address address = new Address();
        address.setStreet(addressDTO.getStreet());
        address.setCity(addressDTO.getCity());
        address.setPostalCode(addressDTO.getPostalCode());
        address.setCountry(addressDTO.getCountry());

        return addressRepository.save(address);
    }
    @Override
    public Address updateAddress(AddressUpdateDTO addressUpdateDTO) {
        Optional<Address> existingAddress = addressRepository.findById(addressUpdateDTO.getId());
        
        if (existingAddress.isPresent()) {
            Address address = existingAddress.get();
            address.setStreet(addressUpdateDTO.getStreet());
            address.setCity(addressUpdateDTO.getCity());
            address.setPostalCode(addressUpdateDTO.getPostalCode());
            address.setCountry(addressUpdateDTO.getCountry());
            return addressRepository.save(address);
        } else {
            throw new RuntimeException("Address not found with id: " + addressUpdateDTO.getId());
        }
    }


    @Override
    public void deleteAddress(Long id) {
        addressRepository.deleteById(id);
    }
    
    

    @Override
    public void sendOrderConfirmation(String to, Order order) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject("Order Confirmation - TechShop");
            helper.setText("Hello, your order #" + order.getId() + " has been placed successfully.\n\nTotal: " 
                           + order.getTotalPrice() + " EUR.\n\nThank you for shopping with us!", false);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }

	

	
}
