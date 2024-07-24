package com.authenticator.Google.Authenticator.service;

import com.google.zxing.WriterException;
import org.apache.coyote.BadRequestException;

import java.io.IOException;

public interface GoogleAuthenticatorService {

    String generateSecretKey();

    String getTOTPCode(String secretKey);

    String getGoogleAuthenticatorBarCode(String secretKey, String account, String issuer);

    byte[] createQRCode(String barCodeData) throws WriterException, IOException;

    String validateTOTPCode(String secretKey, String otp) throws BadRequestException;
}
