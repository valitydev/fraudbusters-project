package dev.vality.fraudbusters.management.utils;

import dev.vality.damsel.fraudbusters.TemplateReference;
import org.springframework.util.StringUtils;

public class ReferenceKeyGenerator {

    public static final String SEPARATOR = "_";
    public static final String GLOBAL = "GLOBAL";

    public static String generateTemplateKey(TemplateReference reference) {
        if (reference.is_global) {
            return GLOBAL;
        }
        return generateTemplateKey(reference.getPartyId(), reference.getShopId());
    }

    public static String generateTemplateKey(String partyId, String shopId) {
        if (!StringUtils.hasLength(shopId)
                && StringUtils.hasLength(partyId)) {
            return partyId;
        } else if (StringUtils.hasLength(shopId)
                && StringUtils.hasLength(partyId)) {
            return partyId + SEPARATOR + shopId;
        }
        return null;
    }

}
