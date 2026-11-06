package dev.vality.fraudbusters.util;

import dev.vality.fraudbusters.domain.CheckedResultModel;
import dev.vality.fraudo.constant.ResultStatus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.function.Supplier;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CheckedResultModelUtil {

    private static final Set<ResultStatus> NON_TERMINAL_STATUSES = EnumSet.of(ResultStatus.NORMAL, ResultStatus.NOTIFY);

    public static CheckedResultModel finalizeCheckedResultModel(CheckedResultModel model, List<String> notifications) {
        if (notifications != null && !notifications.isEmpty()) {
            if (model.getResultModel().getNotificationsRule() == null) {
                model.getResultModel().setNotificationsRule(notifications);
            } else {
                model.getResultModel().getNotificationsRule().addAll(notifications);
            }
        }
        return model;
    }

    public static boolean isTerminal(CheckedResultModel model) {
        return model != null
                && model.getResultModel() != null
                && model.getResultModel().getResultStatus() != null
                && !NON_TERMINAL_STATUSES.contains(model.getResultModel().getResultStatus());
    }

    public static boolean isNotification(CheckedResultModel model) {
        return model.getResultModel() != null
                && model.getResultModel().getResultStatus() == ResultStatus.NOTIFY;
    }

    /**
     * Applies the steps in order until one returns a decision. Notification-only results do not stop the search:
     * their notifications are added to the decision, or to the last notification result when no decision is made.
     */
    public static Optional<CheckedResultModel> findFirstDecision(List<Supplier<Optional<CheckedResultModel>>> steps) {
        List<String> notifications = new ArrayList<>();
        CheckedResultModel notificationResult = null;
        for (Supplier<Optional<CheckedResultModel>> step : steps) {
            Optional<CheckedResultModel> result = step.get();
            if (result.isEmpty()) {
                continue;
            }
            if (!isNotification(result.get())) {
                return Optional.of(finalizeCheckedResultModel(result.get(), notifications));
            }
            notifications.addAll(extractNotifications(result));
            notificationResult = result.get();
        }
        if (notificationResult != null) {
            notificationResult.getResultModel().setNotificationsRule(notifications);
        }
        return Optional.ofNullable(notificationResult);
    }

    public static List<String> extractNotifications(Optional<CheckedResultModel> optionalCheckedResult) {
        return optionalCheckedResult
                .filter(model -> model.getResultModel() != null
                        && model.getResultModel().getNotificationsRule() != null)
                .map(model -> model.getResultModel().getNotificationsRule())
                .orElseGet(ArrayList::new);
    }

}
