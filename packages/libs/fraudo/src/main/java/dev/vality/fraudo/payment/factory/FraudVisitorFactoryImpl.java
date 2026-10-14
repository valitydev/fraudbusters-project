package dev.vality.fraudo.payment.factory;

import dev.vality.fraudo.converter.TrustConditionConverter;
import dev.vality.fraudo.bundle.AggregatorBundle;
import dev.vality.fraudo.bundle.FinderBundle;
import dev.vality.fraudo.bundle.ResolverBundle;
import dev.vality.fraudo.bundle.VisitorBundle;
import dev.vality.fraudo.model.BaseModel;
import dev.vality.fraudo.payment.visitor.*;
import dev.vality.fraudo.payment.visitor.impl.*;

public class FraudVisitorFactoryImpl implements FraudVisitorFactory {

    @Override
    public <T extends BaseModel, U> FirstFindVisitorImpl<T, U> createVisitor(VisitorBundle<T, U> visitorBundle) {
        AggregatorBundle<T, U> aggregatorBundle = visitorBundle.getAggregatorBundle();
        ResolverBundle<T, U> resolverBundle = visitorBundle.getResolverBundle();
        FinderBundle<T, U> finderBundle = visitorBundle.getFinderBundle();
        CountVisitor<T> countVisitor = new CountVisitorImpl<>(aggregatorBundle.getCountPaymentAggregator(),
                resolverBundle.getFieldPairResolver(),
                resolverBundle.getPaymentGroupResolver(),
                resolverBundle.getTimeWindowResolver());
        SumVisitor<T> sumVisitor = new SumVisitorImpl<>(aggregatorBundle.getSumPaymentAggregator(),
                resolverBundle.getFieldPairResolver(),
                resolverBundle.getPaymentGroupResolver(),
                resolverBundle.getTimeWindowResolver());
        ListVisitor<T> listVisitor = new ListVisitorImpl<>(finderBundle.getListFinder(),
                resolverBundle.getFieldPairResolver());
        CustomFuncVisitor<T> customFuncVisitor = new CustomFuncVisitorImpl<>(
                aggregatorBundle.getUniqueValueAggregator(),
                resolverBundle.getCountryResolver(),
                resolverBundle.getFieldPairResolver(),
                resolverBundle.getPaymentGroupResolver(),
                resolverBundle.getTimeWindowResolver(),
                resolverBundle.getPaymentTypeResolver());
        IsTrustedFuncVisitor<T> isTrustedFuncVisitor =
                new IsTrustedFuncVisitorImpl<>(resolverBundle.getCustomerTypeResolver());
        TrustConditionConverter trustConditionConverter = new TrustConditionConverter();

        return new FirstFindVisitorImpl<>(
                countVisitor,
                sumVisitor,
                listVisitor,
                customFuncVisitor,
                isTrustedFuncVisitor,
                resolverBundle.getFieldPairResolver(),
                trustConditionConverter
        );
    }

}
