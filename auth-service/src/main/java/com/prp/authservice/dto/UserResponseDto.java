package com.prp.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data  // generates getters, setters, toString, equals, hashCode
@NoArgsConstructor
@AllArgsConstructor

public class UserResponseDto {
    private String id;
    private String fullName;
    private String phoneNumber;
    private String email;
    private boolean isActive;

    // Getters, Setters, Constructors
}
