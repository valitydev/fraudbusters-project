package dev.vality.fraudo.utils.key;

import lombok.Builder;

import java.util.List;

@Builder
public record HistoryCacheKey<U>(String function, List<U> fields, int start, int end, String timeUnit,
                                String errorCode, List<U> groupBy) {
}
