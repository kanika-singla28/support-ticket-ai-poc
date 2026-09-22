package com.supportticket.poc.dto;

public record ApiResponse<T>(
        T data,
        Object errors
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, null);
    }
}
