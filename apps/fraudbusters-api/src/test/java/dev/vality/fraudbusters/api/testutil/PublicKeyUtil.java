package dev.vality.fraudbusters.api.testutil;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.math.BigInteger;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

@UtilityClass
public class PublicKeyUtil {

    @SneakyThrows
    public String getModulus(PublicKey publicKey) {
        BigInteger publicKeyModulus = ((RSAPublicKey) (publicKey)).getModulus();
        return Base64.getUrlEncoder().encodeToString(publicKeyModulus.toByteArray());
    }

    @SneakyThrows
    public String getExponent(PublicKey publicKey) {
        BigInteger publicKeyExponent = ((RSAPublicKey) (publicKey)).getPublicExponent();
        return Base64.getUrlEncoder().encodeToString(publicKeyExponent.toByteArray());
    }
}
