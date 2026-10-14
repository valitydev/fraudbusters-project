package dev.vality.fraudbusters.management.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AnalyticsResultField {

    public static final String COUNT = "count";
    public static final String SUM = "sum";
    public static final String RATIO = "ratio";
    public static final String TEMPLATE = "template";
    public static final String STATUS = "status";
    public static final String RULE = "rule";
    public static final String TRUSTED_SCORE = "trusted";
    public static final String LOW_SCORE = "low";
    public static final String HIGH_SCORE = "high";
    public static final String FATAL_SCORE = "fatal";

}
