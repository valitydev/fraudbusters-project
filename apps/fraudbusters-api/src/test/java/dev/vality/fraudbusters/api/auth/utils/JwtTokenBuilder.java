package dev.vality.fraudbusters.api.auth.utils;

import io.jsonwebtoken.Jwts;
import lombok.SneakyThrows;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.UUID;

public class JwtTokenBuilder {

    public static final String DEFAULT_USERNAME = "Darth Vader";

    public static final String DEFAULT_EMAIL = "darkside-the-best@mail.com";

    private final String userId;

    private final String username;

    private final String email;

    private final PrivateKey privateKey;

    private final PublicKey publicKey;

    public JwtTokenBuilder(KeyPair keyPair) {
        this(UUID.randomUUID().toString(), DEFAULT_USERNAME, DEFAULT_EMAIL, keyPair.getPrivate(), keyPair.getPublic());
    }

    public JwtTokenBuilder(String userId, String username, String email, PrivateKey privateKey, PublicKey publicKey) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public String generateJwtWithRoles(String issuer, String... roles) {
        long iat = Instant.now().getEpochSecond();
        long exp = iat + 60 * 10;
        return generateJwtWithRoles(iat, exp, issuer, roles);
    }

    public String generateJwtWithRoles(long iat, long exp, String issuer, String... roles) {
        return generateJwtWithRoles(privateKey, iat, exp, issuer, roles);
    }

    public String generateJwtWithRoles(PrivateKey privateKey, long iat, long exp, String issuer, String... roles) {
        String payload;
        try {
            payload = new JSONObject()
                    .put("jti", UUID.randomUUID().toString())
                    .put("exp", exp)
                    .put("nbf", 0L)
                    .put("iat", iat)
                    .put("iss", issuer)
                    .put("aud", "private-api")
                    .put("sub", userId)
                    .put("typ", "Bearer")
                    .put("azp", "private-api")
                    .put("resource_access", new JSONObject()
                            .put("common-api", new JSONObject()
                                    .put("roles", new JSONArray(roles))))
                    .put("preferred_username", username)
                    .put("email", email).toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return Jwts.builder()
                .content(payload)
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    @SneakyThrows
    public PublicKey getPublicKey() {
        return this.publicKey;
    }

    @SneakyThrows
    public PrivateKey getPrivateKey() {
        return this.privateKey;
    }

}
