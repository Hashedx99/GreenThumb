package com.greenthumb.shared.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Generic API response wrapper used across all endpoints.
 * <p>
 * Ensures a consistent response envelope with success flag,
 * message, optional data payload, and timestamp.
 * </p>
 *
 * @param <T> the type of the data payload
 * @author Hamza Ali
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /** Indicates whether the request was successful. */
    private final boolean success;

    /** Human-readable message describing the result. */
    private final String message;

    /** Optional data payload returned to the client. */
    private final T data;

    /** Timestamp of when the response was generated. */
    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Creates a successful response with a data payload.
     *
     * @param <T>     the type of data
     * @param message the success message
     * @param data    the data payload
     * @return a successful ApiResponse
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Creates a successful response without a data payload.
     *
     * @param <T>     the type parameter
     * @param message the success message
     * @return a successful ApiResponse with no data
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    /**
     * Creates an error response.
     *
     * @param <T>     the type parameter
     * @param message the error message
     * @return an error ApiResponse
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}
