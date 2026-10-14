package dev.vality.fraudbusters.warehouse.query;

public class TestQuery {

    public static final String QUERY_METRIC_RECURRENT = " SELECT" +
            "        t," +
            "        shopId," +
            "        currency," +
            "        sm_ref * 100 / sm_all as metric" +
            "    FROM" +
            "(" +
            "        SELECT" +
            "            timestamp AS t," +
            "            shopId," +
            "            currency," +
            "            sum(amount / 100) as sm_ref" +
            "        FROM analytic.events_sink_refund" +
            "        WHERE" +
            "        '2019-12-05' <= timestamp and '2019-12-12' >= timestamp " +
            "            and status = 'succeeded'" +
            "            and shopId != 'TEST'" +
            "        GROUP BY" +
            "            t," +
            "            currency," +
            "            shopId" +
            ")    " +
            "ANY LEFT JOIN " +
            "(" +
            "        SELECT" +
            "            timestamp AS t," +
            "            shopId," +
            "            currency," +
            "            sum(amount / 100) as sm_all" +
            "        FROM analytic.events_sink" +
            "        WHERE" +
            "        '2019-12-05' <= timestamp and '2019-12-12' >= timestamp " +
            "            and status = 'captured'" +
            "            and shopId != 'TEST'" +
            "        GROUP BY" +
            "            t," +
            "            currency," +
            "            shopId" +
            "        HAVING (sm_all > 1 and currency = 'RUB') OR(sm_all > 1 and currency = 'USD') " +
            "OR (sm_all > 1 and currency = 'EUR')" +
            ") USING " +
            "        t," +
            "        shopId," +
            "        currency" +
            "    WHERE metric > 10" +
            "    ORDER BY t DESC";

    public static final String QUERY_METRIC_RECURRENT_WITH_PARAMS = " SELECT" +
            "        t," +
            "        shopId," +
            "        currency," +
            "        sm_ref * 100 / sm_all as metric" +
            "    FROM" +
            "(" +
            "        SELECT" +
            "            timestamp AS t," +
            "            shopId," +
            "            currency," +
            "            sum(amount / 100) as sm_ref" +
            "        FROM analytic.events_sink_refund" +
            "        WHERE" +
            "        :dateFrom <= timestamp and :dateTo >= timestamp " +
            "            and status = 'succeeded'" +
            "            and shopId != 'TEST'" +
            "        GROUP BY" +
            "            t," +
            "            currency," +
            "            shopId" +
            ")    " +
            "ANY LEFT JOIN " +
            "(" +
            "        SELECT" +
            "            timestamp AS t," +
            "            shopId," +
            "            currency," +
            "            sum(amount / 100) as sm_all" +
            "        FROM analytic.events_sink" +
            "        WHERE" +
            "        :dateFrom  <= timestamp and :dateTo >= timestamp " +
            "            and status = 'captured'" +
            "            and shopId != 'TEST'" +
            "        GROUP BY" +
            "            t," +
            "            currency," +
            "            shopId" +
            "        HAVING (sm_all > 1 and currency = 'RUB') OR(sm_all > 1 and currency = 'USD') " +
            "OR (sm_all > 1 and currency = 'EUR')" +
            ") USING " +
            "        t," +
            "        shopId," +
            "        currency" +
            "    WHERE metric > 10" +
            "    ORDER BY t DESC";

}
