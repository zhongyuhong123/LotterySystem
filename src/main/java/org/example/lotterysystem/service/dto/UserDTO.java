package org.example.lotterysystem.service.dto;

import lombok.Data;

import org.example.lotterysystem.service.enums.UserIdentityEnum;

@Data
public class UserDTO {
    private Long userId;
    private String userName;
    private String email;
    private String phoneNumber;
    private UserIdentityEnum identity;
}
