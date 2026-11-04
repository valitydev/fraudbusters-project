package dev.vality.fraudbusters.config.payout;

import dev.vality.fraudbusters.fraud.FraudTemplateValidator;
import dev.vality.fraudbusters.fraud.InspectStorage;
import dev.vality.fraudbusters.fraud.constant.CheckedField;
import dev.vality.fraudbusters.fraud.model.PayoutModel;
import dev.vality.fraudbusters.fraud.payout.PayoutInspectStorage;
import dev.vality.fraudbusters.pool.Pool;
import dev.vality.fraudbusters.pool.PoolImpl;
import dev.vality.fraudbusters.stream.impl.NotifyingRuleApplierImpl;
import dev.vality.fraudbusters.util.CheckedResultFactory;
import dev.vality.fraudo.payment.visitor.impl.FirstFindVisitorImpl;
import org.antlr.v4.runtime.ParserRuleContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class PayoutPoolConfig {

    @Bean
    public Pool<List<String>> payoutGroupPoolImpl() {
        return new PoolImpl<>("payout-group");
    }

    @Bean
    public Pool<String> payoutReferencePoolImpl() {
        return new PoolImpl<>("payout-reference");
    }

    @Bean
    public Pool<String> payoutGroupReferencePoolImpl() {
        return new PoolImpl<>("payout-group-reference");
    }

    @Bean
    public Pool<ParserRuleContext> payoutTemplatePoolImpl() {
        return new PoolImpl<>("payout-template");
    }

    @Bean
    public NotifyingRuleApplierImpl<PayoutModel> payoutRuleApplier(
            FirstFindVisitorImpl<PayoutModel, CheckedField> payoutRuleVisitor,
            Pool<ParserRuleContext> payoutTemplatePoolImpl,
            CheckedResultFactory checkedResultFactory) {
        return new NotifyingRuleApplierImpl<>(payoutRuleVisitor, payoutTemplatePoolImpl, checkedResultFactory);
    }

    @Bean
    public InspectStorage payoutInspectStorage(
            Pool<ParserRuleContext> payoutTemplatePoolImpl,
            Pool<List<String>> payoutGroupPoolImpl,
            Pool<String> payoutReferencePoolImpl,
            Pool<String> payoutGroupReferencePoolImpl,
            FraudTemplateValidator payoutTemplateValidator) {
        return new PayoutInspectStorage(payoutTemplatePoolImpl, payoutGroupPoolImpl, payoutReferencePoolImpl,
                payoutGroupReferencePoolImpl, payoutTemplateValidator);
    }

}
