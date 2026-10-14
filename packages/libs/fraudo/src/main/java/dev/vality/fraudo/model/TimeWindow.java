package dev.vality.fraudo.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TimeWindow {

    private int start;
    private int end;
    private String timeUnit;

}
