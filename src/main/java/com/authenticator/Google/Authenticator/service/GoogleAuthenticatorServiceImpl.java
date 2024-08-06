package com.authenticator.Google.Authenticator.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Service
@Slf4j
public class GoogleAuthenticatorServiceImpl implements GoogleAuthenticatorService {

    public static final int CODE_LENGTH = 8;

    @Override
    public String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        Base32 base32 = new Base32();
        return base32.encodeToString(bytes);
    }

    @Override
    public String validateTOTPCode(String secretKey, String otp) throws BadRequestException {
        String lastTOTPCode = getTOTPCode(secretKey);

        if (!otp.equals(lastTOTPCode)) {
            throw new BadRequestException("Invalid Code");
        }
        return "Logged in successfully";
    }

    @Override
    public byte[] createQRCode(String barcodeText) throws WriterException, IOException {
        System.out.println("Barcode URL : " + barcodeText);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, 300, 300);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }

    @Override
    public String getGoogleAuthenticatorBarCode(String secretKey, String account, String issuer) {
        try {
            return "otpauth://totp/"
                    + URLEncoder.encode(issuer + ":" + account, "UTF-8").replace("+", "%20")
                    + "?secret=" + URLEncoder.encode(secretKey, "UTF-8").replace("+", "%20")
                    + "&issuer=" + URLEncoder.encode(issuer, "UTF-8").replace("+", "%20")
                    + "&digits=" + URLEncoder.encode(String.valueOf(CODE_LENGTH), "UTF-8").replace("+", "%20")
                    + "&period=" + URLEncoder.encode("60", "UTF-8").replace("+", "%20")
                    + "&algorithm =" + URLEncoder.encode("HmacSHA1", "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String getTOTPCode(String secretKey) {
        Base32 base32 = new Base32();
        byte[] bytes = base32.decode(secretKey);
        String hexKey = Hex.encodeHexString(bytes);
        return getOTP(getStep(), hexKey);
    }

    private static long getStep() {
        return System.currentTimeMillis() / 30000L;
    }

    private static String getOTP(long step, String key) {
        String steps;
        for (steps = Long.toHexString(step).toUpperCase(); steps.length() < 16; steps = "0" + steps) {
        }

        byte[] msg = hexStr2Bytes(steps);
        byte[] k = hexStr2Bytes(key);
        byte[] hash = hmac_sha1(k, msg);
        int offset = hash[hash.length - 1] & 15;
        int binary = (hash[offset] & 127) << 24 | (hash[offset + 1] & 255) << 16 | (hash[offset + 2] & 255) << 8 | hash[offset + 3] & 255;
        int otp = binary % Integer.parseInt("1" + "0".repeat(Math.max(0, CODE_LENGTH)));

        String result;
        for (result = Integer.toString(otp); result.length() < CODE_LENGTH; result = "0" + result) {
        }

        return result;
    }

    private static byte[] hexStr2Bytes(String hex) {
        byte[] bArray = (new BigInteger("10" + hex, 16)).toByteArray();
        byte[] ret = new byte[bArray.length - 1];
        System.arraycopy(bArray, 1, ret, 0, ret.length);
        return ret;
    }

    private static byte[] hmac_sha1(byte[] keyBytes, byte[] text) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA1");
            SecretKeySpec macKey = new SecretKeySpec(keyBytes, "RAW");
            hmac.init(macKey);
            return hmac.doFinal(text);
        } catch (GeneralSecurityException var4) {
            GeneralSecurityException gse = var4;
            throw new UndeclaredThrowableException(gse);
        }
    }
}
