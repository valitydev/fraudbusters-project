package dev.vality.fraudbusters.config.payout;

import dev.vality.fraudbusters.fraud.constant.PaymentCheckedField;
import dev.vality.fraudbusters.fraud.model.PaymentModel;
import dev.vality.fraudbusters.fraud.payout.PayoutAggregators;
import dev.vality.fraudbusters.fraud.payout.PayoutInListFinder;
import dev.vality.fraudo.aggregator.UniqueValueAggregator;
import dev.vality.fraudo.bundle.AggregatorBundle;
import dev.vality.fraudo.bundle.FinderBundle;
import dev.vality.fraudo.bundle.ResolverBundle;
import dev.vality.fraudo.bundle.VisitorBundle;
import dev.vality.fraudo.payment.aggregator.CountPaymentAggregator;
import dev.vality.fraudo.payment.aggregator.SumPaymentAggregator;
import dev.vality.fraudo.payment.factory.FraudVisitorFactoryImpl;
import dev.vality.fraudo.payment.visitor.impl.FirstFindVisitorImpl;
import dev.vality.fraudo.resolver.CountryResolver;
import dev.vality.fraudo.resolver.FieldResolver;
import dev.vality.fraudo.resolver.impl.GroupFieldsResolverImpl;
import dev.vality.fraudo.resolver.impl.TimeWindowResolverImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PayoutFraudoConfig {

    @Bean
    public FirstFindVisitorImpl<PaymentModel, PaymentCheckedField> payoutRuleVisitor(
            CountPaymentAggregator<PaymentModel, PaymentCheckedField> countAggregator,
            SumPaymentAggregator<PaymentModel, PaymentCheckedField> sumAggregator,
            UniqueValueAggregator<PaymentModel, PaymentCheckedField> uniqueValueAggregator,
            CountryResolver<PaymentCheckedField> countryResolver,
            PayoutInListFinder payoutInListFinder,
            FieldResolver<PaymentModel, PaymentCheckedField> paymentModelFieldResolver,
            PayoutAggregators payoutAggregators) {
        var aggregators = new AggregatorBundle<>(countAggregator, sumAggregator, uniqueValueAggregator,
                payoutAggregators, payoutAggregators, payoutAggregators);
        var resolvers = new ResolverBundle<>(countryResolver, paymentModelFieldResolver,
                new GroupFieldsResolverImpl<>(paymentModelFieldResolver), new TimeWindowResolverImpl(), null, null);
        return new FraudVisitorFactoryImpl().createVisitor(
                new VisitorBundle<>(aggregators, resolvers, new FinderBundle<>(payoutInListFinder)));
    }

}
