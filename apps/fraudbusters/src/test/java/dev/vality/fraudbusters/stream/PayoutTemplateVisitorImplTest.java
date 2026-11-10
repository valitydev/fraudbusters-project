package dev.vality.fraudbusters.stream;

import dev.vality.fraudbusters.domain.CheckedResultModel;
import dev.vality.fraudbusters.domain.ConcreteResultModel;
import dev.vality.fraudbusters.fraud.model.PayoutModel;
import dev.vality.fraudbusters.pool.Pool;
import dev.vality.fraudbusters.pool.PoolImpl;
import dev.vality.fraudbusters.stream.impl.PayoutTemplateVisitorImpl;
import dev.vality.fraudbusters.stream.impl.RuleCascade;
import dev.vality.fraudbusters.util.PayoutScope;
import dev.vality.fraudo.constant.ResultStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PayoutTemplateVisitorImplTest {

    private static final String PARTY = "synthetic-party";
    private static final String WALLET = "synthetic-wallet";
    private static final String PARTY_KEY = PayoutScope.generateKey(PARTY, null);
    private static final String WALLET_KEY = PayoutScope.generateKey(PARTY, WALLET);

    /** The order in which a withdrawal meets its templates. */
    private static final List<String> CASCADE = List.of(
            "global-template",
            "global-group-template",
            "party-group-template",
            "wallet-group-template",
            "party-template",
            "wallet-template");

    private final Map<String, ResultStatus> decisions = new HashMap<>();
    private final List<String> applied = new ArrayList<>();
    private final Pool<List<String>> groups = new PoolImpl<>("payout-group");
    private final Pool<String> references = new PoolImpl<>("payout-reference");
    private final Pool<String> groupReferences = new PoolImpl<>("payout-group-reference");
    private PayoutTemplateVisitorImpl visitor;

    @BeforeEach
    void setUp() {
        RuleApplier<PayoutModel> applier = new RuleApplier<>() {
            @Override
            public Optional<CheckedResultModel> apply(PayoutModel model, String templateKey) {
                if (templateKey == null) {
                    return Optional.empty();
                }
                applied.add(templateKey);
                return Optional.ofNullable(decisions.get(templateKey))
                        .map(status -> checked(templateKey, status));
            }

            @Override
            public Optional<CheckedResultModel> applyForAny(PayoutModel model, List<String> templateKeys) {
                if (templateKeys == null) {
                    return Optional.empty();
                }
                return templateKeys.stream()
                        .map(key -> apply(model, key))
                        .flatMap(Optional::stream)
                        .findFirst();
            }
        };
        visitor = new PayoutTemplateVisitorImpl(new RuleCascade<>(applier, groups, references, groupReferences));

        references.add(PayoutScope.GLOBAL, "global-template");
        references.add(PARTY_KEY, "party-template");
        references.add(WALLET_KEY, "wallet-template");
        groupReferences.add(PayoutScope.GLOBAL, "global-group");
        groupReferences.add(PARTY_KEY, "party-group");
        groupReferences.add(WALLET_KEY, "wallet-group");
        groups.add("global-group", List.of("global-group-template"));
        groups.add("party-group", List.of("party-group-template"));
        groups.add("wallet-group", List.of("wallet-group-template"));
    }

    @Test
    void everyLevelDecidesInCascadeOrder() {
        CASCADE.forEach(template -> decisions.put(template, ResultStatus.DECLINE));
        for (String expected : CASCADE) {
            assertEquals(expected, visitor.visit(model(WALLET)).getCheckedTemplate());
            decisions.remove(expected);
        }
        CheckedResultModel undecided = visitor.visit(model(WALLET));
        assertEquals("RULE_NOT_CHECKED", undecided.getCheckedTemplate());
        assertEquals(ResultStatus.HIGH_RISK, undecided.getResultModel().getResultStatus());
    }

    @Test
    void laterLevelsAreNotEvaluatedOnceALevelDecides() {
        decisions.put("party-group-template", ResultStatus.ACCEPT);
        assertEquals("party-group-template", visitor.visit(model(WALLET)).getCheckedTemplate());
        assertEquals(List.of("global-template", "global-group-template", "party-group-template"), applied);
    }

    @Test
    void anotherWalletOfThePartyGetsOnlyPartyAndGlobalLevels() {
        CASCADE.forEach(template -> decisions.put(template, ResultStatus.DECLINE));
        decisions.remove("global-template");
        decisions.remove("global-group-template");
        decisions.remove("party-group-template");
        assertEquals("party-template", visitor.visit(model("other-wallet")).getCheckedTemplate());
        assertEquals(List.of("global-template", "global-group-template", "party-group-template", "party-template"),
                applied);
    }

    @Test
    void partyAndWalletAreRequired() {
        PayoutModel withoutWallet = model(null);
        assertThrows(IllegalArgumentException.class, () -> visitor.visit(withoutWallet));
        PayoutModel withoutParty = model(WALLET);
        withoutParty.setPartyId(" ");
        assertThrows(IllegalArgumentException.class, () -> visitor.visit(withoutParty));
    }

    private PayoutModel model(String wallet) {
        PayoutModel model = new PayoutModel();
        model.setPartyId(PARTY);
        model.setWalletId(wallet);
        return model;
    }

    private static CheckedResultModel checked(String template, ResultStatus status) {
        ConcreteResultModel result = new ConcreteResultModel();
        result.setResultStatus(status);
        CheckedResultModel checked = new CheckedResultModel();
        checked.setResultModel(result);
        checked.setCheckedTemplate(template);
        return checked;
    }
}
