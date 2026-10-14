package dev.vality.fraudbusters.notificator.config;

import dev.vality.fraudbusters.notificator.service.VaultSecretService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.vault.config.EnvironmentVaultConfiguration;

@Configuration
@Import(EnvironmentVaultConfiguration.class)
public class VaultConfig {

    @Bean
    public VaultSecretService vaultSecretService(EnvironmentVaultConfiguration environmentVaultConfiguration) {
        return new VaultSecretService(environmentVaultConfiguration.vaultTemplate());
    }
}
