package dev.vality.fraudbusters.fraud.resolver;

import dev.vality.fraudbusters.constant.ClickhouseUtilsValue;
import dev.vality.fraudbusters.fraud.payment.CountryByIpResolver;
import dev.vality.fraudo.resolver.CountryResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Resolves a country from the ip field through geo lookup or takes the bank country field as is. */
@Slf4j
@RequiredArgsConstructor
public class CountryResolverImpl<U> implements CountryResolver<U> {

    private final CountryByIpResolver countryByIpResolver;
    private final U ipField;
    private final U bankCountryField;

    @Override
    public String resolveCountry(U checkedField, String fieldValue) {
        String location = null;
        if (ipField.equals(checkedField)) {
            location = countryByIpResolver.resolveCountry(fieldValue);
        } else if (bankCountryField.equals(checkedField)) {
            location = fieldValue;
        }
        if (location == null) {
            return ClickhouseUtilsValue.UNKNOWN;
        }
        log.debug("CountryResolverImpl resolve ip: {} country_id: {}", fieldValue, location);
        return location;
    }

    public boolean isSupported(U field) {
        return ipField.equals(field) || bankCountryField.equals(field);
    }

}
