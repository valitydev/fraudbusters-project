package dev.vality.fraudo.utils;

import dev.vality.fraudo.constant.ResultStatus;
import dev.vality.fraudo.model.ResultModel;
import dev.vality.fraudo.model.RuleResult;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ResultUtils {

    public static List<String> getNotifications(ResultModel result) {
        return result.getRuleResults().stream()
                .filter(ruleResult -> ResultStatus.NOTIFY.equals(ruleResult.getResultStatus())
                        || ResultStatus.DECLINE_AND_NOTIFY.equals(ruleResult.getResultStatus())
                        || ResultStatus.ACCEPT_AND_NOTIFY.equals(ruleResult.getResultStatus()))
                .map(RuleResult::getRuleChecked)
                .collect(Collectors.toList());
    }

    public static Optional<RuleResult> findFirstNotNotifyStatus(ResultModel result) {
        return result.getRuleResults().stream()
                .filter(ruleResult -> !ruleResult.getResultStatus().equals(ResultStatus.NOTIFY))
                .findFirst();
    }

}
