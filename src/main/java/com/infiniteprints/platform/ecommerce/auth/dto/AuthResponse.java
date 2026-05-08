package com.infiniteprints.platform.ecommerce.auth.dto;

import com.infiniteprints.platform.ecommerce.auth.entity.User;

public class AuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";

    public AuthResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() { return accessToken; }
    public String getTokenType() { return tokenType; }
}