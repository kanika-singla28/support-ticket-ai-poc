package com.supportticket.poc.dto;

import java.util.List;

public record ApiResponse<T>(
        T data,
        Object errors
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, null);
    }

    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>(null, List.of(message));
    }
}
