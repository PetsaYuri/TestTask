package com.TestTask.Users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.sql.Date;

public record UserDTO(
        Long id,
        @NotNull @NotBlank @Email String email,
        @NotNull @NotBlank String firstName,
        @NotNull @NotBlank String lastName,
        @NotNull @Past Date birthDate,
        String address,
        String phoneNumber) {
}
