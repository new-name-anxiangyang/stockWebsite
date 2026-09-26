package com.stock.Dto;

public record LoginResponse(String accessToken, String tokenType, Long expiresIn) {
}
