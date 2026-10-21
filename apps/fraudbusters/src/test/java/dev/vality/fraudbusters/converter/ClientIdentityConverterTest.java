package dev.vality.fraudbusters.converter;

import dev.vality.damsel.domain.ClientIdentity;
import dev.vality.damsel.fraudbusters.PaymentStatus;
import dev.vality.fraudbusters.util.BeanUtil;
import dev.vality.fraudbusters.util.PaymentTypeByContextResolver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClientIdentityConverterTest {

    private final PaymentTypeByContextResolver paymentTypeResolver = new PaymentTypeByContextResolver();

    @Test
    void inspectorUsesInvoiceIdentityAndAcceptsOldInvoices() {
        var context = BeanUtil.createContext();
        var converter = new ContextToFraudRequestConverter(paymentTypeResolver);
        var legacy = converter.convert(context).getFraudModel();
        assertNull(legacy.getCustomerId());
        assertNull(legacy.getUserId());
        context.getPayment().getInvoice().setClientIdentity(new ClientIdentity("synthetic-customer")
                .setUserId("Synthetic-User"));
        var model = converter.convert(context).getFraudModel();
        assertEquals("synthetic-customer", model.getCustomerId());
        assertEquals("Synthetic-User", model.getUserId());
        context.getPayment().getInvoice().getClientIdentity().unsetUserId();
        assertNull(converter.convert(context).getFraudModel().getUserId());
    }

    @Test
    void userInspectionUsesIdentityAndSeparatesCachedResults() {
        var context = new dev.vality.damsel.fraudbusters.InspectUserContext()
                .setUserInfo(new dev.vality.damsel.fraudbusters.ClientInfo()
                        .setCustomerId("synthetic-customer").setUserId("Synthetic-User"))
                .setShopList(java.util.List.of(new dev.vality.damsel.fraudbusters.ShopContext()
                        .setPartyId("synthetic-party").setShopId("synthetic-shop")));
        var model = dev.vality.fraudbusters.util.PaymentModelFactory.buildPaymentModel(
                context, context.getShopList().get(0));
        assertEquals("synthetic-customer", model.getCustomerId());
        assertEquals("Synthetic-User", model.getUserId());
        String firstKey = dev.vality.fraudbusters.util.UserCacheKeyUtil.buildInspectUserCacheKey(context);
        context.getUserInfo().setUserId("different-synthetic-user");
        assertNotEquals(firstKey, dev.vality.fraudbusters.util.UserCacheKeyUtil.buildInspectUserCacheKey(context));
        context.getUserInfo().setUserId("Synthetic-User").setCustomerId("different-synthetic-customer");
        assertNotEquals(firstKey, dev.vality.fraudbusters.util.UserCacheKeyUtil.buildInspectUserCacheKey(context));
    }

    @Test
    void languageFieldsResolveInListsAggregationsAndGroups() {
        var model = new dev.vality.fraudbusters.fraud.model.PaymentModel();
        model.setCustomerId("synthetic-customer");
        model.setUserId("Synthetic-User");
        var fields = new dev.vality.fraudbusters.fraud.payment.resolver.PaymentModelFieldResolver();
        var database = new dev.vality.fraudbusters.fraud.payment.resolver.DatabasePaymentFieldResolver();
        assertEquals("synthetic-customer", fields.resolveValue("customer_id", model));
        assertEquals("Synthetic-User", fields.resolveValue("user_id", model));
        var userField = fields.resolveName("user_id");
        assertEquals("userId", database.resolve(userField));
        assertEquals("Synthetic-User", database.resolve(userField, model).getValue());
        assertEquals("customerId", database.resolve(fields.resolveName("customer_id")));
    }

    @Test
    void historicalPaymentRetainsIdentityWhenReplayed() {
        var payment = BeanUtil.createPayment(PaymentStatus.captured);
        payment.getClientInfo().setCustomerId("synthetic-customer").setUserId("Synthetic-User");
        var checked = new PaymentToCheckedPaymentConverter(paymentTypeResolver).convert(payment);
        var replay = new CheckedPaymentToPaymentConverter().convert(checked);
        assertEquals(payment.getClientInfo().getCustomerId(), replay.getClientInfo().getCustomerId());
        assertEquals(payment.getClientInfo().getUserId(), replay.getClientInfo().getUserId());
        var model = new PaymentToPaymentModelConverter().convert(replay);
        assertEquals("synthetic-customer", model.getCustomerId());
        assertEquals("Synthetic-User", model.getUserId());
    }
}
