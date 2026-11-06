package dev.vality.fraudbusters.fraud.payment.resolver;

import dev.vality.fraudbusters.fraud.model.PaymentModel;
import dev.vality.fraudbusters.fraud.pool.CardTokenPool;
import dev.vality.trusted.tokens.TrustedTokensSrv;
import org.apache.thrift.TException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static dev.vality.fraudbusters.util.BeanUtil.TOKEN;
import static dev.vality.fraudbusters.util.BeanUtil.createPaymentModel;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerTypeResolverImplTest {

    private CustomerTypeResolverImpl customerTypeResolver;

    @Mock
    private CardTokenPool cardTokenPool;

    @Mock
    private TrustedTokensSrv.Iface trustedTokensSrv;

    @BeforeEach
    void setUp() {
        customerTypeResolver = new CustomerTypeResolverImpl(cardTokenPool, trustedTokensSrv);
    }

    @Test
    void isTrustedTest() {
        when(cardTokenPool.isExist(anyString()))
                .thenReturn(false)
                .thenReturn(true);

        PaymentModel paymentModel = createPaymentModel();
        assertFalse(customerTypeResolver.isTrusted(paymentModel));
        assertTrue(customerTypeResolver.isTrusted(paymentModel));

        verify(cardTokenPool, times(2)).isExist(TOKEN);
    }

    @Test
    void isTrustedByConditionTemplateNameTest() throws TException {
        when(trustedTokensSrv.isTokenTrustedByConditionTemplateName(anyString(), anyString()))
                .thenReturn(false)
                .thenReturn(true);

        PaymentModel paymentModel = createPaymentModel();
        String templateName = "templateName";
        assertFalse(customerTypeResolver.isTrusted(paymentModel, templateName));
        assertTrue(customerTypeResolver.isTrusted(paymentModel, templateName));

        verify(trustedTokensSrv, times(2))
                .isTokenTrustedByConditionTemplateName(TOKEN, templateName);
    }

}
