package dev.vality.fraudo;

import dev.vality.fraudo.FraudoRulesParser.ParseContext;
import dev.vality.fraudo.aggregator.UniqueValueAggregator;
import dev.vality.fraudo.bundle.AggregatorBundle;
import dev.vality.fraudo.bundle.FinderBundle;
import dev.vality.fraudo.bundle.ResolverBundle;
import dev.vality.fraudo.bundle.VisitorBundle;
import dev.vality.fraudo.finder.InListFinder;
import dev.vality.fraudo.model.ResultModel;
import dev.vality.fraudo.FraudoRulesParser;
import dev.vality.fraudo.FraudoRulesLexer;
import dev.vality.fraudo.payment.aggregator.CountPaymentAggregator;
import dev.vality.fraudo.payment.aggregator.SumPaymentAggregator;
import dev.vality.fraudo.payment.factory.FraudVisitorFactoryImpl;
import dev.vality.fraudo.payment.factory.FullVisitorFactoryImpl;
import dev.vality.fraudo.payment.resolver.CustomerTypeResolver;
import dev.vality.fraudo.resolver.impl.GroupFieldsResolverImpl;
import dev.vality.fraudo.resolver.impl.TimeWindowResolverImpl;
import dev.vality.fraudo.payment.resolver.PaymentTypeResolver;
import dev.vality.fraudo.resolver.CountryResolver;
import dev.vality.fraudo.resolver.FieldResolver;
import dev.vality.fraudo.test.constant.PaymentCheckedField;
import dev.vality.fraudo.test.model.PaymentModel;
import dev.vality.fraudo.test.payment.PaymentModelFieldResolver;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;

@ExtendWith(MockitoExtension.class)
public class AbstractPaymentTest {

    @Mock
    CountPaymentAggregator<PaymentModel, PaymentCheckedField> countPaymentAggregator;
    @Mock
    SumPaymentAggregator<PaymentModel, PaymentCheckedField> sumPaymentAggregator;
    @Mock
    UniqueValueAggregator<PaymentModel, PaymentCheckedField> uniqueValueAggregator;
    @Mock
    CountryResolver<PaymentCheckedField> countryResolver;
    @Mock
    InListFinder<PaymentModel, PaymentCheckedField> inListFinder;
    @Mock
    TimeWindowResolverImpl timeWindowResolver;
    @Mock
    PaymentTypeResolver<PaymentModel> paymentModelPaymentTypeResolver;
    @Mock
    CustomerTypeResolver<PaymentModel> customerTypeResolver;

    private final FieldResolver<PaymentModel, PaymentCheckedField> fieldResolver = new PaymentModelFieldResolver();
    private final GroupFieldsResolverImpl<PaymentModel, PaymentCheckedField> groupFieldsResolver =
            new GroupFieldsResolverImpl<>(fieldResolver);

    ResultModel parseAndVisit(InputStream resourceAsStream) throws IOException {
        ParseContext parse = getParseContext(resourceAsStream);
        return invokeParse(parse);
    }

    ResultModel invokeParse(ParseContext parse) {
        PaymentModel model = new PaymentModel();
        return invoke(parse, model);
    }

    ResultModel invoke(ParseContext parse, PaymentModel model) {
        VisitorBundle<PaymentModel, PaymentCheckedField> visitorBundle = buildVisitorDto();
        return new FraudVisitorFactoryImpl()
                .createVisitor(visitorBundle)
                .visit(parse, model);
    }

    ResultModel invokeFullVisitor(ParseContext parse, PaymentModel model) {
        VisitorBundle<PaymentModel, PaymentCheckedField> visitorBundle = buildVisitorDto();
        return new FullVisitorFactoryImpl()
                .createVisitor(visitorBundle)
                .visit(parse, model);
    }

    private VisitorBundle<PaymentModel, PaymentCheckedField> buildVisitorDto() {
        var aggregatorBundle = new AggregatorBundle<>(
                countPaymentAggregator,
                sumPaymentAggregator,
                uniqueValueAggregator, null, null, null);
        var resolverBundle = new ResolverBundle<>(
                countryResolver,
                fieldResolver,
                groupFieldsResolver,
                timeWindowResolver,
                paymentModelPaymentTypeResolver,
                customerTypeResolver);
        var finderBundle = new FinderBundle<>(inListFinder);
        return new VisitorBundle<>(aggregatorBundle, resolverBundle, finderBundle);
    }

    ParseContext getParseContext(InputStream resourceAsStream) throws IOException {
        FraudoRulesLexer lexer = new FraudoRulesLexer(new ANTLRInputStream(resourceAsStream));
        FraudoRulesParser parser = new FraudoRulesParser(new CommonTokenStream(lexer));
        return parser.parse();
    }

}
