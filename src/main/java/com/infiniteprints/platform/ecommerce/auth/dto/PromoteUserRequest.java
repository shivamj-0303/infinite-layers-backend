package com.infiniteprints.platform.ecommerce.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for promoting or demoting users to/from admin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromoteUserRequest {
    private String email;
}
