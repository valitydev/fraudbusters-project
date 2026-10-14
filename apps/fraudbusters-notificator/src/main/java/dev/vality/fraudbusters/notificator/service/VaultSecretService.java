package dev.vality.fraudbusters.notificator.service;

import dev.vality.fraudbusters.notificator.exception.SecretPathNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.vault.core.SecretNotFoundException;
import org.springframework.vault.core.VaultTemplate;

@Component
@RequiredArgsConstructor
public class VaultSecretService {

    public static final String TOKEN = "token";
    public static final String TELEGRAM = "telegram";

    private final VaultTemplate vaultTemplate;

    @Value("${spring.application.name}")
    private String applicationName;

    public String getBotToken() throws SecretPathNotFoundException {
        var map = vaultTemplate.opsForVersionedKeyValue(applicationName).get(TELEGRAM);
        if (map == null || map.getData() == null || map.getData().get(TOKEN) == null) {
            throw new SecretNotFoundException(TELEGRAM, TOKEN);
        }
        return map.getData().get(TOKEN).toString();
    }

}