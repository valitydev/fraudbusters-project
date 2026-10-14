package dev.vality.fraudbusters.management.converter;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

import static dev.vality.fraudbusters.management.constant.FraudPaymentQueryParam.*;

@Component
public class QueryParamsToAnalyticParamsConverter {

    private static final String ALL_MATCH_SYMBOL = "%";

    public Map<String, String> convert(String fromTime,
                                       String toTime,
                                       String currency,
                                       String merchantId,
                                       String shopId) {
        return Map.of(
                FROM, fromTime,
                TO, toTime,
                CURRENCY, currency,
                PARTY_ID, StringUtils.hasLength(merchantId) ? merchantId : ALL_MATCH_SYMBOL,
                SHOP_ID, StringUtils.hasLength(shopId) ? shopId : ALL_MATCH_SYMBOL);
    }
}
