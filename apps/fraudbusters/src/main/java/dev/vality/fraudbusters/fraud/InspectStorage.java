package dev.vality.fraudbusters.fraud;

import dev.vality.damsel.fraudbusters.GroupReference;
import dev.vality.damsel.fraudbusters.OperationType;
import dev.vality.damsel.fraudbusters.TemplateReference;
import dev.vality.fraudbusters.pool.Pool;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.List;

/**
 * In-memory storage of inspection rules (templates, groups and their references) for one operation type,
 * with its reference key conventions and acceptance validation.
 * Command listeners select an implementation by the command operation type.
 */
public interface InspectStorage {

    OperationType getOperationType();

    Pool<ParserRuleContext> getTemplatePool();

    Pool<List<String>> getGroupPool();

    Pool<String> getReferencePool();

    Pool<String> getGroupReferencePool();

    String referenceKey(TemplateReference reference);

    String groupReferenceKey(GroupReference reference);

    FraudTemplateValidator getTemplateValidator();

    void validateTemplate(String templateId, String template);

}
