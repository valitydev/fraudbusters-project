package dev.vality.fraudbusters.management.config;

public final class FraudAuthorization {

    private static final String FRAUD_OFFICER_ROLE = "fraud-officer";
    private static final String FRAUD_MONITORING_ROLE = "fraud-monitoring";

    public static final String FRAUD_OFFICER_ONLY = "hasRole('" + FRAUD_OFFICER_ROLE + "')";
    public static final String FRAUD_MONITORING_OR_OFFICER =
            "hasAnyRole('" + FRAUD_MONITORING_ROLE + "', '" + FRAUD_OFFICER_ROLE + "')";

    private FraudAuthorization() {
    }
}
