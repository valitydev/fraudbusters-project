package dev.vality.fraudbusters.management.resource.payment;

import dev.vality.damsel.domain.BankCard;
import dev.vality.fraud_data_crawler.FraudDataCandidate;
import dev.vality.fraudbusters.management.TestObjectFactory;
import dev.vality.fraudbusters.management.constant.CandidateListName;
import dev.vality.fraudbusters.management.dao.CatalogMetadataDao;
import dev.vality.fraudbusters.management.dao.CatalogMetadataDao.ListNameCount;
import dev.vality.fraudbusters.management.domain.WbListCandidateBatchModel;
import dev.vality.fraudbusters.management.domain.enums.ListType;
import dev.vality.fraudbusters.management.domain.request.FilterRequest;
import dev.vality.fraudbusters.management.domain.response.FilterResponse;
import dev.vality.fraudbusters.management.domain.tables.pojos.WbListCandidate;
import dev.vality.fraudbusters.management.service.iface.WbListCandidateBatchService;
import dev.vality.fraudbusters.management.service.iface.WbListCandidateService;
import dev.vality.fraudbusters.management.service.payment.PaymentsListsService;
import dev.vality.swag.fraudbusters.management.model.IdListRequest;
import dev.vality.swag.fraudbusters.management.model.WbListCandidatesRequest;
import dev.vality.swag.fraudbusters.management.model.WbListRecordsResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jooq.autoconfigure.JooqAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = {FlywayAutoConfiguration.class, JooqAutoConfiguration.class,
        ManagementWebSecurityAutoConfiguration.class, SecurityAutoConfiguration.class,
        ServletWebSecurityAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class})
class PaymentListCandidateResourceTest {

    private static final String PAYMENT_CATALOG_FILTER_RECORDS = "/payments/catalog/lists/filter-records";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WbListCandidateService wbListCandidateService;

    @MockitoBean
    private WbListCandidateBatchService wbListCandidateBatchService;

    @MockitoBean
    private CatalogMetadataDao catalogMetadataDao;

    @MockitoBean
    private PaymentsListsService paymentsListsService;

    private final JsonMapper objectMapper = JsonMapper.builder().build();

    @Test
    void servesPaymentAndPayoutListMetadata() throws Exception {
        String firstListName = "first";
        String secondListName = "second";
        String namedListName = "example";
        int candidateBatches = 105;
        when(catalogMetadataDao.countPaymentListRows()).thenReturn(List.of(
                new ListNameCount(ListType.black, firstListName, 2),
                new ListNameCount(ListType.black, secondListName, 1)));
        when(catalogMetadataDao.countCandidateBatches()).thenReturn(candidateBatches);
        when(catalogMetadataDao.countPayoutListRows()).thenReturn(
                List.of(new ListNameCount(ListType.naming, namedListName, 7)));

        mockMvc.perform(get("/payments/catalog/lists/metadata"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.counts.black", equalTo(3)))
                .andExpect(jsonPath("$.counts.white", equalTo(0)))
                .andExpect(jsonPath("$.counts.candidateBatches", equalTo(candidateBatches)))
                .andExpect(jsonPath("$.listNames.black", equalTo(List.of(firstListName, secondListName))));
        mockMvc.perform(get("/payouts/catalog/lists/metadata"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.counts.named", equalTo(7)))
                .andExpect(jsonPath("$.listNames.named", equalTo(List.of(namedListName))));
    }

    @Test
    void servesPaymentCatalogRowsWithoutListNames() throws Exception {
        String listType = ListType.black.getLiteral();
        int recordCount = 7;
        when(paymentsListsService.filterLists(listType, null, null, null, null, null, null, 50))
                .thenReturn(new WbListRecordsResponse().count(recordCount).result(List.of()));

        mockMvc.perform(get(PAYMENT_CATALOG_FILTER_RECORDS).param("listType", listType))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", equalTo(recordCount)));
    }

    @Test
    void requiresListTypeForPaymentCatalogRows() throws Exception {
        mockMvc.perform(get(PAYMENT_CATALOG_FILTER_RECORDS))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approveListCandidates() throws Exception {
        List<Long> ids = List.of(
                TestObjectFactory.randomLong(),
                TestObjectFactory.randomLong(),
                TestObjectFactory.randomLong());
        doNothing().when(wbListCandidateService).approve(anyList(), anyString());

        this.mockMvc.perform(post("/payments-lists/candidates/approved")
                        .content(objectMapper.writeValueAsString(new IdListRequest().ids(ids)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

    }

    @Test
    void listCandidates() throws Exception {
        int count = 3;
        String source = "chargebacks";
        List<WbListCandidate> wbListCandidates = TestObjectFactory.testWbListCandidates(count);
        wbListCandidates.forEach(wbListCandidate -> wbListCandidate.setSource(source));
        FilterResponse<WbListCandidate> response = new FilterResponse<>();
        response.setResult(wbListCandidates);
        Long lastId = TestObjectFactory.randomLong();
        response.setNumericLastId(lastId);
        when(wbListCandidateService.getList(any(FilterRequest.class))).thenReturn(response);

        this.mockMvc.perform(get("/payments-lists/candidates")
                        .queryParams(createParams()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", hasSize(count)))
                .andExpect(jsonPath("$.source", equalTo(source)))
                .andExpect(jsonPath("$.lastId", equalTo(lastId.intValue())));

    }

    private LinkedMultiValueMap<String, String> createParams() {
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("continuationId", "1");
        params.add("size", "100");
        params.add("sortOrder", "DESC");
        return params;
    }

    @Test
    void insertListCandidates() throws Exception {
        String cardToken = "test-card-token_1";
        var withToken = TestObjectFactory.testChargeback()
                .paymentTool(new BankCard().setToken(cardToken).toString());
        var withoutToken = TestObjectFactory.testChargeback()
                .paymentTool(new BankCard().toString());
        var blankToken = TestObjectFactory.testChargeback()
                .paymentTool(new BankCard().setToken(" ").toString());
        var withoutPaymentTool = TestObjectFactory.testChargeback();
        var request = new WbListCandidatesRequest()
                .records(List.of(withToken, withoutToken, blankToken, withoutPaymentTool));
        when(wbListCandidateService.sendToCandidate(anyList())).thenReturn("batch-key");

        this.mockMvc.perform(post("/payments-lists/candidates")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        ArgumentCaptor<List<FraudDataCandidate>> captor = ArgumentCaptor.captor();
        verify(wbListCandidateService).sendToCandidate(captor.capture());
        var candidates = captor.getValue();
        assertEquals(13, candidates.size());
        var cardCandidates = candidates.stream()
                .filter(candidate -> CandidateListName.CARD_TOKEN.equals(candidate.getType()))
                .toList();
        assertEquals(1, cardCandidates.size());
        var candidate = cardCandidates.getFirst();
        assertEquals(cardToken, candidate.getValue());
        assertEquals(dev.vality.fraud_data_crawler.ListType.black, candidate.getListType());
        assertEquals(withToken.getMerchantInfo().getPartyId(), candidate.getMerchantId());
        assertEquals(withToken.getMerchantInfo().getShopId(), candidate.getShopId());
    }

    @Test
    void listCandidatesBatches() throws Exception {
        int count = 3;
        String source = "Chargebacks";
        var wbListCandidates = TestObjectFactory.testWbListCandidateBatchModels(count);
        wbListCandidates.forEach(wbListCandidate -> wbListCandidate.setSource(source));
        FilterResponse<WbListCandidateBatchModel> response = new FilterResponse<>();
        response.setResult(wbListCandidates);
        String lastId = TestObjectFactory.randomString();
        response.setLastId(lastId);
        when(wbListCandidateBatchService.getList(any(FilterRequest.class))).thenReturn(response);

        this.mockMvc.perform(get("/payments-lists/candidates-batches")
                        .queryParams(createParams()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", hasSize(count)))
                .andExpect(jsonPath("$.lastId", equalTo(lastId)));

    }
}
