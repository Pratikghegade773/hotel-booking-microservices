package com.example.userservice.controller;

import com.example.common.constant.SecurityHeaders;
import com.example.userservice.dto.ManagerCreateRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final CustomerService customerService;

    public UserController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/me")
    public UserResponse getMe(@RequestHeader(SecurityHeaders.USER_EMAIL) String email) {
        return customerService.getCustomerByEmail(email);
    }

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id) {
        return customerService.getCustomerById(id);
    }

    @PostMapping("/managers")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createManager(@Valid @RequestBody ManagerCreateRequest request) {
        return customerService.createManager(request);
    }

    @GetMapping("/managers/by-hotel/{hotelId}")
    public List<UserResponse> getManagersByHotel(@PathVariable Long hotelId) {
        return customerService.getManagersByHotel(hotelId);
    }
}
