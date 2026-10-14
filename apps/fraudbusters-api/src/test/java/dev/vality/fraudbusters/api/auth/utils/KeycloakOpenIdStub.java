package dev.vality.fraudbusters.api.auth.utils;

import dev.vality.fraudbusters.api.testutil.GenerateSelfSigned;
import dev.vality.fraudbusters.api.testutil.PublicKeyUtil;
import lombok.SneakyThrows;

import java.security.KeyPair;
import java.util.Base64;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class KeycloakOpenIdStub {

    private final String keycloakRealm;
    private final String issuer;
    private final String openidConfig;
    private final String jwkConfig;
    private final JwtTokenBuilder jwtTokenBuilder;

    @SneakyThrows
    public KeycloakOpenIdStub(String keycloakAuthServerUrl, String keycloakRealm, JwtTokenBuilder jwtTokenBuilder) {
        this.keycloakRealm = keycloakRealm;
        this.jwtTokenBuilder = jwtTokenBuilder;
        this.issuer = keycloakAuthServerUrl + "/realms/" + keycloakRealm;
        this.openidConfig = "{\n" +
                            "  \"issuer\": \"" + issuer + "\",\n" +
                            "  \"authorization_endpoint\": \"" + keycloakAuthServerUrl + "/realms/" + keycloakRealm +
                            "/protocol/openid-connect/auth\",\n" +
                            "  \"token_endpoint\": \"" + keycloakAuthServerUrl + "/realms/" + keycloakRealm +
                            "/protocol/openid-connect/token\",\n" +
                            "  \"token_introspection_endpoint\": \"" + keycloakAuthServerUrl + "/realms/" +
                            keycloakRealm +
                            "/protocol/openid-connect/token/introspect\",\n" +
                            "  \"userinfo_endpoint\": \"" + keycloakAuthServerUrl + "/realms/" + keycloakRealm +
                            "/protocol/openid-connect/userinfo\",\n" +
                            "  \"end_session_endpoint\": \"" + keycloakAuthServerUrl + "/realms/" + keycloakRealm +
                            "/protocol/openid-connect/logout\",\n" +
                            "  \"jwks_uri\": \"" + keycloakAuthServerUrl + "/realms/" + keycloakRealm +
                            "/protocol/openid-connect/certs\",\n" +
                            "  \"check_session_iframe\": \"" + keycloakAuthServerUrl + "/realms/" + keycloakRealm +
                            "/protocol/openid-connect/login-status-iframe.html\",\n" +
                            "  \"registration_endpoint\": \"" + keycloakAuthServerUrl + "/realms/" + keycloakRealm +
                            "/clients-registrations/openid-connect\",\n" +
                            "  \"introspection_endpoint\": \"" + keycloakAuthServerUrl + "/realms/" + keycloakRealm +
                            "/protocol/openid-connect/token/introspect\"\n" +
                            "}";
        this.jwkConfig = """
                {
                    "keys": [
                        {
                            "alg": "RS256",
                            "e": "%s",
                            "kid": "BZdHlAdlt3F1XatlYtZg3f1Cfpk5IpEINuIgviUW59s",
                            "kty": "RSA",
                            "n": "%s",
                            "use": "sig",
                            "x5c": [
                                "%s"
                            ],
                            "x5t": "9APiqOME1mVmyv8hak6HB_PTezA",
                            "x5t#S256": "kweH93DnMHKD_NrAZF-mgpAM3Njv_8-oxaDAzki4t48"
                        }
                    ]
                }
                """.formatted(
                PublicKeyUtil.getExponent(jwtTokenBuilder.getPublicKey()),
                PublicKeyUtil.getModulus(jwtTokenBuilder.getPublicKey()),
                Base64.getEncoder().encodeToString(
                        GenerateSelfSigned.generateCertificate(new KeyPair(jwtTokenBuilder.getPublicKey(),
                                jwtTokenBuilder.getPrivateKey())).getEncoded()));
    }

    public void givenStub() {
        stubFor(get(urlEqualTo(String.format("/auth/realms/%s/.well-known/openid-configuration", keycloakRealm)))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(openidConfig)
                )
        );
        stubFor(get(urlEqualTo(String.format("/auth/realms/%s/protocol/openid-connect/certs", keycloakRealm)))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(jwkConfig)
                ));
    }

    public String generateJwt(String... roles) {
        return jwtTokenBuilder.generateJwtWithRoles(issuer, roles);
    }

    public String generateJwt(long iat, long exp, String... roles) {
        return jwtTokenBuilder.generateJwtWithRoles(iat, exp, issuer, roles);
    }
}
