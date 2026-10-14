package dev.vality.fraudbusters.management.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TimeSplitInfo {

    private String timeUnit;
    private String statement;
}
