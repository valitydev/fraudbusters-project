package dev.vality.fraudbusters.config.payment;

import dev.vality.damsel.wb_list.WbListServiceSrv;
import dev.vality.fraudbusters.fraud.constant.CheckedField;
import dev.vality.fraudbusters.fraud.localstorage.LocalPaymentFieldResolver;
import dev.vality.fraudbusters.fraud.localstorage.LocalResultStorageRepository;
import dev.vality.fraudbusters.fraud.localstorage.aggregator.LocalCountAggregatorDecorator;
import dev.vality.fraudbusters.fraud.localstorage.aggregator.LocalSumAggregatorDecorator;
import dev.vality.fraudbusters.fraud.localstorage.aggregator.LocalUniqueValueAggregatorDecorator;
import dev.vality.fraudbusters.fraud.model.PaymentModel;
import dev.vality.fraudbusters.fraud.payment.CountryByIpResolver;
import dev.vality.fraudbusters.fraud.payment.aggregator.clickhouse.CountAggregatorImpl;
import dev.vality.fraudbusters.fraud.payment.aggregator.clickhouse.SumAggregatorImpl;
import dev.vality.fraudbusters.fraud.payment.aggregator.clickhouse.UniqueValueAggregatorImpl;
import dev.vality.fraudbusters.fraud.payment.finder.PaymentInListFinderImpl;
import dev.vality.fraudbusters.fraud.payment.resolver.CountryResolverImpl;
import dev.vality.fraudbusters.fraud.payment.resolver.DatabasePaymentFieldResolver;
import dev.vality.fraudbusters.fraud.payment.resolver.PaymentModelFieldResolver;
import dev.vality.fraudbusters.repository.PaymentRepository;
import dev.vality.fraudbusters.repository.clickhouse.impl.ChargebackRepository;
import dev.vality.fraudbusters.repository.clickhouse.impl.RefundRepository;
import dev.vality.fraudbusters.service.TimeBoundaryService;
import dev.vality.fraudo.aggregator.UniqueValueAggregator;
import dev.vality.fraudbusters.fraud.payout.PayoutAggregators;
import dev.vality.fraudo.bundle.AggregatorBundle;
import dev.vality.fraudo.bundle.FinderBundle;
import dev.vality.fraudo.bundle.ResolverBundle;
import dev.vality.fraudo.bundle.VisitorBundle;
import dev.vality.fraudo.finder.InListFinder;
import dev.vality.fraudo.payment.aggregator.CountPaymentAggregator;
import dev.vality.fraudo.payment.aggregator.SumPaymentAggregator;
import dev.vality.fraudo.payment.factory.FraudVisitorFactoryImpl;
import dev.vality.fraudo.payment.factory.FullVisitorFactoryImpl;
import dev.vality.fraudo.payment.resolver.CustomerTypeResolver;
import dev.vality.fraudo.resolver.impl.GroupFieldsResolverImpl;
import dev.vality.fraudo.resolver.impl.TimeWindowResolverImpl;
import dev.vality.fraudo.payment.resolver.PaymentTypeResolver;
import dev.vality.fraudo.payment.visitor.impl.FirstFindVisitorImpl;
import dev.vality.fraudo.resolver.CountryResolver;
import dev.vality.fraudo.resolver.FieldResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentFraudoConfig {

    @Bean
    public CountPaymentAggregator<PaymentModel, CheckedField> countAggregator(
            PaymentRepository paymentRepository,
            RefundRepository refundRepository,
            ChargebackRepository chargebackRepository,
            DatabasePaymentFieldResolver databasePaymentFieldResolver,
            TimeBoundaryService timeBoundaryService) {
        return new CountAggregatorImpl<>(
                databasePaymentFieldResolver,
                paymentRepository,
                refundRepository,
                chargebackRepository,
                timeBoundaryService
        );
    }

    @Bean
    public SumPaymentAggregator<PaymentModel, CheckedField> sumAggregator(
            PaymentRepository paymentRepository,
            RefundRepository refundRepository,
            ChargebackRepository chargebackRepository,
            DatabasePaymentFieldResolver databasePaymentFieldResolver,
            TimeBoundaryService timeBoundaryService) {
        return new SumAggregatorImpl<>(
                databasePaymentFieldResolver,
                paymentRepository,
                refundRepository,
                chargebackRepository,
                timeBoundaryService
        );
    }

    @Bean
    public UniqueValueAggregator<PaymentModel, CheckedField> uniqueValueAggregator(
            PaymentRepository paymentRepository,
            DatabasePaymentFieldResolver databasePaymentFieldResolver,
            TimeBoundaryService timeBoundaryService) {
        return new UniqueValueAggregatorImpl<>(databasePaymentFieldResolver, paymentRepository, timeBoundaryService);
    }

    @Bean
    public CountryResolver<CheckedField> countryResolver(CountryByIpResolver countryByIpResolver) {
        return new CountryResolverImpl(countryByIpResolver);
    }

    @Bean
    public FieldResolver<PaymentModel, CheckedField> paymentModelFieldResolver() {
        return new PaymentModelFieldResolver();
    }

    @Bean
    public LocalPaymentFieldResolver localPaymentFieldResolver(
            DatabasePaymentFieldResolver databasePaymentFieldResolver) {
        return new LocalPaymentFieldResolver(databasePaymentFieldResolver);
    }

    @Bean
    public InListFinder<PaymentModel, CheckedField> paymentInListFinder(
            WbListServiceSrv.Iface wbListServiceSrv,
            PaymentRepository paymentRepository,
            DatabasePaymentFieldResolver databasePaymentFieldResolver) {
        return new PaymentInListFinderImpl(wbListServiceSrv, databasePaymentFieldResolver, paymentRepository);
    }

    @Bean
    public FirstFindVisitorImpl<PaymentModel, CheckedField> paymentRuleVisitor(
            CountPaymentAggregator<PaymentModel, CheckedField> countAggregator,
            SumPaymentAggregator<PaymentModel, CheckedField> sumAggregator,
            UniqueValueAggregator<PaymentModel, CheckedField> uniqueValueAggregator,
            CountryResolver<CheckedField> countryResolver,
            InListFinder<PaymentModel, CheckedField> paymentInListFinder,
            FieldResolver<PaymentModel, CheckedField> paymentModelFieldResolver,
            PaymentTypeResolver<PaymentModel> paymentTypeResolver,
            CustomerTypeResolver<PaymentModel> customerTypeResolver,
            PayoutAggregators<PaymentModel> payoutHistoryAggregatorsForPayments) {
        AggregatorBundle<PaymentModel, CheckedField> aggregatorBundle =
                new AggregatorBundle<>(countAggregator, sumAggregator, uniqueValueAggregator,
                        payoutHistoryAggregatorsForPayments, payoutHistoryAggregatorsForPayments,
                        payoutHistoryAggregatorsForPayments);
        ResolverBundle<PaymentModel, CheckedField> resolverBundle = new ResolverBundle<>(
                countryResolver,
                paymentModelFieldResolver,
                new GroupFieldsResolverImpl<>(paymentModelFieldResolver),
                new TimeWindowResolverImpl(),
                paymentTypeResolver,
                customerTypeResolver
        );
        FinderBundle<PaymentModel, CheckedField> finderBundle = new FinderBundle<>(paymentInListFinder);
        return new FraudVisitorFactoryImpl()
                .createVisitor(new VisitorBundle<>(aggregatorBundle, resolverBundle, finderBundle));
    }

    @Bean
    public CountPaymentAggregator<PaymentModel, CheckedField> countResultAggregator(
            LocalResultStorageRepository localResultStorageRepository,
            PaymentRepository paymentRepositoryImpl,
            RefundRepository refundRepository,
            ChargebackRepository chargebackRepository,
            DatabasePaymentFieldResolver databasePaymentFieldResolver,
            LocalPaymentFieldResolver localPaymentFieldResolver,
            TimeBoundaryService timeBoundaryService) {

        CountAggregatorImpl<PaymentModel> countAggregatorDecorator = new CountAggregatorImpl<>(
                databasePaymentFieldResolver,
                paymentRepositoryImpl,
                refundRepository,
                chargebackRepository,
                timeBoundaryService
        );
        return new LocalCountAggregatorDecorator(
                countAggregatorDecorator,
                localPaymentFieldResolver,
                localResultStorageRepository,
                timeBoundaryService
        );
    }

    @Bean
    public SumPaymentAggregator<PaymentModel, CheckedField> sumResultAggregator(
            LocalResultStorageRepository localResultStorageRepository,
            PaymentRepository paymentRepositoryImpl,
            RefundRepository refundRepository,
            ChargebackRepository chargebackRepository,
            DatabasePaymentFieldResolver databasePaymentFieldResolver,
            LocalPaymentFieldResolver localPaymentFieldResolver,
            TimeBoundaryService timeBoundaryService) {

        SumAggregatorImpl<PaymentModel> sumAggregator = new SumAggregatorImpl<>(
                databasePaymentFieldResolver,
                paymentRepositoryImpl,
                refundRepository,
                chargebackRepository,
                timeBoundaryService
        );
        return new LocalSumAggregatorDecorator(
                sumAggregator,
                localPaymentFieldResolver,
                localResultStorageRepository,
                timeBoundaryService
        );
    }

    @Bean
    public UniqueValueAggregator<PaymentModel, CheckedField> uniqueValueResultAggregator(
            LocalResultStorageRepository localResultStorageRepository,
            PaymentRepository fraudResultRepository,
            DatabasePaymentFieldResolver databasePaymentFieldResolver,
            LocalPaymentFieldResolver localPaymentFieldResolver,
            TimeBoundaryService timeBoundaryService) {
        UniqueValueAggregatorImpl<PaymentModel> uniqueValueAggregator =
                new UniqueValueAggregatorImpl<>(databasePaymentFieldResolver, fraudResultRepository,
                        timeBoundaryService);

        return new LocalUniqueValueAggregatorDecorator(
                uniqueValueAggregator,
                localPaymentFieldResolver,
                localResultStorageRepository,
                timeBoundaryService
        );
    }

    @Bean
    public FirstFindVisitorImpl<PaymentModel, CheckedField> fullPaymentRuleVisitor(
            CountPaymentAggregator<PaymentModel, CheckedField> countResultAggregator,
            SumPaymentAggregator<PaymentModel, CheckedField> sumResultAggregator,
            UniqueValueAggregator<PaymentModel, CheckedField> uniqueValueResultAggregator,
            CountryResolver<CheckedField> countryResolver,
            InListFinder<PaymentModel, CheckedField> paymentInListFinder,
            FieldResolver<PaymentModel, CheckedField> paymentModelFieldResolver,
            PaymentTypeResolver<PaymentModel> paymentTypeResolver,
            CustomerTypeResolver<PaymentModel> customerTypeResolver,
            PayoutAggregators<PaymentModel> payoutHistoryAggregatorsForPayments) {
        AggregatorBundle<PaymentModel, CheckedField> aggregatorBundle =
                new AggregatorBundle<>(countResultAggregator, sumResultAggregator, uniqueValueResultAggregator,
                        payoutHistoryAggregatorsForPayments, payoutHistoryAggregatorsForPayments,
                        payoutHistoryAggregatorsForPayments);
        ResolverBundle<PaymentModel, CheckedField> resolverBundle = new ResolverBundle<>(
                countryResolver,
                paymentModelFieldResolver,
                new GroupFieldsResolverImpl<>(paymentModelFieldResolver),
                new TimeWindowResolverImpl(),
                paymentTypeResolver,
                customerTypeResolver
        );
        FinderBundle<PaymentModel, CheckedField> finderBundle = new FinderBundle<>(paymentInListFinder);
        return new FullVisitorFactoryImpl()
                .createVisitor(new VisitorBundle<>(aggregatorBundle, resolverBundle, finderBundle));
    }

}
