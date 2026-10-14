package dev.vality.fraudbusters.notificator.parser;

import org.springframework.stereotype.Service;

@Service
public class PeriodParser {

    public Long parse(String period) {
        return switch (period) {
            case "1s" -> 1000L;
            case "1m" -> 60 * 1000L;
            case "1h" -> 60 * 60 * 1000L;
            case "1d" -> 24 * 60 * 60 * 1000L;
            default -> 0L;
        };
    }

}
