package dev.vality.fraudbusters.config.payout;

import dev.vality.damsel.wb_list.WbListServiceSrv;
import dev.vality.fraudbusters.constant.PayoutHistoryColumn;
import dev.vality.fraudbusters.fraud.FraudTemplateValidator;
import dev.vality.fraudbusters.fraud.constant.PaymentCheckedField;
import dev.vality.fraudbusters.fraud.constant.PayoutCheckedField;
import dev.vality.fraudbusters.fraud.model.PaymentModel;
import dev.vality.fraudbusters.fraud.model.PayoutModel;
import dev.vality.fraudbusters.fraud.payment.CountryByIpResolver;
import dev.vality.fraudbusters.fraud.payment.aggregator.clickhouse.CountAggregatorImpl;
import dev.vality.fraudbusters.fraud.payment.aggregator.clickhouse.SumAggregatorImpl;
import dev.vality.fraudbusters.fraud.payment.aggregator.clickhouse.UniqueValueAggregatorImpl;
import dev.vality.fraudbusters.fraud.payment.resolver.DatabasePaymentFieldResolver;
import dev.vality.fraudbusters.fraud.payout.aggregator.clickhouse.PayoutAggregators;
import dev.vality.fraudbusters.fraud.payout.finder.PayoutInListFinderImpl;
import dev.vality.fraudbusters.fraud.payout.resolver.PayoutModelFieldResolver;
import dev.vality.fraudbusters.fraud.resolver.CountryResolverImpl;
import dev.vality.fraudbusters.fraud.resolver.HistoryFieldResolverImpl;
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
import dev.vality.fraudo.finder.InListFinder;
import dev.vality.fraudo.payment.aggregator.CountPaymentAggregator;
import dev.vality.fraudo.payment.aggregator.SumPaymentAggregator;
import dev.vality.fraudo.payment.factory.FraudVisitorFactoryImpl;
import dev.vality.fraudo.payment.visitor.impl.FirstFindVisitorImpl;
import dev.vality.fraudo.payout.validator.PayoutRuleFields;
import dev.vality.fraudo.payout.validator.PayoutTemplateValidator;
import dev.vality.fraudo.resolver.CountryResolver;
import dev.vality.fraudo.resolver.FieldResolver;
import dev.vality.fraudo.resolver.impl.GroupFieldsResolverImpl;
import dev.vality.fraudo.resolver.impl.TimeWindowResolverImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Configuration
public class PayoutFraudoConfig {

    @Bean
    public HistoryFieldResolverImpl<PayoutModel, PayoutCheckedField> payoutFieldsInPaymentHistory(
            DatabasePaymentFieldResolver databasePaymentFieldResolver,
            FieldResolver<PayoutModel, PayoutCheckedField> payoutModelFieldResolver) {
        return new HistoryFieldResolverImpl<>(PayoutCheckedField::getValue,
                fieldName -> databasePaymentFieldResolver.findColumn(PaymentCheckedField.getByValue(fieldName)),
                payoutModelFieldResolver);
    }

    @Bean
    public HistoryFieldResolverImpl<PayoutModel, PayoutCheckedField> payoutFieldsInPayoutHistory(
            FieldResolver<PayoutModel, PayoutCheckedField> payoutModelFieldResolver) {
        return new HistoryFieldResolverImpl<>(PayoutCheckedField::getValue, PayoutHistoryColumn::findColumn,
                payoutModelFieldResolver);
    }

    @Bean
    public HistoryFieldResolverImpl<PaymentModel, PaymentCheckedField> paymentFieldsInPayoutHistory(
            FieldResolver<PaymentModel, PaymentCheckedField> paymentModelFieldResolver) {
        return new HistoryFieldResolverImpl<>(PaymentCheckedField::getValue, PayoutHistoryColumn::findColumn,
                paymentModelFieldResolver);
    }

    @Bean
    public CountPaymentAggregator<PayoutModel, PayoutCheckedField> paymentHistoryCountAggregatorForPayouts(
            PaymentRepository paymentRepository,
            RefundRepository refundRepository,
            ChargebackRepository chargebackRepository,
            HistoryFieldResolverImpl<PayoutModel, PayoutCheckedField> payoutFieldsInPaymentHistory,
            TimeBoundaryService timeBoundaryService) {
        return new CountAggregatorImpl<>(payoutFieldsInPaymentHistory, paymentRepository, refundRepository,
                chargebackRepository, timeBoundaryService);
    }

    @Bean
    public SumPaymentAggregator<PayoutModel, PayoutCheckedField> paymentHistorySumAggregatorForPayouts(
            PaymentRepository paymentRepository,
            RefundRepository refundRepository,
            ChargebackRepository chargebackRepository,
            HistoryFieldResolverImpl<PayoutModel, PayoutCheckedField> payoutFieldsInPaymentHistory,
            TimeBoundaryService timeBoundaryService) {
        return new SumAggregatorImpl<>(payoutFieldsInPaymentHistory, paymentRepository, refundRepository,
                chargebackRepository, timeBoundaryService);
    }

    @Bean
    public UniqueValueAggregator<PayoutModel, PayoutCheckedField> paymentHistoryUniqueValueAggregatorForPayouts(
            PaymentRepository paymentRepository,
            HistoryFieldResolverImpl<PayoutModel, PayoutCheckedField> payoutFieldsInPaymentHistory,
            TimeBoundaryService timeBoundaryService) {
        return new UniqueValueAggregatorImpl<>(payoutFieldsInPaymentHistory, paymentRepository, timeBoundaryService);
    }

    @Bean
    public PayoutAggregators<PayoutModel, PayoutCheckedField> payoutHistoryAggregatorsForPayouts(
            PayoutHistoryRepository repository,
            TimeBoundaryService timeBoundaryService,
            HistoryFieldResolverImpl<PayoutModel, PayoutCheckedField> payoutFieldsInPayoutHistory) {
        return new PayoutAggregators<>(repository, timeBoundaryService, payoutFieldsInPayoutHistory);
    }

    @Bean
    public PayoutAggregators<PaymentModel, PaymentCheckedField> payoutHistoryAggregatorsForPayments(
            PayoutHistoryRepository repository,
            TimeBoundaryService timeBoundaryService,
            HistoryFieldResolverImpl<PaymentModel, PaymentCheckedField> paymentFieldsInPayoutHistory) {
        return new PayoutAggregators<>(repository, timeBoundaryService, paymentFieldsInPayoutHistory);
    }

    @Bean
    public FieldResolver<PayoutModel, PayoutCheckedField> payoutModelFieldResolver() {
        return new PayoutModelFieldResolver();
    }

    @Bean
    public CountryResolverImpl<PayoutCheckedField> payoutCountryResolver(CountryByIpResolver countryByIpResolver) {
        return new CountryResolverImpl<>(countryByIpResolver, PayoutCheckedField.IP, PayoutCheckedField.COUNTRY_BANK);
    }

    @Bean
    public InListFinder<PayoutModel, PayoutCheckedField> payoutInListFinder(
            WbListServiceSrv.Iface wbListServiceSrv,
            PayoutHistoryRepository repository,
            HistoryFieldResolverImpl<PayoutModel, PayoutCheckedField> payoutFieldsInPayoutHistory) {
        return new PayoutInListFinderImpl(wbListServiceSrv, repository, payoutFieldsInPayoutHistory);
    }

    @Bean
    public FirstFindVisitorImpl<PayoutModel, PayoutCheckedField> payoutRuleVisitor(
            CountPaymentAggregator<PayoutModel, PayoutCheckedField> paymentHistoryCountAggregatorForPayouts,
            SumPaymentAggregator<PayoutModel, PayoutCheckedField> paymentHistorySumAggregatorForPayouts,
            UniqueValueAggregator<PayoutModel, PayoutCheckedField> paymentHistoryUniqueValueAggregatorForPayouts,
            CountryResolver<PayoutCheckedField> payoutCountryResolver,
            InListFinder<PayoutModel, PayoutCheckedField> payoutInListFinder,
            FieldResolver<PayoutModel, PayoutCheckedField> payoutModelFieldResolver,
            PayoutAggregators<PayoutModel, PayoutCheckedField> payoutHistoryAggregatorsForPayouts) {
        var aggregators = new AggregatorBundle<>(paymentHistoryCountAggregatorForPayouts,
                paymentHistorySumAggregatorForPayouts, paymentHistoryUniqueValueAggregatorForPayouts,
                payoutHistoryAggregatorsForPayouts, payoutHistoryAggregatorsForPayouts,
                payoutHistoryAggregatorsForPayouts);
        var resolvers = new ResolverBundle<>(payoutCountryResolver, payoutModelFieldResolver,
                new GroupFieldsResolverImpl<>(payoutModelFieldResolver), new TimeWindowResolverImpl(), null, null);
        return new FraudVisitorFactoryImpl().createVisitor(
                new VisitorBundle<>(aggregators, resolvers, new FinderBundle<>(payoutInListFinder)));
    }

    @Bean
    public FraudTemplateValidator payoutTemplateValidator(
            CountryResolverImpl<PayoutCheckedField> payoutCountryResolver,
            HistoryFieldResolverImpl<PayoutModel, PayoutCheckedField> payoutFieldsInPaymentHistory,
            HistoryFieldResolverImpl<PayoutModel, PayoutCheckedField> payoutFieldsInPayoutHistory) {
        return new PayoutTemplateValidator(new PayoutRuleFields(
                collectFieldNames(field -> true),
                collectFieldNames(payoutCountryResolver::isSupported),
                collectFieldNames(payoutFieldsInPaymentHistory::isSupported),
                collectFieldNames(payoutFieldsInPayoutHistory::isSupported)))::validate;
    }

    private static Set<String> collectFieldNames(Predicate<PayoutCheckedField> supported) {
        return Arrays.stream(PayoutCheckedField.values())
                .filter(supported)
                .map(PayoutCheckedField::getValue)
                .collect(Collectors.toUnmodifiableSet());
    }

}
