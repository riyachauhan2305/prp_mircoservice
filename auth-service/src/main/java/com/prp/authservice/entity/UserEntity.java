package com.prp.authservice.entity;

import com.arangodb.springframework.annotation.Document;
import org.springframework.data.annotation.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document("users")
public class UserEntity {

    @Id
    private String id;

    private String fullName;
    private String phoneNumber;
    private String email;
    private boolean verified;
    private boolean isActive;
    private String mpin;
    private String otp;
}
