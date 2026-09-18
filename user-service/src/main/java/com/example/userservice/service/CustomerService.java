package com.example.userservice.service;

import com.example.userservice.dto.*;
import com.example.userservice.entity.Customer;
import com.example.userservice.repository.CustomerRepository;
import com.example.userservice.security.JwtUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public CustomerService(CustomerRepository customerRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    public UserResponse register(RegisterRequest request) {
        if (customerRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        Customer customer = new Customer();
        customer.setName(request.name());
        customer.setEmail(request.email());
        customer.setPassword(passwordEncoder.encode(request.password()));
        String role = (request.role() != null && !request.role().isBlank()) ? request.role().toUpperCase() : "CUSTOMER";
        customer.setRole(role);

        Customer saved = customerRepository.save(customer);
        return new UserResponse(saved.getId(), saved.getName(), saved.getEmail(), saved.getRole(), saved.getHotelId());
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

    public UserResponse createManager(ManagerCreateRequest request) {
        if (customerRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        Customer manager = new Customer();
        manager.setName(request.name());
        manager.setEmail(request.email());
        manager.setPassword(passwordEncoder.encode(request.password()));
        manager.setRole("MANAGER");
        manager.setHotelId(request.hotelId());

        Customer saved = customerRepository.save(manager);
        return new UserResponse(saved.getId(), saved.getName(), saved.getEmail(), saved.getRole(), saved.getHotelId());
    }

    public UserResponse getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + id));
        return new UserResponse(customer.getId(), customer.getName(), customer.getEmail(), customer.getRole(), customer.getHotelId());
    }

    public UserResponse getCustomerByEmail(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with email: " + email));
        return new UserResponse(customer.getId(), customer.getName(), customer.getEmail(), customer.getRole(), customer.getHotelId());
    }

    public List<UserResponse> getManagersByHotel(Long hotelId) {
        return customerRepository.findByHotelId(hotelId).stream()
                .map(m -> new UserResponse(m.getId(), m.getName(), m.getEmail(), m.getRole(), m.getHotelId()))
                .toList();
    }
}
