package dev.vality.fraudbusters.fraud.payout;

import dev.vality.damsel.fraudbusters.GroupReference;
import dev.vality.damsel.fraudbusters.OperationType;
import dev.vality.damsel.fraudbusters.TemplateReference;
import dev.vality.fraudbusters.constant.TemplateLevel;
import dev.vality.fraudbusters.fraud.FraudTemplateValidator;
import dev.vality.fraudbusters.fraud.InspectStorage;
import dev.vality.fraudbusters.pool.Pool;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.springframework.util.StringUtils;

import java.util.List;

/** Separate pools preserve every existing payment key, including arbitrary template IDs. */
@RequiredArgsConstructor
public class PayoutInspectStorage implements InspectStorage {

    private final Pool<ParserRuleContext> templatePool;
    private final Pool<List<String>> groupPool;
    private final Pool<String> referencePool;
    private final Pool<String> groupReferencePool;
    private final FraudTemplateValidator templateValidator;

    @Override
    public Pool<ParserRuleContext> getTemplatePool() {
        return templatePool;
    }

    @Override
    public Pool<List<String>> getGroupPool() {
        return groupPool;
    }

    @Override
    public Pool<String> getReferencePool() {
        return referencePool;
    }

    @Override
    public Pool<String> getGroupReferencePool() {
        return groupReferencePool;
    }

    @Override
    public FraudTemplateValidator getTemplateValidator() {
        return templateValidator;
    }

    @Override
    public OperationType getOperationType() {
        return OperationType.payout;
    }

    @Override
    public String referenceKey(TemplateReference reference) {
        if (reference.isIsGlobal()) {
            return PayoutScope.GLOBAL;
        }
        PayoutScope.validate(reference.getShopId(), reference.getPartyId(), reference.getWalletId());
        return !StringUtils.hasText(reference.getPartyId()) && !StringUtils.hasText(reference.getWalletId())
                ? TemplateLevel.DEFAULT.name()
                : PayoutScope.key(reference.getPartyId(), reference.getWalletId());
    }

    @Override
    public String groupReferenceKey(GroupReference reference) {
        PayoutScope.validate(reference.getShopId(), reference.getPartyId(), reference.getWalletId());
        return PayoutScope.key(reference.getPartyId(), reference.getWalletId());
    }

    @Override
    public void validateTemplate(String templateId, String template) {
        List<String> errors = templateValidator.validate(template);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Invalid payout template " + templateId + ": " + errors);
        }
    }

}
