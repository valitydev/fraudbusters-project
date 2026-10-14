package dev.vality.fraudbusters.api.converter;

import dev.vality.damsel.fraudbusters.ProviderInfo;
import dev.vality.swag.fraudbusters.model.Provider;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ProviderToInternalDtoConverter
        implements Converter<Provider, ProviderInfo> {

    @Override
    public ProviderInfo convert(Provider provider) {
        return new ProviderInfo()
                .setProviderId(provider.getId())
                .setCountry(provider.getCountry())
                .setTerminalId(provider.getTerminalId());
    }

}
