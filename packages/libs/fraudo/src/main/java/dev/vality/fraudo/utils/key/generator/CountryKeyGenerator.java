package dev.vality.fraudo.utils.key.generator;

import dev.vality.fraudo.FraudoRulesParser;
import dev.vality.fraudo.utils.TextUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CountryKeyGenerator {

    public static String generate(FraudoRulesParser.Country_byContext ctx) {
        String fieldName = TextUtil.safeGetText(ctx.STRING());
        return new StringBuilder()
                .append(ctx.children.get(0))
                .append(fieldName)
                .toString();
    }

}
