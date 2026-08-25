package com.example.chatapp.DTO.signUp;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @deprecated kept only for backward compatibility with older responses.
 * New code should use {@link com.example.chatapp.DTO.common.ApiResponse}.
 */
@Deprecated
@Data
@AllArgsConstructor
public class ApiResponse {
    private boolean success;
    private String message;
}
