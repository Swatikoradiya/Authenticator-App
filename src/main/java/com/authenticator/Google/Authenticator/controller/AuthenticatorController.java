package com.authenticator.Google.Authenticator.controller;

import com.authenticator.Google.Authenticator.dto.DataResponse;
import com.authenticator.Google.Authenticator.dto.Response;
import com.authenticator.Google.Authenticator.service.GoogleAuthenticatorService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/authenticator")
public class AuthenticatorController {

    private final GoogleAuthenticatorService authenticatorService;

    public AuthenticatorController(GoogleAuthenticatorService authenticatorService) {
        this.authenticatorService = authenticatorService;
    }

    @GetMapping("/generateSecretKey")
    public DataResponse<String> generateSecretKey() {
        DataResponse<String> dataResponse = new DataResponse<>();
        try {
            dataResponse.setData(authenticatorService.generateSecretKey());
            dataResponse.setStatus(Response.getSuccessResponse("Success"));
        } catch (Exception e) {
            e.printStackTrace();
            dataResponse.setStatus(Response.getErrorResponse(e.getMessage()));

        }
        return dataResponse;
    }

    @GetMapping(value = "/getQRCode", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] createQRCode(@RequestParam String secretKey, @RequestParam String email, @RequestParam String companyName) {
        String barcodeUrl = authenticatorService.getGoogleAuthenticatorBarCode(secretKey, email, companyName);
        try {
            return authenticatorService.createQRCode(barcodeUrl);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/checkTOTP")
    public DataResponse<String> checkTOTPCode(@RequestParam String code, @RequestParam String secretKey) {
        DataResponse<String> dataResponse = new DataResponse<>();
        try {
            dataResponse.setData(authenticatorService.validateTOTPCode(secretKey, code));
            dataResponse.setStatus(Response.getSuccessResponse("Success"));
        } catch (Exception e) {
            e.printStackTrace();
            dataResponse.setStatus(Response.getErrorResponse(e.getMessage()));

        }
        return dataResponse;
    }
}
