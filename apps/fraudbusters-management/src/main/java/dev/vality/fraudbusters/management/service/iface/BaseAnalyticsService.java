package dev.vality.fraudbusters.management.service.iface;

import dev.vality.swag.fraudbusters.management.model.SplitUnit;

import java.util.List;
import java.util.Map;

public interface BaseAnalyticsService {

    Integer getBlockedFraudPaymentsCount(Map<String, String> params);

    Float getBlockedFraudPaymentsCountRatio(Map<String, String> params);

    Float getBlockedFraudPaymentsSum(Map<String, String> params);

    Integer getFraudPaymentsCount(Map<String, String> params);

    List<Map<String, String>> getFraudPaymentsResultsSummary(Map<String, String> params);

    List<Map<String, String>> getFraudPaymentsScoreSplitCountRatio(Map<String, String> params, SplitUnit splintUnit);

    List<String> getCurrencies();

}
