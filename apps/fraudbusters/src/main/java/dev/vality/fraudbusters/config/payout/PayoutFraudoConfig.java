package dev.vality.fraudbusters.config.payout;

import dev.vality.fraudbusters.fraud.FraudTemplateValidator;
import dev.vality.fraudbusters.fraud.constant.PaymentCheckedField;
import dev.vality.fraudbusters.fraud.model.PaymentModel;
import dev.vality.fraudbusters.fraud.model.PayoutModel;
import dev.vality.fraudbusters.fraud.payment.aggregator.clickhouse.CountAggregatorImpl;
import dev.vality.fraudbusters.fraud.payment.aggregator.clickhouse.SumAggregatorImpl;
import dev.vality.fraudbusters.fraud.payment.aggregator.clickhouse.UniqueValueAggregatorImpl;
import dev.vality.fraudbusters.fraud.payment.resolver.DatabasePaymentFieldResolver;
import dev.vality.fraudbusters.fraud.payout.PayoutAggregators;
import dev.vality.fraudbusters.fraud.payout.PayoutInListFinder;
import dev.vality.fraudbusters.fraud.payout.PayoutModelFieldResolver;
import dev.vality.fraudbusters.repository.PaymentRepository;
import dev.vality.fraudbusters.repository.clickhouse.impl.ChargebackRepository;
import dev.vality.fraudbusters.repository.clickhouse.impl.PayoutHistoryRepository;
import dev.vality.fraudbusters.repository.clickhouse.impl.RefundRepository;
import dev.vality.fraudbusters.service.TimeBoundaryService;
import dev.vality.fraudo.aggregator.UniqueValueAggregator;
import dev.vality.fraudo.bundle.AggregatorBundle;
import dev.vality.fraudo.bundle.FinderBundle;
import dev.vality.fraudo.bundle.ResolverBundle;
import dev.vality.fraudo.bundle.VisitorBundle;
import dev.vality.fraudo.payment.aggregator.CountPaymentAggregator;
import dev.vality.fraudo.payment.aggregator.SumPaymentAggregator;
import dev.vality.fraudo.payment.factory.FraudVisitorFactoryImpl;
import dev.vality.fraudo.payment.visitor.impl.FirstFindVisitorImpl;
import dev.vality.fraudo.payout.validator.PayoutTemplateValidator;
import dev.vality.fraudo.resolver.CountryResolver;
import dev.vality.fraudo.resolver.FieldResolver;
import dev.vality.fraudo.resolver.impl.GroupFieldsResolverImpl;
import dev.vality.fraudo.resolver.impl.TimeWindowResolverImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PayoutFraudoConfig {

    @Bean
    public CountPaymentAggregator<PayoutModel, PaymentCheckedField> payoutRulesCountAggregator(
            PaymentRepository paymentRepository,
            RefundRepository refundRepository,
            ChargebackRepository chargebackRepository,
            DatabasePaymentFieldResolver databasePaymentFieldResolver,
            TimeBoundaryService timeBoundaryService) {
        return new CountAggregatorImpl<>(databasePaymentFieldResolver, paymentRepository, refundRepository,
                chargebackRepository, timeBoundaryService);
    }

    @Bean
    public SumPaymentAggregator<PayoutModel, PaymentCheckedField> payoutRulesSumAggregator(
            PaymentRepository paymentRepository,
            RefundRepository refundRepository,
            ChargebackRepository chargebackRepository,
            DatabasePaymentFieldResolver databasePaymentFieldResolver,
            TimeBoundaryService timeBoundaryService) {
        return new SumAggregatorImpl<>(databasePaymentFieldResolver, paymentRepository, refundRepository,
                chargebackRepository, timeBoundaryService);
    }

    @Bean
    public UniqueValueAggregator<PayoutModel, PaymentCheckedField> payoutRulesUniqueValueAggregator(
            PaymentRepository paymentRepository,
            DatabasePaymentFieldResolver databasePaymentFieldResolver,
            TimeBoundaryService timeBoundaryService) {
        return new UniqueValueAggregatorImpl<>(databasePaymentFieldResolver, paymentRepository, timeBoundaryService);
    }

    @Bean
    public PayoutAggregators<PayoutModel> payoutRulesPayoutAggregators(PayoutHistoryRepository repository,
                                                                       TimeBoundaryService timeBoundaryService) {
        return new PayoutAggregators<>(repository, timeBoundaryService);
    }

    @Bean
    public PayoutAggregators<PaymentModel> paymentRulesPayoutAggregators(PayoutHistoryRepository repository,
                                                                         TimeBoundaryService timeBoundaryService) {
        return new PayoutAggregators<>(repository, timeBoundaryService);
    }

    @Bean
    public FieldResolver<PayoutModel, PaymentCheckedField> payoutModelFieldResolver() {
        return new PayoutModelFieldResolver();
    }

    @Bean
    public FirstFindVisitorImpl<PayoutModel, PaymentCheckedField> payoutRuleVisitor(
            CountPaymentAggregator<PayoutModel, PaymentCheckedField> payoutRulesCountAggregator,
            SumPaymentAggregator<PayoutModel, PaymentCheckedField> payoutRulesSumAggregator,
            UniqueValueAggregator<PayoutModel, PaymentCheckedField> payoutRulesUniqueValueAggregator,
            CountryResolver<PaymentCheckedField> countryResolver,
            PayoutInListFinder payoutInListFinder,
            FieldResolver<PayoutModel, PaymentCheckedField> payoutModelFieldResolver,
            PayoutAggregators<PayoutModel> payoutRulesPayoutAggregators) {
        var aggregators = new AggregatorBundle<>(payoutRulesCountAggregator, payoutRulesSumAggregator,
                payoutRulesUniqueValueAggregator, payoutRulesPayoutAggregators, payoutRulesPayoutAggregators,
                payoutRulesPayoutAggregators);
        var resolvers = new ResolverBundle<>(countryResolver, payoutModelFieldResolver,
                new GroupFieldsResolverImpl<>(payoutModelFieldResolver), new TimeWindowResolverImpl(), null, null);
        return new FraudVisitorFactoryImpl().createVisitor(
                new VisitorBundle<>(aggregators, resolvers, new FinderBundle<>(payoutInListFinder)));
    }

    @Bean
    public FraudTemplateValidator payoutTemplateValidator() {
        return new PayoutTemplateValidator()::validate;
    }

}
