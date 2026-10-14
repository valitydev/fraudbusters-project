package dev.vality.fraudbusters.management.converter;

import dev.vality.fraudbusters.management.service.iface.SqlTimeSplitService;
import dev.vality.swag.fraudbusters.management.model.OffsetCountRatio;
import dev.vality.swag.fraudbusters.management.model.RiskScoreOffsetCountRatio;
import dev.vality.swag.fraudbusters.management.model.SplitUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static dev.vality.fraudbusters.management.constant.AnalyticsResultField.*;

@Component
@RequiredArgsConstructor
public class RowListToRiskScoreOffsetCountRatioListConverter {

    private final SqlTimeSplitService sqlTimeSplitService;

    public List<RiskScoreOffsetCountRatio> convert(List<Map<String, String>> rows, SplitUnit splitUnit) {
        if (isEmpty(rows)) {
            return Collections.emptyList();
        }
        List<OffsetCountRatio> trustedOffsetCountRatios = new ArrayList<>();
        List<OffsetCountRatio> lowOffsetCountRatios = new ArrayList<>();
        List<OffsetCountRatio> highOffsetCountRatios = new ArrayList<>();
        List<OffsetCountRatio> fatalOffsetCountRatios = new ArrayList<>();
        for (Map<String, String> row : rows) {
            OffsetCountRatio trustedOffsetCountRatio = buildOffsetCountRatio(splitUnit, row, TRUSTED_SCORE);
            trustedOffsetCountRatios.add(trustedOffsetCountRatio);
            OffsetCountRatio lowOffsetCountRatio = buildOffsetCountRatio(splitUnit, row, LOW_SCORE);
            lowOffsetCountRatios.add(lowOffsetCountRatio);
            OffsetCountRatio highOffsetCountRatio = buildOffsetCountRatio(splitUnit, row, HIGH_SCORE);
            highOffsetCountRatios.add(highOffsetCountRatio);
            OffsetCountRatio fatalOffsetCountRatio = buildOffsetCountRatio(splitUnit, row, FATAL_SCORE);
            fatalOffsetCountRatios.add(fatalOffsetCountRatio);
        }
        var trustedRiscScoreOffsetCountRatio = buildRiscScoreOffsetCountRatio(TRUSTED_SCORE, trustedOffsetCountRatios);
        var lowRiscScoreOffsetCountRatio = buildRiscScoreOffsetCountRatio(LOW_SCORE, lowOffsetCountRatios);
        var highRiscScoreOffsetCountRatio = buildRiscScoreOffsetCountRatio(HIGH_SCORE, highOffsetCountRatios);
        var fatalRiscScoreOffsetCountRatio = buildRiscScoreOffsetCountRatio(FATAL_SCORE, fatalOffsetCountRatios);
        return List.of(
                trustedRiscScoreOffsetCountRatio,
                lowRiscScoreOffsetCountRatio,
                highRiscScoreOffsetCountRatio,
                fatalRiscScoreOffsetCountRatio
        );
    }

    private boolean isEmpty(List<Map<String, String>> source) {
        return source.stream()
                .allMatch(CollectionUtils::isEmpty);
    }

    private OffsetCountRatio buildOffsetCountRatio(SplitUnit splitUnit, Map<String, String> row, String score) {
        OffsetCountRatio lowOffsetCountRatio = new OffsetCountRatio();
        lowOffsetCountRatio.setCountRatio(Objects.nonNull(row.get(score)) ? Float.parseFloat(row.get(score)) : -1);
        lowOffsetCountRatio.setOffset(sqlTimeSplitService.calculateSplitOffset(row, splitUnit));
        return lowOffsetCountRatio;
    }

    private RiskScoreOffsetCountRatio buildRiscScoreOffsetCountRatio(String lowScore,
                                                                     List<OffsetCountRatio> offsetCountRatioForLow) {
        RiskScoreOffsetCountRatio lowRiscScoreOffsetCountRatio = new RiskScoreOffsetCountRatio();
        lowRiscScoreOffsetCountRatio.setScore(lowScore);
        lowRiscScoreOffsetCountRatio.setOffsetCountRatio(offsetCountRatioForLow);
        return lowRiscScoreOffsetCountRatio;
    }
}
