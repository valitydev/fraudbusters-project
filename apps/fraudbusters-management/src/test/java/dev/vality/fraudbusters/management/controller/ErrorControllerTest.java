package dev.vality.fraudbusters.management.controller;

import dev.vality.fraudbusters.management.converter.payment.TemplateValidateErrorsToValidateTemplateResponseConverter;
import dev.vality.fraudbusters.management.dao.TemplateDao;
import dev.vality.fraudbusters.management.domain.request.FilterRequest;
import dev.vality.fraudbusters.management.resource.payment.PaymentsTemplatesResource;
import dev.vality.fraudbusters.management.service.TemplateCommandService;
import dev.vality.fraudbusters.management.service.iface.ValidationTemplateService;
import dev.vality.fraudbusters.management.service.payment.PaymentsTemplatesService;
import dev.vality.fraudbusters.management.utils.CommandMapper;
import dev.vality.fraudbusters.management.utils.UserInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ErrorControllerTest {

    private final PaymentsTemplatesService templatesService = mock(PaymentsTemplatesService.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new PaymentsTemplatesResource(
                    mock(TemplateCommandService.class),
                    mock(ValidationTemplateService.class),
                    mock(TemplateDao.class),
                    mock(UserInfoService.class),
                    mock(CommandMapper.class),
                    templatesService,
                    mock(TemplateValidateErrorsToValidateTemplateResponseConverter.class)))
            .setControllerAdvice(new ErrorController())
            .build();

    @Test
    void unknownSortOrderIsABadRequest() throws Exception {
        mvc.perform(get("/payments-templates/filter")
                        .param("sortOrder", "FOO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorController.INVALID_PARAMETERS));
    }

    @Test
    void negativeSizeIsABadRequest() throws Exception {
        when(templatesService.filterTemplates(any(FilterRequest.class)))
                .thenThrow(new IllegalArgumentException("size must not be negative: -1"));

        mvc.perform(get("/payments-templates/filter")
                        .param("size", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("size must not be negative: -1"));
    }

}
