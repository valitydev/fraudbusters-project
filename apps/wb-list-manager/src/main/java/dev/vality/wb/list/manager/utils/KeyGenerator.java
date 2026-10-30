package dev.vality.wb.list.manager.utils;

import dev.vality.damsel.wb_list.ListType;
import dev.vality.damsel.wb_list.PaymentId;
import dev.vality.damsel.wb_list.PayoutId;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KeyGenerator {

    private static final String DELIMITER = "_";

    public static String generateKey(dev.vality.damsel.wb_list.Row row) {
        if (row.isSetId() && row.getId().isSetPayoutId()) {
            PayoutId id = row.getId().getPayoutId();
            return generatePayoutKey(row.getListType(), row.getListName(), row.getValue(),
                    id.getPartyId(), id.getWalletId());
        }
        if (row.isSetId() && row.getId().isSetPaymentId()) {
            PaymentId paymentId = row.getId().getPaymentId();
            return generateKey(row.getListType(), row.getListName(), row.getValue(), paymentId.getPartyId(),
                    paymentId.getShopId());
        } else if (row.isSetId() && row.getId().isSetP2pId()) {
            throw new IllegalStateException("P2P is not supported. Row: " + row);
        }
        return generateKey(row.getListType(), row.getListName(), row.getValue(), row.getPartyId(), row.getShopId());
    }

    public static String generateKey(ListType listType, String listName, String value, String... params) {
        StringBuilder stringBuilder = new StringBuilder();
        if (params != null && params.length != 0) {
            for (String param : params) {
                addIfExist(param, stringBuilder);
            }
        }
        return stringBuilder
                .append(listType)
                .append(DELIMITER)
                .append(listName)
                .append(DELIMITER)
                .append(value)
                .toString();
    }

    public static String generatePayoutKey(ListType type, String name, String value, String party, String wallet) {
        if (StringUtils.hasText(wallet) && !StringUtils.hasText(party)) {
            throw new IllegalArgumentException("wallet_id requires party_id");
        }
        return encode(party) + encode(wallet) + encode(type.name()) + encode(name) + encode(value);
    }

    private static String encode(String value) {
        return value == null || value.isEmpty() ? "0:" : value.length() + ":" + value;
    }

    private static void addIfExist(String param, StringBuilder stringBuilder) {
        if (StringUtils.hasLength(param)) {
            stringBuilder
                    .append(param)
                    .append(DELIMITER);
        }
    }
}
