package com.authenticator.Google.Authenticator.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DataResponse<T> {

    T data;
    Response status;
}
