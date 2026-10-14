package dev.vality.fraudbusters.management.resource.analytics;

import tools.jackson.databind.json.JsonMapper;
import dev.vality.fraudbusters.management.TestObjectFactory;
import dev.vality.fraudbusters.management.controller.ErrorController;
import dev.vality.fraudbusters.management.converter.QueryParamsToAnalyticParamsConverter;
import dev.vality.fraudbusters.management.converter.RowListToFraudResultSummaryListConverter;
import dev.vality.fraudbusters.management.converter.RowListToRiskScoreOffsetCountRatioListConverter;
import dev.vality.fraudbusters.management.service.BaseAnalyticsServiceImpl;
import dev.vality.fraudbusters.management.service.ClickhouseTimeSplitService;
import dev.vality.fraudbusters.management.service.iface.BaseAnalyticsService;
import dev.vality.fraudbusters.management.service.iface.SqlTimeSplitService;
import dev.vality.fraudbusters.management.service.iface.WarehouseQueryService;
import dev.vality.fraudbusters.warehouse.Query;
import dev.vality.fraudbusters.warehouse.Result;
import dev.vality.fraudbusters.warehouse.Row;
import dev.vality.swag.fraudbusters.management.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static dev.vality.fraudbusters.management.constant.AnalyticsResultField.*;
import static dev.vality.swag.fraudbusters.management.model.SplitUnit.DAY;
import static dev.vality.swag.fraudbusters.management.model.SplitUnit.MONTH;
import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
class AnalyticsResourceTest {

    private static final String CURRENCY = "currency";
    private MockMvc mockMvc;

    private final JsonMapper objectMapper = JsonMapper.builder().build();

    @Mock
    private WarehouseQueryService warehouseQueryService;

    @BeforeEach
    void setUp() {
        SqlTimeSplitService sqlTimeSplitService = new ClickhouseTimeSplitService();
        BaseAnalyticsService baseAnalyticsService =
                new BaseAnalyticsServiceImpl(warehouseQueryService, sqlTimeSplitService);
        var resultSummaryListConverter = new RowListToFraudResultSummaryListConverter();
        var scoreOffsetCountRatioListConverter =
                new RowListToRiskScoreOffsetCountRatioListConverter(sqlTimeSplitService);
        var analyticsResource =
                new AnalyticsResource(
                        baseAnalyticsService,
                        resultSummaryListConverter,
                        scoreOffsetCountRatioListConverter,
                        new QueryParamsToAnalyticParamsConverter());
        this.mockMvc = MockMvcBuilders.standaloneSetup(analyticsResource, new ErrorController()).build();
    }

    @Test
    void getBlockedFraudPaymentsCountWithEmptyResult() throws Exception {
        when(warehouseQueryService.execute(any(Query.class)))
                .thenReturn(new Result());

        MvcResult result = mockMvc.perform(get("/analytics/fraud-payments/blocked/count")
                        .queryParam("fromTime", "2022-06-22T06:12:27Z")
                        .queryParam("toTime", "2016-06-30T06:12:27Z")
                        .queryParam(CURRENCY, "KZT")
                        .queryParam("merchantId", "1234")
                        .queryParam("shopId", "5678"))
                .andExpect(status().isOk())
                .andReturn();

        var countResponse = objectMapper.readValue(result.getResponse().getContentAsString(), CountResponse.class);

        assertEquals(0, countResponse.getCount());
    }

    @Test
    void getBlockedFraudPaymentsCountWithSuccessResult() throws Exception {
        int count = 1000;
        when(warehouseQueryService.execute(any(Query.class))).thenReturn(TestObjectFactory.testResult(COUNT, count));

        MvcResult result = mockMvc.perform(get("/analytics/fraud-payments/blocked/count")
                        .queryParam("fromTime", "2022-06-22T06:12:27Z")
                        .queryParam("toTime", "2016-06-30T06:12:27Z")
                        .queryParam(CURRENCY, "KZT")
                        .queryParam("merchantId", "1234")
                        .queryParam("shopId", "5678"))
                .andExpect(status().isOk())
                .andReturn();

        var countResponse = objectMapper.readValue(result.getResponse().getContentAsString(), CountResponse.class);

        assertEquals(count, countResponse.getCount());
    }

    @Test
    void getBlockedFraudPaymentsCountRatioWithEmptyResult() throws Exception {
        when(warehouseQueryService.execute(any(Query.class))).thenReturn(new Result());

        MvcResult result = mockMvc.perform(get("/analytics/fraud-payments/blocked/count/ratio")
                        .queryParam("fromTime", "2022-06-22T06:12:27Z")
                        .queryParam("toTime", "2016-06-30T06:12:27Z")
                        .queryParam(CURRENCY, "KZT")
                        .queryParam("merchantId", "1234")
                        .queryParam("shopId", "5678"))
                .andExpect(status().isOk())
                .andReturn();

        var ratioResponse = objectMapper.readValue(result.getResponse().getContentAsString(), RatioResponse.class);

        assertEquals(0.0f, ratioResponse.getRatio());
    }

    @Test
    void getBlockedFraudPaymentsCountRatio() throws Exception {
        float ratio = 1.456f;
        when(warehouseQueryService.execute(any(Query.class))).thenReturn(TestObjectFactory.testResult(RATIO, ratio));

        MvcResult result = mockMvc.perform(get("/analytics/fraud-payments/blocked/count/ratio")
                        .queryParam("fromTime", "2022-06-22T06:12:27Z")
                        .queryParam("toTime", "2016-06-30T06:12:27Z")
                        .queryParam(CURRENCY, "KZT")
                        .queryParam("merchantId", "1234")
                        .queryParam("shopId", "5678"))
                .andExpect(status().isOk())
                .andReturn();

        var ratioResponse = objectMapper.readValue(result.getResponse().getContentAsString(), RatioResponse.class);

        assertEquals(ratio, ratioResponse.getRatio());
    }

    @Test
    void getBlockedFraudPaymentsSumWithEmptyResult() throws Exception {
        when(warehouseQueryService.execute(any(Query.class))).thenReturn(new Result());

        MvcResult result = mockMvc.perform(get("/analytics/fraud-payments/blocked/sum")
                        .queryParam("fromTime", "2022-06-22T06:12:27Z")
                        .queryParam("toTime", "2016-06-30T06:12:27Z")
                        .queryParam(CURRENCY, "KZT")
                        .queryParam("merchantId", "1234")
                        .queryParam("shopId", "5678"))
                .andExpect(status().isOk())
                .andReturn();

        var sumResponse = objectMapper.readValue(result.getResponse().getContentAsString(), SumResponse.class);

        assertEquals(0.0f, sumResponse.getSum());
    }

    @Test
    void getBlockedFraudPaymentsSum() throws Exception {
        float sum = 1000F;
        when(warehouseQueryService.execute(any(Query.class))).thenReturn(TestObjectFactory.testResult(SUM, sum));

        MvcResult result = mockMvc.perform(get("/analytics/fraud-payments/blocked/sum")
                        .queryParam("fromTime", "2022-06-22T06:12:27Z")
                        .queryParam("toTime", "2016-06-30T06:12:27Z")
                        .queryParam(CURRENCY, "KZT")
                        .queryParam("merchantId", "1234")
                        .queryParam("shopId", "5678"))
                .andExpect(status().isOk())
                .andReturn();

        var sumResponse = objectMapper.readValue(result.getResponse().getContentAsString(), SumResponse.class);

        assertEquals(sum, sumResponse.getSum());
    }

    @Test
    void getFraudPaymentsCountWithEmptyResult() throws Exception {
        when(warehouseQueryService.execute(any(Query.class))).thenReturn(new Result());

        MvcResult result = mockMvc.perform(get("/analytics/fraud-payments/count")
                        .queryParam("fromTime", "2022-06-22T06:12:27Z")
                        .queryParam("toTime", "2016-06-30T06:12:27Z")
                        .queryParam(CURRENCY, "KZT")
                        .queryParam("merchantId", "1234")
                        .queryParam("shopId", "5678"))
                .andExpect(status().isOk())
                .andReturn();

        var countResponse = objectMapper.readValue(result.getResponse().getContentAsString(), CountResponse.class);

        assertEquals(0, countResponse.getCount());
    }

    @Test
    void getFraudPaymentsCount() throws Exception {
        int count = 1000;
        when(warehouseQueryService.execute(any(Query.class))).thenReturn(TestObjectFactory.testResult(COUNT, count));

        MvcResult result = mockMvc.perform(get("/analytics/fraud-payments/count")
                        .queryParam("fromTime", "2022-06-22T06:12:27Z")
                        .queryParam("toTime", "2016-06-30T06:12:27Z")
                        .queryParam(CURRENCY, "KZT")
                        .queryParam("merchantId", "1234")
                        .queryParam("shopId", "5678"))
                .andExpect(status().isOk())
                .andReturn();

        var countResponse = objectMapper.readValue(result.getResponse().getContentAsString(), CountResponse.class);

        assertEquals(count, countResponse.getCount());
    }

    @Test
    void getFraudPaymentsResultsSummaryWithEmptyResult() throws Exception {
        Row row = TestObjectFactory.testRow(Collections.emptyMap());
        Result result = TestObjectFactory.testResult(List.of(row));
        when(warehouseQueryService.execute(any(Query.class))).thenReturn(result);

        MvcResult mvcResult = mockMvc.perform(get("/analytics/fraud-payments/results/summary")
                        .queryParam("fromTime", "2022-06-22T06:12:27Z")
                        .queryParam("toTime", "2016-06-30T06:12:27Z")
                        .queryParam(CURRENCY, "KZT")
                        .queryParam("merchantId", "1234")
                        .queryParam("shopId", "5678"))
                .andExpect(status().isOk())
                .andReturn();

        String responseAsString = mvcResult.getResponse().getContentAsString();
        var response =
                objectMapper.readValue(responseAsString, FraudResultListSummaryResponse.class);

        assertTrue(response.getResult().isEmpty());
    }

    @Test
    void getFraudPaymentsResultsSummaryWithFailResult() throws Exception {
        Row row = TestObjectFactory.testRow(Map.of(TestObjectFactory.randomString(), TestObjectFactory.randomString()));
        Result result = TestObjectFactory.testResult(List.of(row));
        when(warehouseQueryService.execute(any(Query.class))).thenReturn(result);

        MvcResult mvcResult = mockMvc.perform(get("/analytics/fraud-payments/results/summary")
                        .queryParam("fromTime", "2022-06-22T06:12:27Z")
                        .queryParam("toTime", "2016-06-30T06:12:27Z")
                        .queryParam(CURRENCY, "KZT")
                        .queryParam("merchantId", "1234")
                        .queryParam("shopId", "5678"))
                .andExpect(status().isOk())
                .andReturn();

        String responseAsString = mvcResult.getResponse().getContentAsString();
        var response =
                objectMapper.readValue(responseAsString, FraudResultListSummaryResponse.class);

        FraudResultSummary fraudResultSummary = response.getResult().get(0);
        assertNull(fraudResultSummary.getCheckedRule());
        assertNull(fraudResultSummary.getStatus());
        assertNull(fraudResultSummary.getTemplate());
        assertEquals(-1, fraudResultSummary.getSummary().getCount());
        assertEquals(-1, fraudResultSummary.getSummary().getSum());
        assertEquals(-1, fraudResultSummary.getSummary().getRatio());
    }

    @Test
    void getFraudPaymentsResultsSummaryWithSuccessResult() throws Exception {
        Row firstRow = TestObjectFactory.testRow(TestObjectFactory.testSummaryRowFieldsMap());
        Row secondRow = TestObjectFactory.testRow(TestObjectFactory.testSummaryRowFieldsMap());
        List<Row> rows = List.of(firstRow, secondRow);
        Result result = TestObjectFactory.testResult(rows);
        when(warehouseQueryService.execute(any(Query.class))).thenReturn(result);

        MvcResult mvcResult = mockMvc.perform(get("/analytics/fraud-payments/results/summary")
                        .queryParam("fromTime", "2022-06-22T06:12:27Z")
                        .queryParam("toTime", "2016-06-30T06:12:27Z")
                        .queryParam(CURRENCY, "KZT")
                        .queryParam("merchantId", "1234")
                        .queryParam("shopId", "5678"))
                .andExpect(status().isOk())
                .andReturn();

        String responseAsString = mvcResult.getResponse().getContentAsString();
        var response =
                objectMapper.readValue(responseAsString, FraudResultListSummaryResponse.class);

        assertEquals(rows.size(), response.getResult().size());
        assertTrue(response.getResult().stream()
                .map(FraudResultSummary::getCheckedRule)
                .anyMatch(rule -> firstRow.getValues().get(RULE).equals(rule)));
        assertTrue(response.getResult().stream()
                .map(FraudResultSummary::getCheckedRule)
                .anyMatch(rule -> secondRow.getValues().get(RULE).equals(rule)));
        assertTrue(response.getResult().stream()
                .map(FraudResultSummary::getStatus)
                .anyMatch(status -> firstRow.getValues().get(STATUS).equals(status)));
        assertTrue(response.getResult().stream()
                .map(FraudResultSummary::getStatus)
                .anyMatch(status -> secondRow.getValues().get(STATUS).equals(status)));
        assertTrue(response.getResult().stream()
                .map(FraudResultSummary::getTemplate)
                .anyMatch(template -> firstRow.getValues().get(TEMPLATE).equals(template)));
        assertTrue(response.getResult().stream()
                .map(FraudResultSummary::getTemplate)
                .anyMatch(template -> secondRow.getValues().get(TEMPLATE).equals(template)));
        assertTrue(response.getResult().stream()
                .map(FraudResultSummary::getSummary)
                .map(Summary::getCount)
                .anyMatch(count -> firstRow.getValues().get(COUNT).equals(count.toString())));
        assertTrue(response.getResult().stream()
                .map(FraudResultSummary::getSummary)
                .map(Summary::getCount)
                .anyMatch(count -> secondRow.getValues().get(COUNT).equals(count.toString())));
        assertTrue(response.getResult().stream()
                .map(FraudResultSummary::getSummary)
                .map(Summary::getSum)
                .anyMatch(sum -> isEqualFloat(firstRow, SUM, sum)));
        assertTrue(response.getResult().stream()
                .map(FraudResultSummary::getSummary)
                .map(Summary::getSum)
                .anyMatch(sum -> isEqualFloat(secondRow, SUM, sum)));
        assertTrue(response.getResult().stream()
                .map(FraudResultSummary::getSummary)
                .map(Summary::getRatio)
                .anyMatch(ratio -> isEqualFloat(firstRow, RATIO, ratio)));
        assertTrue(response.getResult().stream()
                .map(FraudResultSummary::getSummary)
                .map(Summary::getRatio)
                .anyMatch(ratio -> isEqualFloat(secondRow, RATIO, ratio)));
    }

    @Test
    void getFraudPaymentsScoreSplitCountRatioWithEmptyResult() throws Exception {
        Row row = TestObjectFactory.testRow(Collections.emptyMap());
        Result result = TestObjectFactory.testResult(List.of(row));
        when(warehouseQueryService.execute(any(Query.class))).thenReturn(result);

        MvcResult mvcResult = mockMvc.perform(get("/analytics/fraud-payments/scores/split-count/ratio")
                        .queryParam("fromTime", "2022-06-22T06:12:27Z")
                        .queryParam("toTime", "2016-06-30T06:12:27Z")
                        .queryParam(CURRENCY, "KZT")
                        .queryParam("merchantId", "1234")
                        .queryParam("shopId", "5678")
                        .queryParam("splitUnit", DAY.getValue()))
                .andExpect(status().isOk())
                .andReturn();

        String responseAsString = mvcResult.getResponse().getContentAsString();
        var response =
                objectMapper.readValue(responseAsString, SplitRiskScoreCountRatioResponse.class);

        assertEquals(DAY, response.getSplitUnit());
        assertTrue(response.getOffsetCountRatios().isEmpty());
    }

    @Test
    void getFraudPaymentsScoreSplitCountRatioByDayWithSuccessResult() throws Exception {
        Row firstRow = TestObjectFactory.testRow(TestObjectFactory.testRiskScoreOffsetCountRatioByDayRowFieldsMap());
        Row secondRow = TestObjectFactory.testRow(TestObjectFactory.testRiskScoreOffsetCountRatioByDayRowFieldsMap());
        List<Row> rows = List.of(firstRow, secondRow);
        Result result = TestObjectFactory.testResult(rows);
        when(warehouseQueryService.execute(any(Query.class))).thenReturn(result);

        MvcResult mvcResult = mockMvc.perform(get("/analytics/fraud-payments/scores/split-count/ratio")
                        .queryParam("fromTime", "2022-06-22T06:12:27Z")
                        .queryParam("toTime", "2016-06-30T06:12:27Z")
                        .queryParam(CURRENCY, "KZT")
                        .queryParam("merchantId", "1234")
                        .queryParam("shopId", "5678")
                        .queryParam("splitUnit", DAY.getValue()))
                .andExpect(status().isOk())
                .andReturn();

        String responseAsString = mvcResult.getResponse().getContentAsString();
        var response =
                objectMapper.readValue(responseAsString, SplitRiskScoreCountRatioResponse.class);

        assertEquals(DAY, response.getSplitUnit());
        assertEquals(4, response.getOffsetCountRatios().size());
        assertTrue(hasCorrectCountRatio(response, firstRow, TRUSTED_SCORE));
        assertTrue(hasCorrectCountRatio(response, secondRow, TRUSTED_SCORE));
        assertTrue(hasCorrectCountRatio(response, firstRow, LOW_SCORE));
        assertTrue(hasCorrectCountRatio(response, secondRow, LOW_SCORE));
        assertTrue(hasCorrectCountRatio(response, firstRow, HIGH_SCORE));
        assertTrue(hasCorrectCountRatio(response, secondRow, HIGH_SCORE));
        assertTrue(hasCorrectCountRatio(response, firstRow, FATAL_SCORE));
        assertTrue(hasCorrectCountRatio(response, secondRow, FATAL_SCORE));

        long firstRowOffset = LocalDate.parse(firstRow.getValues().get(DAY.getValue()))
                .atStartOfDay(UTC).toInstant()
                .toEpochMilli();
        assertTrue(hasCorrectOffset(response, firstRowOffset, TRUSTED_SCORE));
        assertTrue(hasCorrectOffset(response, firstRowOffset, LOW_SCORE));
        assertTrue(hasCorrectOffset(response, firstRowOffset, HIGH_SCORE));
        assertTrue(hasCorrectOffset(response, firstRowOffset, FATAL_SCORE));

        long secondRowOffset = LocalDate.parse(secondRow.getValues().get(DAY.getValue()))
                .atStartOfDay(UTC).toInstant()
                .toEpochMilli();
        assertTrue(hasCorrectOffset(response, secondRowOffset, TRUSTED_SCORE));
        assertTrue(hasCorrectOffset(response, secondRowOffset, LOW_SCORE));
        assertTrue(hasCorrectOffset(response, secondRowOffset, HIGH_SCORE));
        assertTrue(hasCorrectOffset(response, secondRowOffset, FATAL_SCORE));
    }

    private boolean hasCorrectCountRatio(SplitRiskScoreCountRatioResponse response, Row row, String score) {
        return response.getOffsetCountRatios().stream()
                .filter(riscScoreOffsetCountRatio -> riscScoreOffsetCountRatio.getScore().equals(score))
                .flatMap(riscScoreOffsetCountRatio -> riscScoreOffsetCountRatio.getOffsetCountRatio().stream())
                .anyMatch(offsetCountRatio -> isEqualFloat(row, score, offsetCountRatio.getCountRatio()));
    }

    private boolean isEqualFloat(Row row, String field, Float actual) {
        float expected = Float.parseFloat(row.getValues().get(field));
        return new BigDecimal(actual).compareTo(new BigDecimal(expected)) == 0;
    }

    private boolean hasCorrectOffset(SplitRiskScoreCountRatioResponse response, long currentDate, String score) {
        return response.getOffsetCountRatios().stream()
                .filter(riscScoreOffsetCountRatio -> riscScoreOffsetCountRatio.getScore().equals(score))
                .flatMap(riscScoreOffsetCountRatio -> riscScoreOffsetCountRatio.getOffsetCountRatio().stream())
                .anyMatch(offsetCountRatio -> offsetCountRatio.getOffset().equals(currentDate));
    }

    @Test
    void getFraudPaymentsScoreSplitCountRatioByMonthWithSuccessResult() throws Exception {
        Row firstRow = TestObjectFactory.testRow(
                TestObjectFactory.testRiskScoreOffsetCountRatioByMonthRowFieldsMap(1));
        Row secondRow = TestObjectFactory.testRow(
                TestObjectFactory.testRiskScoreOffsetCountRatioByMonthRowFieldsMap(2));
        List<Row> rows = List.of(firstRow, secondRow);
        Result result = TestObjectFactory.testResult(rows);
        when(warehouseQueryService.execute(any(Query.class))).thenReturn(result);

        MvcResult mvcResult = mockMvc.perform(get("/analytics/fraud-payments/scores/split-count/ratio")
                        .queryParam("fromTime", "2022-06-22T06:12:27Z")
                        .queryParam("toTime", "2016-06-30T06:12:27Z")
                        .queryParam(CURRENCY, "KZT")
                        .queryParam("merchantId", "1234")
                        .queryParam("shopId", "5678")
                        .queryParam("splitUnit", MONTH.getValue()))
                .andExpect(status().isOk())
                .andReturn();

        String responseAsString = mvcResult.getResponse().getContentAsString();
        var response =
                objectMapper.readValue(responseAsString, SplitRiskScoreCountRatioResponse.class);

        assertEquals(MONTH, response.getSplitUnit());
        assertEquals(4, response.getOffsetCountRatios().size());
        assertTrue(hasCorrectCountRatio(response, firstRow, TRUSTED_SCORE));
        assertTrue(hasCorrectCountRatio(response, secondRow, TRUSTED_SCORE));
        assertTrue(hasCorrectCountRatio(response, firstRow, LOW_SCORE));
        assertTrue(hasCorrectCountRatio(response, secondRow, LOW_SCORE));
        assertTrue(hasCorrectCountRatio(response, firstRow, HIGH_SCORE));
        assertTrue(hasCorrectCountRatio(response, secondRow, HIGH_SCORE));
        assertTrue(hasCorrectCountRatio(response, firstRow, FATAL_SCORE));
        assertTrue(hasCorrectCountRatio(response, secondRow, FATAL_SCORE));

        long previousMonthDate = getDateWithMonthOffset(1);
        long previousTwoMonthsDate = getDateWithMonthOffset(2);

        assertTrue(hasCorrectOffset(response, previousMonthDate, TRUSTED_SCORE));
        assertTrue(hasCorrectOffset(response, previousTwoMonthsDate, TRUSTED_SCORE));
        assertTrue(hasCorrectOffset(response, previousMonthDate, LOW_SCORE));
        assertTrue(hasCorrectOffset(response, previousTwoMonthsDate, LOW_SCORE));
        assertTrue(hasCorrectOffset(response, previousMonthDate, HIGH_SCORE));
        assertTrue(hasCorrectOffset(response, previousTwoMonthsDate, HIGH_SCORE));
        assertTrue(hasCorrectOffset(response, previousMonthDate, FATAL_SCORE));
        assertTrue(hasCorrectOffset(response, previousTwoMonthsDate, FATAL_SCORE));
    }

    private long getDateWithMonthOffset(int monthOffset) {
        LocalDate now = LocalDate.now();
        return LocalDate.of(TestObjectFactory.getYearWithOffset(monthOffset, now),
                        TestObjectFactory.getMonthWithOffset(monthOffset, now), 1)
                .atStartOfDay()
                .atZone(UTC)
                .toInstant().toEpochMilli();
    }

    @Test
    void getCurrenciesWithEmptyResult() throws Exception {
        Row row = TestObjectFactory.testRow(Collections.emptyMap());
        Result result = TestObjectFactory.testResult(List.of(row));
        when(warehouseQueryService.execute(any(Query.class))).thenReturn(result);

        MvcResult mvcResult = mockMvc.perform(get("/analytics/currencies"))
                .andExpect(status().isOk())
                .andReturn();

        String responseAsString = mvcResult.getResponse().getContentAsString();
        var response =
                objectMapper.readValue(responseAsString, ListResponse.class);

        assertTrue(response.getResult().isEmpty());
    }

    @Test
    void getCurrenciesWithSuccessResult() throws Exception {
        String rubCurrency = "RUB";
        String eurCurrency = "EUR";
        Row firstRow = TestObjectFactory.testRow(Map.of(CURRENCY, rubCurrency));
        Row secondRow = TestObjectFactory.testRow(Map.of(CURRENCY, eurCurrency));
        Result result = TestObjectFactory.testResult(List.of(firstRow, secondRow));
        when(warehouseQueryService.execute(any(Query.class))).thenReturn(result);

        MvcResult mvcResult = mockMvc.perform(get("/analytics/currencies"))
                .andExpect(status().isOk())
                .andReturn();

        String responseAsString = mvcResult.getResponse().getContentAsString();
        var response =
                objectMapper.readValue(responseAsString, ListResponse.class);

        assertTrue(response.getResult().stream().anyMatch(currency -> currency.equals(rubCurrency)));
        assertTrue(response.getResult().stream().anyMatch(currency -> currency.equals(eurCurrency)));
    }
}