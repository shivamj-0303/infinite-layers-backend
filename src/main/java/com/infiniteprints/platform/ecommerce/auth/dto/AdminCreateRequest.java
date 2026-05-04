package com.infiniteprints.platform.ecommerce.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCreateRequest {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
}
