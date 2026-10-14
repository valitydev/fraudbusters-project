package dev.vality.fraudbusters.management;

import dev.vality.damsel.wb_list.ListType;
import dev.vality.dao.DaoException;
import dev.vality.fraudbusters.management.config.converter.JwtAuthConverter;
import dev.vality.fraudbusters.management.converter.candidate.ChargebacksToFraudDataCandidatesConverter;
import dev.vality.fraudbusters.management.converter.candidate.WbListCandidateToWbListRecordConverter;
import dev.vality.fraudbusters.management.converter.payment.*;
import dev.vality.fraudbusters.management.dao.payment.wblist.WbListDao;
import dev.vality.fraudbusters.management.domain.ListRecord;
import dev.vality.fraudbusters.management.domain.payment.PaymentCountInfo;
import dev.vality.fraudbusters.management.domain.payment.PaymentListRecord;
import dev.vality.fraudbusters.management.domain.payment.request.ListRowsInsertRequest;
import dev.vality.fraudbusters.management.domain.response.ErrorResponse;
import dev.vality.fraudbusters.management.exception.KafkaSerializationException;
import dev.vality.fraudbusters.management.listener.WbListEventListener;
import dev.vality.fraudbusters.management.resource.payment.PaymentsListsResource;
import dev.vality.fraudbusters.management.service.WbListCommandService;
import dev.vality.fraudbusters.management.service.iface.AuditService;
import dev.vality.fraudbusters.management.service.iface.WbListCandidateBatchService;
import dev.vality.fraudbusters.management.service.iface.WbListCandidateService;
import dev.vality.fraudbusters.management.service.payment.PaymentsListsService;
import dev.vality.fraudbusters.management.utils.*;
import dev.vality.fraudbusters.management.utils.parser.CsvPaymentCountInfoParser;
import dev.vality.fraudbusters.management.validator.ListRowValidator;
import dev.vality.swag.fraudbusters.management.model.ListResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jooq.autoconfigure.JooqAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = {ParametersService.class, PaymentsListsResource.class,
        UserInfoService.class, WbListRecordToRowConverter.class, PaymentCountInfoGenerator.class,
        CountInfoUtils.class, CountInfoApiUtils.class, CsvPaymentCountInfoParser.class,
        WbListRecordsModelToWbListRecordConverter.class, PaymentsListsService.class, ListRowValidator.class})
@EnableAutoConfiguration(exclude = {FlywayAutoConfiguration.class, JooqAutoConfiguration.class,
        ManagementWebSecurityAutoConfiguration.class, SecurityAutoConfiguration.class,
        ServletWebSecurityAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class})
public class ExceptionApplicationTest {

    public static final String ID_TEST = "42";
    public static final String TEST_MESSAGE = "test_message";
    private static final String VALUE = "value";
    private static final String SHOP_ID = "shopId";
    private static final String PARTY_ID = "partyId";
    private static final String LIST_NAME = "listName";

    @Value("${kafka.topic.wblist.event.sink}")
    public String topicEventSink;

    @MockitoBean
    public AuditService auditService;
    @MockitoBean
    public JwtAuthConverter jwtAuthConverter;
    @MockitoBean
    public WbListCommandService wbListCommandService;
    @MockitoBean
    public PaymentListRecordToRowConverter paymentListRecordToRowConverter;
    @MockitoBean
    public WbListEventListener wbListEventListener;
    @MockitoBean
    public WbListDao wbListDao;
    @MockitoBean
    public WbListRecordsToListRecordConverter wbListRecordsToListRecordConverter;
    @MockitoBean
    public PaymentCountInfoRequestToRowConverter countInfoListRecordToRowConverter;
    @MockitoBean
    public WbListRecordsToCountInfoListRequestConverter wbListRecordsToListRecordWithRowConverter;
    @MockitoBean
    WbListCandidateBatchService wbListCandidateBatchService;
    @MockitoBean
    WbListCandidateService wbListCandidateService;
    @MockitoBean
    WbListCandidateToWbListRecordConverter wbListCandidateToWbListRecordConverter;
    @MockitoBean
    CandidateBatchModelToCandidateBatchConverter candidateBatchConverter;
    @MockitoBean
    ChargebacksToFraudDataCandidatesConverter chargebackConverter;
    @Autowired
    RestTemplateBuilder restTemplateBuilder;
    @LocalServerPort
    int serverPort;

    String paymentListPath;
    String paymentListFilterPath;

    @BeforeEach
    void init() {
        paymentListPath = String.format(MethodPaths.SERVICE_BASE_URL + MethodPaths.INSERT_PAYMENTS_LIST_ROW_PATH,
                serverPort);
        paymentListFilterPath = String.format(MethodPaths.SERVICE_BASE_URL + MethodPaths.INSERT_PAYMENTS_FILTER_PATH,
                serverPort);
    }

    private ListRecord createRow() {
        PaymentListRecord listRecord = new PaymentListRecord();
        listRecord.setShopId(SHOP_ID);
        listRecord.setPartyId(PARTY_ID);
        listRecord.setListName(LIST_NAME);
        listRecord.setValue(VALUE);
        return listRecord;
    }

    @Test
    void executionRestTest() {
        RestTemplate restTemplate = restTemplateBuilder.build();
        Mockito.when(wbListCommandService.sendListRecords(any(), any(), any(), any(), any()))
                .thenReturn(List.of(ID_TEST));

        ResponseEntity<ListResponse> response = restTemplate.exchange(paymentListPath, HttpMethod.POST,
                new HttpEntity<>(createRequest()), new ParameterizedTypeReference<>() {
                });
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(response.getBody().getResult().get(0), ID_TEST);
    }

    @Test
    void executionRestDaoExceptionTest() {
        RestTemplate restTemplate = restTemplateBuilder.build();
        Mockito.when(wbListCommandService.sendListRecords(any(), any(), any(), any(), any()))
                .thenThrow(new DaoException(TEST_MESSAGE));
        assertThrows(HttpServerErrorException.InternalServerError.class,
                () -> restTemplate.postForEntity(paymentListPath, createRequest(), ErrorResponse.class));
    }

    private ListRowsInsertRequest createRequest() {
        ListRowsInsertRequest listRowsInsertRequest = new ListRowsInsertRequest();
        listRowsInsertRequest.setListType(ListType.white);
        PaymentCountInfo countInfo = new PaymentCountInfo();
        countInfo.setListRecord((PaymentListRecord) createRow());
        listRowsInsertRequest.setRecords(List.of(countInfo));
        return listRowsInsertRequest;
    }

    @Test
    void executionRestKafkaSerializationTest() {
        RestTemplate restTemplate = restTemplateBuilder.build();
        Mockito.when(wbListCommandService.sendListRecords(any(), any(), any(), any(), any()))
                .thenThrow(new KafkaSerializationException(TEST_MESSAGE));

        assertThrows(HttpServerErrorException.InternalServerError.class,
                () -> restTemplate.postForEntity(paymentListPath, createRequest(), ErrorResponse.class));
    }

    @Test
    void getRestTestBadRequest() {
        Mockito.when(wbListCommandService.sendListRecords(any(), any(), any(), any(), any()))
                .thenThrow(new KafkaSerializationException(TEST_MESSAGE));
        HashMap<String, Object> uriVariables = new HashMap<>();
        UriComponentsBuilder builder =
                UriComponentsBuilder.fromUriString(paymentListFilterPath)
                        .queryParam("partyId", PARTY_ID)
                        .queryParam("shopId", SHOP_ID);
        uriVariables.put("partyId", PARTY_ID);
        uriVariables.put("shopId", SHOP_ID);
        RestTemplate restTemplate = restTemplateBuilder.build();
        assertThrows(HttpClientErrorException.BadRequest.class,
                () -> restTemplate.getForEntity(builder.buildAndExpand(uriVariables).toUri(), ErrorResponse.class));
    }
}
