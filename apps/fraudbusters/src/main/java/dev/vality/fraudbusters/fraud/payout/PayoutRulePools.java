package dev.vality.fraudbusters.fraud.payout;

import dev.vality.fraudbusters.pool.Pool;
import dev.vality.fraudbusters.pool.PoolImpl;
import lombok.Getter;
import org.antlr.v4.runtime.ParserRuleContext;
import org.springframework.stereotype.Component;

import java.util.List;

/** Separate stores preserve every existing payment key, including arbitrary template IDs. */
@Getter
@Component
public class PayoutRulePools {

    private final Pool<ParserRuleContext> templates = new PoolImpl<>("payout-template");
    private final Pool<List<String>> groups = new PoolImpl<>("payout-group");
    private final Pool<String> references = new PoolImpl<>("payout-reference");
    private final Pool<String> groupReferences = new PoolImpl<>("payout-group-reference");
}
