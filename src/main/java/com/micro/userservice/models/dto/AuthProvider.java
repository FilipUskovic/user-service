package com.micro.userservice.models.dto;

import lombok.Getter;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

@Getter
public enum AuthProvider {

    LOCAL("local"),
    GOOGLE("google"),
    GITHUB("github");

    private final String ProviderName;

    AuthProvider(String ProviderName) {
        this.ProviderName = ProviderName;
    }

    public static AuthProvider fromString(String ProviderName) {
        try{
            return AuthProvider.valueOf(ProviderName.toUpperCase());
        }catch (IllegalArgumentException e) {
            throw new OAuth2AuthenticationException(new OAuth2Error
                    ("invalid_provider", ProviderName, "Invalid provider"));
        }
    }
}
