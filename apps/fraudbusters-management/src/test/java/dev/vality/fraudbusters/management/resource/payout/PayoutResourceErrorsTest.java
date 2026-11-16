package dev.vality.fraudbusters.management.resource.payout;

import dev.vality.fraudbusters.management.exception.NotFoundException;
import dev.vality.fraudbusters.management.service.payout.PayoutReferencesService;
import dev.vality.fraudbusters.management.utils.UserInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PayoutResourceErrorsTest {

    private final PayoutReferencesService service = mock(PayoutReferencesService.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(
                    new PayoutsReferencesResource(service, mock(UserInfoService.class)))
            .setControllerAdvice(new PayoutResourceErrors())
            .build();

    @Test
    void invalidScopeIsABadRequest() throws Exception {
        when(service.insertReferences(anyList(), any()))
                .thenThrow(new IllegalArgumentException("Invalid payout scope"));

        mvc.perform(post("/payouts-references")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"templateId\":\"t\",\"walletId\":\"wallet-1\"}]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid payout scope"));
    }

    @Test
    void missingReferenceIsNotFound() throws Exception {
        when(service.removeReference(eq("party-1"), any(), any(), any()))
                .thenThrow(new NotFoundException("Payout reference not found"));

        mvc.perform(delete("/payouts-references")
                        .param("partyId", "party-1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void validRequestIsAccepted() throws Exception {
        when(service.removeReference(anyString(), any(), any(), any())).thenReturn("party:7:party-1");

        mvc.perform(delete("/payouts-references")
                        .param("partyId", "party-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("party:7:party-1"));
    }
}
