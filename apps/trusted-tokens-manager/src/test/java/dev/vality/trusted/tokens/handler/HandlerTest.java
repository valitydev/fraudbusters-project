package dev.vality.trusted.tokens.handler;

import dev.vality.trusted.tokens.config.ObjectMapperConfig;
import dev.vality.trusted.tokens.ConditionTemplateNotFound;
import dev.vality.trusted.tokens.InvalidRequest;
import dev.vality.trusted.tokens.calculator.ConditionTrustedResolver;
import dev.vality.trusted.tokens.converter.RowConverter;
import dev.vality.trusted.tokens.dao.TemplateDao;
import dev.vality.trusted.tokens.dao.TokenDao;
import dev.vality.trusted.tokens.service.TemplateService;
import dev.vality.trusted.tokens.validator.ConditionTemplateValidator;
import dev.vality.trusted.tokens.validator.conditions.PaymentsConditionsValidator;
import dev.vality.trusted.tokens.validator.conditions.PayoutConditionsValidator;
import org.apache.thrift.TException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static dev.vality.trusted.tokens.utils.CardTokenDataUtils.createCardTokenData;
import static dev.vality.trusted.tokens.utils.ConditionTemplateRequestUtils.*;
import static dev.vality.trusted.tokens.utils.ConditionTemplateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ContextConfiguration(classes = {
        TrustedTokensHandler.class, TemplateService.class, RowConverter.class, ObjectMapperConfig.class,
        ConditionTemplateValidator.class, PaymentsConditionsValidator.class, PayoutConditionsValidator.class,
        ConditionTrustedResolver.class
})
public class HandlerTest {

    private static final String TOKEN = "token";

    @MockitoBean
    private TemplateDao templateDao;

    @MockitoBean
    private TokenDao tokenDao;

    @Autowired
    private TrustedTokensHandler trustedTokensHandler;

    @BeforeEach
    public void init() {
        when(tokenDao.get(TOKEN))
                .thenReturn(createCardTokenData());
        when(templateDao.get("TemplateNotTrustedPayment"))
                .thenReturn(createTemplateNotTrusted(PAYMENT));
        when(templateDao.get("TemplateTrustedPayment"))
                .thenReturn(createTemplateTrusted(PAYMENT));
        when(templateDao.get("TemplateTrustedWithSeveralCurrencyPayment"))
                .thenReturn(createTemplateTrustedWithSeveralCurrency(PAYMENT));
        when(templateDao.get("TemplateNotTrustedPayout"))
                .thenReturn(createTemplateNotTrusted(PAYOUT));
        when(templateDao.get("TemplateTrustedPayout"))
                .thenReturn(createTemplateTrusted(PAYOUT));
        when(templateDao.get("TemplateTrustedWithSeveralCurrencyPayout"))
                .thenReturn(createTemplateTrustedWithSeveralCurrency(PAYOUT));
    }

    @Test
    void isTokenTrustedTest() throws TException {
        assertFalse(trustedTokensHandler.isTokenTrusted(
                TOKEN, createTemplateNotTrusted(PAYMENT)));
        assertTrue(trustedTokensHandler.isTokenTrusted(
                TOKEN, createTemplateTrusted(PAYMENT)));
        assertTrue(trustedTokensHandler.isTokenTrusted(
                TOKEN, createTemplateTrustedWithSeveralCurrency(PAYMENT)));
        assertFalse(trustedTokensHandler.isTokenTrusted(
                TOKEN, createTemplateNotTrustedWithSeveralCurrency(PAYMENT)));
        assertFalse(trustedTokensHandler.isTokenTrusted(
                TOKEN, createTemplateNotTrusted(PAYOUT)));
        assertTrue(trustedTokensHandler.isTokenTrusted(
                TOKEN, createTemplateTrusted(PAYOUT)));
        assertTrue(trustedTokensHandler.isTokenTrusted(
                TOKEN, createTemplateTrustedWithSeveralCurrency(PAYOUT)));
        assertFalse(trustedTokensHandler.isTokenTrusted(
                TOKEN, createTemplateNotTrustedWithSeveralCurrency(PAYOUT)));
        assertThrows(InvalidRequest.class,
                () -> trustedTokensHandler.isTokenTrusted(TOKEN,
                        createTemplate(null, null)));
        assertTrue(trustedTokensHandler.isTokenTrusted(TOKEN,
                createTemplateWithPayoutAndPayment()));
    }

    @Test
    void isTokenTrustedByConditionTemplateNameTest() throws TException {
        assertFalse(trustedTokensHandler.isTokenTrustedByConditionTemplateName(
                TOKEN, "TemplateNotTrustedPayment"));
        assertTrue(trustedTokensHandler.isTokenTrustedByConditionTemplateName(
                TOKEN, "TemplateTrustedPayment"));
        assertTrue(trustedTokensHandler.isTokenTrustedByConditionTemplateName(
                TOKEN, "TemplateTrustedWithSeveralCurrencyPayment"));
        assertFalse(trustedTokensHandler.isTokenTrustedByConditionTemplateName(
                TOKEN, "TemplateNotTrustedPayout"));
        assertTrue(trustedTokensHandler.isTokenTrustedByConditionTemplateName(
                TOKEN, "TemplateTrustedPayout"));
        assertTrue(trustedTokensHandler.isTokenTrustedByConditionTemplateName(
                TOKEN, "TemplateTrustedWithSeveralCurrencyPayout"));
        assertThrows(ConditionTemplateNotFound.class,
                () -> trustedTokensHandler.isTokenTrustedByConditionTemplateName(
                        TOKEN, "UnknownTemplate"));

    }

    @Test
    void createNewConditionTemplateTest() {
        assertThrows(NullPointerException.class,
                () -> trustedTokensHandler.createNewConditionTemplate(
                        createTemplatePaymentRequestWithNullCurrency()));
        assertThrows(NullPointerException.class,
                () -> trustedTokensHandler.createNewConditionTemplate(
                        createTemplatePaymentRequestWithNullYearsOffset()));
        assertThrows(NullPointerException.class,
                () -> trustedTokensHandler.createNewConditionTemplate(
                        createTemplatePaymentRequestWithNullYearsOffset()));
        assertThrows(InvalidRequest.class,
                () -> trustedTokensHandler.createNewConditionTemplate(
                        createTemplatePaymentRequestWithNullCount()));
    }

}
