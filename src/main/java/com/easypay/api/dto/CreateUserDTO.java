package com.easypay.api.dto;

import com.easypay.api.model.enums.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserDTO(

    @NotBlank(message = "Name is mandatory")
    String name,

    @NotBlank(message = "Email is mandatory")
    String email,

    @NotBlank(message = "Document is mandatory")
    String document,

    @NotBlank(message = "Password is mandatory")
    String password,

    @NotNull(message = "User Type is mandatory")
    UserType userType
){}
