package com.example.hotel_booking_ai.service;

import com.example.hotel_booking_ai.dto.AuthResponse;
import com.example.hotel_booking_ai.dto.LoginRequest;
import com.example.hotel_booking_ai.dto.ManagerCreateRequest;
import com.example.hotel_booking_ai.dto.RegisterRequest;
import com.example.hotel_booking_ai.entity.Customer;
import com.example.hotel_booking_ai.repository.CustomerRepository;
import com.example.hotel_booking_ai.security.JwtUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public CustomerService(
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder,
            JwtUtils jwtUtils) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    public Customer register(RegisterRequest request) {
        if (customerRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        Customer customer = new Customer();
        customer.setName(request.name());
        customer.setEmail(request.email());
        customer.setPassword(passwordEncoder.encode(request.password()));
        customer.setRole("CUSTOMER");

        return customerRepository.save(customer);
    }

    public AuthResponse login(LoginRequest request) {
        Customer customer = customerRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), customer.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtUtils.generateToken(
                customer.getId(),
                customer.getEmail(),
                customer.getRole(),
                customer.getHotelId()
        );

        return new AuthResponse(
                token,
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getRole(),
                customer.getHotelId()
        );
    }

    public Customer createManager(ManagerCreateRequest request) {
        if (customerRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        Customer manager = new Customer();
        manager.setName(request.name());
        manager.setEmail(request.email());
        manager.setPassword(passwordEncoder.encode(request.password()));
        manager.setRole("MANAGER");
        manager.setHotelId(request.hotelId());

        return customerRepository.save(manager);
    }

    public Customer getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + email));
    }
}
