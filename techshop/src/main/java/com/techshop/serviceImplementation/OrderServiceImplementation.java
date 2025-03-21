package com.techshop.serviceImplementation;

import com.techshop.dto.AddressDTO;
import com.techshop.dto.OrderDTO;
import com.techshop.dto.OrderUpdateDTO;
import com.techshop.enums.CustomerType;
import com.techshop.enums.OrderStatus;
import com.techshop.model.*;
import com.techshop.repository.*;
import com.techshop.service.EmailService;
import com.techshop.service.OrderService;
import com.techshop.service.UserService;
import com.techshop.elasticsearch.service.ProductSyncService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderServiceImplementation implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImplementation.class);

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final EmailService emailService;
    private final AddressRepository addressRepository;
    private final UserService userService;
    private final ProductSyncService productSyncService; // Dodajemo ProductSyncService

    public OrderServiceImplementation(OrderRepository orderRepository, UserRepository userRepository,
                                      CartRepository cartRepository, CartItemRepository cartItemRepository,
                                      ProductRepository productRepository, OrderItemRepository orderItemRepository,
                                      AddressRepository addressRepository, EmailService emailService,
                                      UserService userService, ProductSyncService productSyncService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
        this.emailService = emailService;
        this.addressRepository = addressRepository;
        this.userService = userService;
        this.productSyncService = productSyncService;
    }

    @Override
    public List<OrderDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    public List<OrderDTO> getOrdersByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        List<Order> orders = orderRepository.findByUser(user);
        return orders.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Order createOrder(OrderDTO orderDTO, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        Order order = Order.builder()
                .totalPrice(orderDTO.getTotalPrice())
                .orderStatus(OrderStatus.PENDING)
                .user(user)
                .orderItems(new ArrayList<>())
                .address(orderDTO.getAddress())
                .build();

        order = orderRepository.save(order);

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Not enough stock for product: " + product.getName());
            }

            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            // Sinhronizuj sa Elasticsearch-om
            productSyncService.updateProductInElasticsearch(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            orderItemRepository.save(orderItem);
            order.getOrderItems().add(orderItem);
        }

        cartItemRepository.deleteAll(cart.getCartItems());
        cartRepository.delete(cart);

        emailService.sendOrderConfirmation(user.getEmail(), order);

        return order;
    }

    @Override
    @Transactional
    public Map<String, Object> createOrderFromCart(Long userId, AddressDTO addressDTO, HttpServletRequest request) throws IOException {
        logger.info("Pokrećem kreiranje porudžbine za userId: {}", userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.error("Korpa nije pronađena za userId: {}", userId);
                    return new RuntimeException("Korpa nije pronađena");
                });

        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            logger.error("Korpa je prazna za cartId: {}", cart.getId());
            throw new RuntimeException("Korpa je prazna");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("Korisnik nije pronađen za userId: {}", userId);
                    return new RuntimeException("Korisnik nije pronađen");
                });

        if (addressDTO == null || addressDTO.getStreet() == null || addressDTO.getCity() == null ||
                addressDTO.getPostalCode() == null || addressDTO.getCountry() == null) {
            logger.error("Neispravna adresa: {}", addressDTO);
            throw new RuntimeException("Adresa nije potpuno popunjena");
        }

        Address address = Address.builder()
                .street(addressDTO.getStreet())
                .city(addressDTO.getCity())
                .postalCode(addressDTO.getPostalCode())
                .country(addressDTO.getCountry())
                .build();
        address = addressRepository.save(address);

        // Računanje popusta na osnovu trenutnog CustomerType korisnika
        BigDecimal discount = getDiscountForUser(user);

        Order order = Order.builder()
                .user(user)
                .address(address)
                .orderStatus(OrderStatus.PENDING)
                .totalPrice(calculateTotalPrice(cart, discount))
                .orderItems(new ArrayList<>())
                .build();
        order = orderRepository.save(order);

        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                logger.error("Nema dovoljno na stanju za proizvod: {}", product.getName());
                throw new RuntimeException("Not enough stock for product: " + product.getName());
            }

            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            // Sinhronizuj sa Elasticsearch-om
            productSyncService.updateProductInElasticsearch(product);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .price(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                    .build();
            orderItemRepository.save(orderItem);
            order.getOrderItems().add(orderItem);
        }

        cartItemRepository.deleteAll(cart.getCartItems());
        cart.setCartItems(new ArrayList<>());
        cartRepository.save(cart);

        emailService.sendOrderConfirmation(user.getEmail(), order);

        // Ažuriranje CustomerType nakon kreiranja porudžbine
        long orderCount = orderRepository.countByUser(user);
        CustomerType newCustomerType = determineCustomerType(orderCount);
        user.setCustomerType(newCustomerType);
        userRepository.save(user);

        // Generisanje novog tokena sa ažuriranim CustomerType
        Algorithm algorithm = Algorithm.HMAC256("secretKey".getBytes());
        String newJwtToken = JWT.create()
                .withSubject(user.getEmail())
                .withExpiresAt(new Date(System.currentTimeMillis() + 15 * 60 * 1000))
                .withIssuer(request.getRequestURI())
                .withClaim("roles", user.getRole() != null ? List.of("ROLE_" + user.getRole().name()) : List.of("ROLE_USER"))
                .withClaim("firstName", user.getFirstName())
                .withClaim("lastName", user.getLastName())
                .withClaim("customerType", newCustomerType.name())
                .sign(algorithm);

        Map<String, Object> result = new HashMap<>();
        result.put("order", mapToDTO(order));
        result.put("jwtToken", newJwtToken);
        result.put("roles", user.getRole() != null ? user.getRole().name() : "USER");
        result.put("email", user.getEmail());
        result.put("firstName", user.getFirstName());
        result.put("lastName", user.getLastName());
        result.put("customerType", newCustomerType.name());

        return result;
    }

    @Override
    public Order updateOrder(Long id, OrderUpdateDTO orderUpdateDTO) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Porudžbina nije pronađena"));

        // Ako se status menja na CANCELLED, pošalji mejl
        if (!order.getOrderStatus().equals(OrderStatus.CANCELLED) && 
            orderUpdateDTO.getOrderStatus().equals(OrderStatus.CANCELLED)) {
            User user = order.getUser();
            emailService.sendOrderCancellationEmail(user.getEmail(), order);
            logger.info("Mejl o otkazivanju poslat za porudžbinu ID: {}", id);
        }

        order.setTotalPrice(orderUpdateDTO.getTotalPrice());
        order.setOrderStatus(orderUpdateDTO.getOrderStatus());

        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Map<String, Object> deleteOrder(Long id, HttpServletRequest request) throws IOException {
        logger.info("Brišem porudžbinu sa ID: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Porudžbina nije pronađena za ID: {}", id);
                    return new RuntimeException("Porudžbina nije pronađena");
                });

        User user = order.getUser();
        Long userId = user.getId();

        // Vraćanje količina na stanje samo ako porudžbina nije isporučena
        if (order.getOrderStatus() != OrderStatus.DELIVERED) {
            for (OrderItem orderItem : order.getOrderItems()) {
                Product product = orderItem.getProduct();
                product.setStockQuantity(product.getStockQuantity() + orderItem.getQuantity());
                productRepository.save(product);

                // Sinhronizuj sa Elasticsearch-om
                productSyncService.updateProductInElasticsearch(product);
            }
        }

        // Brisanje porudžbine
        orderRepository.delete(order);

        // Ažuriranje CustomerType na osnovu novog broja porudžbina
        long orderCount = orderRepository.countByUser(user);
        CustomerType newCustomerType = determineCustomerType(orderCount);
        user.setCustomerType(newCustomerType);
        userRepository.save(user);

        logger.info("CustomerType korisnika {} ažuriran na: {}", userId, newCustomerType);

        // Generisanje novog tokena sa ažuriranim CustomerType
        Algorithm algorithm = Algorithm.HMAC256("secretKey".getBytes());
        String newJwtToken = JWT.create()
                .withSubject(user.getEmail())
                .withExpiresAt(new Date(System.currentTimeMillis() + 15 * 60 * 1000))
                .withIssuer(request.getRequestURI())
                .withClaim("roles", user.getRole() != null ? List.of("ROLE_" + user.getRole().name()) : List.of("ROLE_USER"))
                .withClaim("firstName", user.getFirstName())
                .withClaim("lastName", user.getLastName())
                .withClaim("customerType", newCustomerType.name())
                .sign(algorithm);

        Map<String, Object> tokens = new HashMap<>();
        tokens.put("jwtToken", newJwtToken);
        tokens.put("roles", user.getRole() != null ? user.getRole().name() : "USER");
        tokens.put("email", user.getEmail());
        tokens.put("firstName", user.getFirstName());
        tokens.put("lastName", user.getLastName());
        tokens.put("customerType", newCustomerType.name());

        logger.info("Token uspešno osvežen za korisnika {} sa novim CustomerType: {}", userId, newCustomerType);
        return tokens;
    }

    @Override
    public BigDecimal calculateTotalPrice(Cart cart, BigDecimal discount) {
        BigDecimal totalPrice = cart.getCartItems().stream()
            .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalPrice.subtract(totalPrice.multiply(discount));
    }

    @Override
    public void refreshTokenAfterOrder(Long userId, HttpServletRequest request, HttpServletResponse response, CustomerType customerType) throws IOException {
        throw new UnsupportedOperationException("This method is deprecated");
    }

    @Override
    public Long getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    @Override
    public String getEmailByUserId(Long userId) {
        return userRepository.findById(userId)
                .map(User::getEmail)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }

    private CustomerType determineCustomerType(long orderCount) {
        if (orderCount >= 5) {
            return CustomerType.VIP; // 30% popusta
        } else if (orderCount >= 3) {
            return CustomerType.PLATINUM; // 20% popusta
        } else if (orderCount >= 1) {
            return CustomerType.PREMIUM; // 10% popusta
        } else {
            return CustomerType.REGULAR; // Nema popusta
        }
    }

    private BigDecimal getDiscountForUser(User user) {
        switch (user.getCustomerType()) {
            case VIP:
                return BigDecimal.valueOf(0.30); // 30% popust
            case PLATINUM:
                return BigDecimal.valueOf(0.20); // 20% popust
            case PREMIUM:
                return BigDecimal.valueOf(0.10); // 10% popust
            default:
                return BigDecimal.ZERO; // Nema popusta za REGULAR korisnike
        }
    }

    @Override
    public OrderDTO mapToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setUserId(order.getUser().getId());
        dto.setOrderItems(order.getOrderItems());
        dto.setAddress(order.getAddress());
        dto.setCreatedAt(order.getCreatedAt());
        return dto;
    }
}