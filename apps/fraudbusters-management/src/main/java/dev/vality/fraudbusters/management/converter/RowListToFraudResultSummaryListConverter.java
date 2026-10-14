package dev.vality.fraudbusters.management.converter;

import dev.vality.swag.fraudbusters.management.model.FraudResultSummary;
import dev.vality.swag.fraudbusters.management.model.Summary;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static dev.vality.fraudbusters.management.constant.AnalyticsResultField.*;

@Component
public class RowListToFraudResultSummaryListConverter {

    public List<FraudResultSummary> convert(List<Map<String, String>> rows) {
        if (isEmpty(rows)) {
            return Collections.emptyList();
        }
        return rows.stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    private FraudResultSummary toSummary(Map<String, String> fieldRow) {
        return new FraudResultSummary()
                .template(fieldRow.get(TEMPLATE))
                .checkedRule(fieldRow.get(RULE))
                .status(fieldRow.get(STATUS))
                .summary(new Summary()
                        .count(Objects.nonNull(fieldRow.get(COUNT)) ? Integer.parseInt(fieldRow.get(COUNT)) : -1)
                        .sum(Objects.nonNull(fieldRow.get(SUM)) ? Float.parseFloat(fieldRow.get(SUM)) : -1)
                        .ratio(Objects.nonNull(fieldRow.get(RATIO)) ? Float.parseFloat(fieldRow.get(RATIO)) : -1)
                );

    }

    private boolean isEmpty(List<Map<String, String>> rows) {
        return rows.stream()
                .allMatch(CollectionUtils::isEmpty);
    }
}
