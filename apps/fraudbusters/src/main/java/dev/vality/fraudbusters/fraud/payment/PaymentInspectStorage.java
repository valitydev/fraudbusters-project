package dev.vality.fraudbusters.fraud.payment;

import dev.vality.damsel.fraudbusters.GroupReference;
import dev.vality.damsel.fraudbusters.OperationType;
import dev.vality.damsel.fraudbusters.TemplateReference;
import dev.vality.fraudbusters.fraud.FraudTemplateValidator;
import dev.vality.fraudbusters.fraud.InspectStorage;
import dev.vality.fraudbusters.pool.Pool;
import dev.vality.fraudbusters.util.ReferenceKeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.ParserRuleContext;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class PaymentInspectStorage implements InspectStorage {

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
        return OperationType.payment;
    }

    @Override
    public String referenceKey(TemplateReference reference) {
        rejectWallet(reference.isSetWalletId());
        return ReferenceKeyGenerator.generateTemplateKey(reference);
    }

    @Override
    public String groupReferenceKey(GroupReference reference) {
        rejectWallet(reference.isSetWalletId());
        return ReferenceKeyGenerator.generateTemplateKeyByList(reference.getPartyId(), reference.getShopId());
    }

    @Override
    public void validateTemplate(String templateId, String template) {
        List<String> errors = templateValidator.validate(template);
        if (!CollectionUtils.isEmpty(errors)) {
            log.warn("TemplateListener templateId: {} validateError: {}", templateId, errors);
        }
    }

    private void rejectWallet(boolean walletIdSet) {
        if (walletIdSet) {
            throw new IllegalArgumentException("wallet_id is not supported for payments");
        }
    }

}
