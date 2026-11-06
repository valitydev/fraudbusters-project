package dev.vality.fraudo;

import dev.vality.fraudo.aggregator.UniqueValueAggregator;
import dev.vality.fraudo.bundle.AggregatorBundle;
import dev.vality.fraudo.bundle.FinderBundle;
import dev.vality.fraudo.bundle.ResolverBundle;
import dev.vality.fraudo.bundle.VisitorBundle;
import dev.vality.fraudo.constant.ResultStatus;
import dev.vality.fraudo.exception.UnresolvableFieldException;
import dev.vality.fraudo.model.BaseModel;
import dev.vality.fraudo.model.ResultModel;
import dev.vality.fraudo.model.TimeWindow;
import dev.vality.fraudo.payment.aggregator.CountPaymentAggregator;
import dev.vality.fraudo.payment.aggregator.SumPaymentAggregator;
import dev.vality.fraudo.payment.factory.FraudVisitorFactory;
import dev.vality.fraudo.payment.factory.FraudVisitorFactoryImpl;
import dev.vality.fraudo.payment.factory.FullVisitorFactoryImpl;
import dev.vality.fraudo.payment.visitor.impl.FirstFindVisitorImpl;
import dev.vality.fraudo.resolver.impl.GroupFieldsResolverImpl;
import dev.vality.fraudo.resolver.impl.TimeWindowResolverImpl;
import dev.vality.fraudo.payout.aggregator.CountPayoutAggregator;
import dev.vality.fraudo.payout.aggregator.SumPayoutAggregator;
import dev.vality.fraudo.resolver.FieldResolver;
import dev.vality.fraudo.utils.ResultUtils;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public abstract class AbstractPayoutTest {

    @Mock
    protected CountPayoutAggregator<BaseModel, String> payouts;
    @Mock
    protected SumPayoutAggregator<BaseModel, String> payoutSums;
    @Mock
    protected UniqueValueAggregator<BaseModel, String> payoutUnique;
    @Mock
    protected CountPaymentAggregator<BaseModel, String> payments;
    @Mock
    protected SumPaymentAggregator<BaseModel, String> paymentSums;
    @Mock
    protected UniqueValueAggregator<BaseModel, String> paymentUnique;

    protected final BaseModel model = new BaseModel();
    protected final TimeWindow window = TimeWindow.builder().start(24).timeUnit("hours").build();
    protected final FieldResolver<BaseModel, String> fields = new FieldResolver<>() {
        @Override
        public String resolveValue(String fieldName, BaseModel value) {
            return value.getUserId();
        }

        @Override
        public String resolveName(String fieldName) {
            if (!List.of("user_id", "party_id", "wallet_id", "currency", "card_token").contains(fieldName)) {
                throw new UnresolvableFieldException(fieldName);
            }
            return fieldName;
        }
    };

    protected FraudoRulesParser.ParseContext getParseContext(String resource) throws IOException {
        try (var stream = getClass().getResourceAsStream(resource)) {
            assertNotNull(stream, resource);
            var lexer = new FraudoRulesLexer(CharStreams.fromStream(stream));
            var parser = new FraudoRulesParser(new CommonTokenStream(lexer));
            var tree = parser.parse();
            assertEquals(0, parser.getNumberOfSyntaxErrors());
            return tree;
        }
    }

    protected FirstFindVisitorImpl<BaseModel, String> createVisitor(boolean fullVisitor) {
        var aggregators = new AggregatorBundle<>(payments, paymentSums, paymentUnique,
                payouts, payoutSums, payoutUnique);
        var resolvers = new ResolverBundle<>(null, fields, new GroupFieldsResolverImpl<>(fields),
                new TimeWindowResolverImpl(), null, null);
        var bundle = new VisitorBundle<>(aggregators, resolvers, new FinderBundle<BaseModel, String>(null));
        FraudVisitorFactory factory = fullVisitor ? new FullVisitorFactoryImpl() : new FraudVisitorFactoryImpl();
        return factory.createVisitor(bundle);
    }

    protected ResultModel evaluate(String resource, boolean fullVisitor) throws IOException {
        var parseContext = getParseContext(resource);
        try (var visitor = createVisitor(fullVisitor)) {
            return visitor.visit(parseContext, model);
        }
    }

    protected void assertStatus(ResultStatus expected, ResultModel result) {
        assertEquals(expected, ResultUtils.findFirstNotNotifyStatus(result).orElseThrow().getResultStatus());
    }
}
