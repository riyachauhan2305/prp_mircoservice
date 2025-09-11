package com.prp.authservice.model;

import com.arangodb.springframework.annotation.Document;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.springframework.data.annotation.Id;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document("users")
public class User {

    @JsonIgnore
    private String key;

    @Id
    private String id;

    private String fullName;
    private String phoneNumber;
    private boolean verified;
    private String email;

    
    private String mpin;

    private boolean isActive;
    private String otp;
}
