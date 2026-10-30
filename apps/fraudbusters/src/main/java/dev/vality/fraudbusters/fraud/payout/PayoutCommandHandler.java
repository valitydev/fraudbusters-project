package dev.vality.fraudbusters.fraud.payout;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.CommandType;
import dev.vality.damsel.fraudbusters.PriorityId;
import dev.vality.fraudbusters.fraud.payment.PaymentContextParserImpl;
import dev.vality.fraudbusters.pool.Pool;
import dev.vality.fraudo.payout.validator.PayoutTemplateValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class PayoutCommandHandler {

    private final PayoutRulePools pools;
    private final PaymentContextParserImpl parser;

    public void handle(Command command) {
        var body = command.getCommandBody();
        if (body.isSetTemplate()) {
            var template = body.getTemplate();
            update(command, pools.getTemplates(), template.getId(), () -> {
                String source = new String(template.getTemplate(), StandardCharsets.UTF_8);
                var errors = new PayoutTemplateValidator().validate(source);
                if (!errors.isEmpty()) {
                    throw new IllegalArgumentException("Invalid payout template " + template.getId() + ": " + errors);
                }
                return parser.parse(source);
            });
        } else if (body.isSetGroup()) {
            var group = body.getGroup();
            update(command, pools.getGroups(), group.getGroupId(), () -> {
                var ids = group.getTemplateIds().stream()
                        .sorted(Comparator.comparingLong(PriorityId::getPriority))
                        .map(PriorityId::getId).toList();
                if (ids.stream().distinct().count() != ids.size()) {
                    throw new IllegalArgumentException("Duplicate payout template in group");
                }
                return ids;
            });
        } else if (body.isSetReference()) {
            var reference = body.getReference();
            String key;
            if (reference.isIsGlobal()) {
                key = PayoutScope.GLOBAL;
            } else {
                PayoutScope.validate(reference.getShopId(), reference.getPartyId(), reference.getWalletId());
                key = !StringUtils.hasText(reference.getPartyId()) && !StringUtils.hasText(reference.getWalletId())
                        ? "DEFAULT" : PayoutScope.key(reference.getPartyId(), reference.getWalletId());
            }
            update(command, pools.getReferences(), key, reference::getTemplateId);
        } else if (body.isSetGroupReference()) {
            var reference = body.getGroupReference();
            PayoutScope.validate(reference.getShopId(), reference.getPartyId(), reference.getWalletId());
            update(command, pools.getGroupReferences(),
                    PayoutScope.key(reference.getPartyId(), reference.getWalletId()),
                    reference::getGroupId);
        }
    }

    private <T> void update(Command command, Pool<T> pool, String key, Supplier<T> value) {
        if (command.getCommandType() == CommandType.CREATE) {
            pool.add(key, value.get());
        } else if (command.getCommandType() == CommandType.DELETE) {
            pool.remove(key);
        } else {
            throw new IllegalArgumentException("Unsupported command type");
        }
    }
}
