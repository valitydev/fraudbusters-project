package dev.vality.fraudbusters.management.resource.payment;

import dev.vality.fraudbusters.management.TestObjectFactory;
import dev.vality.fraudbusters.management.domain.WbListCandidateBatchModel;
import dev.vality.fraudbusters.management.domain.request.FilterRequest;
import dev.vality.fraudbusters.management.domain.response.FilterResponse;
import dev.vality.fraudbusters.management.domain.tables.pojos.WbListCandidate;
import dev.vality.fraudbusters.management.service.iface.WbListCandidateBatchService;
import dev.vality.fraudbusters.management.service.iface.WbListCandidateService;
import dev.vality.swag.fraudbusters.management.model.IdListRequest;
import dev.vality.swag.fraudbusters.management.model.WbListCandidatesRequest;
import org.junit.jupiter.api.Test;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
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

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WbListCandidateService wbListCandidateService;

    @MockitoBean
    private WbListCandidateBatchService wbListCandidateBatchService;

    private final JsonMapper objectMapper = JsonMapper.builder().build();

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
        WbListCandidatesRequest request = new WbListCandidatesRequest();
        int count = 3;
        request.setRecords(TestObjectFactory.testChargebacks(count));
        String key = TestObjectFactory.randomString();
        when(wbListCandidateService.sendToCandidate(anyList())).thenReturn(key);

        this.mockMvc.perform(post("/payments-lists/candidates")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
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