package dev.vality.fraudo.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class HistoryFunction {

    public static final String COUNT = "count";
    public static final String COUNT_SUCCESS = "countSuccess";
    public static final String COUNT_PENDING = "countPending";
    public static final String COUNT_ERROR = "countError";
    public static final String COUNT_CHARGEBACK = "countChargeback";
    public static final String COUNT_REFUND = "countRefund";
    public static final String SUM = "sum";
    public static final String SUM_SUCCESS = "sumSuccess";
    public static final String SUM_ERROR = "sumError";
    public static final String SUM_CHARGEBACK = "sumChargeback";
    public static final String SUM_REFUND = "sumRefund";
    public static final String UNIQUE = "unique";

    public static final String COUNT_PAYOUTS = "countPayouts";
    public static final String COUNT_PAYOUTS_SUCCESS = "countPayoutsSuccess";
    public static final String COUNT_PAYOUTS_ERROR = "countPayoutsError";
    public static final String SUM_PAYOUTS = "sumPayouts";
    public static final String SUM_PAYOUTS_SUCCESS = "sumPayoutsSuccess";
    public static final String SUM_PAYOUTS_ERROR = "sumPayoutsError";
    public static final String UNIQUE_PAYOUTS = "uniquePayouts";
}
