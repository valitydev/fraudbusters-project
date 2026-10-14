package dev.vality.fraudbusters.notificator.dao;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SqlFilterUtils {

    public static final String LIKE_SYMBOL = "%";

    static String prepareSearchField(String searchField) {
        if (StringUtils.hasText(searchField)) {
            return LIKE_SYMBOL + searchField + LIKE_SYMBOL;
        }
        return searchField;
    }
}
