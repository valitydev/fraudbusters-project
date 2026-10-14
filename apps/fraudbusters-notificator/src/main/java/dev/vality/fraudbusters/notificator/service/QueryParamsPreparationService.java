package dev.vality.fraudbusters.notificator.service;

import dev.vality.fraudbusters.notificator.constant.CustomParameters;
import dev.vality.fraudbusters.notificator.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

import static java.time.format.DateTimeFormatter.ISO_DATE;


@Service
@RequiredArgsConstructor
public class QueryParamsPreparationService {

    public Map<String, String> prepare() {
        LocalDateTime now = LocalDateTime.now();
        String currentDate = now.format(ISO_DATE);
        String currentDateTime = DateUtils.timeToString(now);
        String currentMonth = String.valueOf(now.getMonth().getValue());
        String currentYear = String.valueOf(now.getYear());
        return Map.of(CustomParameters.CURRENT_DATE_TIME, currentDateTime,
                CustomParameters.CURRENT_DATE, currentDate,
                CustomParameters.CURRENT_MONTH, currentMonth,
                CustomParameters.CURRENT_YEAR, currentYear);

    }

}
