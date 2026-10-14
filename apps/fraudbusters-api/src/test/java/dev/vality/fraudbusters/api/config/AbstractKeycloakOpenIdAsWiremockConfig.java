package dev.vality.fraudbusters.api.config;

import dev.vality.fraudbusters.api.FraudbustersApiApplication;
import dev.vality.fraudbusters.api.auth.utils.KeycloakOpenIdStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.wiremock.spring.EnableWireMock;

@SuppressWarnings("LineLength")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {FraudbustersApiApplication.class},
        properties = {
            "spring.security.oauth2.resourceserver.url=${wiremock.server.baseUrl}",
            "spring.security.oauth2.resourceserver.jwt.issuer-uri=${wiremock.server.baseUrl}/auth/realms/" +
                    "${spring.security.oauth2.resourceserver.jwt.realm}"})
@AutoConfigureMockMvc
@EnableWireMock
@ExtendWith(SpringExtension.class)
public abstract class AbstractKeycloakOpenIdAsWiremockConfig {

    @Autowired
    private KeycloakOpenIdStub keycloakOpenIdStub;

    @BeforeEach
    public void setUp(@Autowired KeycloakOpenIdStub keycloakOpenIdStub) throws Exception {
        keycloakOpenIdStub.givenStub();
    }

    protected String generateSimpleJwt() {
        return keycloakOpenIdStub.generateJwt();
    }
}
