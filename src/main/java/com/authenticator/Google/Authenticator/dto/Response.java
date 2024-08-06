package com.authenticator.Google.Authenticator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response {

    private HttpStatus status;
    private int code;
    private String description;

    public static Response getSuccessResponse(String message) {
        return new Response(HttpStatus.OK, HttpStatus.OK.value(), message);
    }

    public static Response getErrorResponse(String message) {
        return new Response(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.value(), message);
    }
}
