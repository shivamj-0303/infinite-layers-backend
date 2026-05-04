package com.infiniteprints.platform.ecommerce.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for admin creation response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminCreateResponse {
    private Object id;
    private String email;
    private String firstName;
    private String lastName;
    private String message;
}
