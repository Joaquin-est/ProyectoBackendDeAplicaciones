package com.patitosrl.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthDtos {

    public record LoginRequest(String username, String password) { }

    public record RefreshRequest(String refreshToken) { }

    public record LogoutRequest(String refreshToken) { }

    public record TokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("refresh_token") String refreshToken,
            @JsonProperty("expires_in") Integer expiresIn,
            @JsonProperty("refresh_expires_in") Integer refreshExpiresIn,
            @JsonProperty("token_type") String tokenType,
            String scope
    ) { }
}