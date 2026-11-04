package dev.vality.fraudbusters.fraud.payment.resolver;

import dev.vality.fraudbusters.constant.ClickhouseUtilsValue;
import dev.vality.fraudbusters.fraud.constant.CheckedField;
import dev.vality.fraudbusters.fraud.payment.CountryByIpResolver;
import dev.vality.fraudo.resolver.CountryResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CountryResolverImpl implements CountryResolver<CheckedField> {

    private final CountryByIpResolver countryByIpResolver;

    @Override
    public String resolveCountry(CheckedField checkedField, String fieldValue) {
        String location = null;
        if (CheckedField.IP.equals(checkedField)) {
            location = countryByIpResolver.resolveCountry(fieldValue);
        } else if (CheckedField.COUNTRY_BANK.equals(checkedField)) {
            location = fieldValue;
        }
        if (location == null) {
            return ClickhouseUtilsValue.UNKNOWN;
        }
        log.debug("CountryResolverImpl resolve ip: {} country_id: {}", fieldValue, location);
        return location;
    }

}
