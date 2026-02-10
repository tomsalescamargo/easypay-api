package com.easypay.api.controller;

import com.easypay.api.dto.CreateUserDTO;
import com.easypay.api.model.User;
import com.easypay.api.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<User> createUser(
            @Valid @RequestBody CreateUserDTO createUserDTO
    ) {
        User savedUser = service.createUser(createUserDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedUser);
    }
}
