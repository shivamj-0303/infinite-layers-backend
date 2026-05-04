package com.infiniteprints.platform.ecommerce.auth.dto;

import com.infiniteprints.platform.ecommerce.auth.entity.User;

public class AuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private User user;  // Include user data

    public AuthResponse() {}
    public AuthResponse(String accessToken) { this.accessToken = accessToken; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
