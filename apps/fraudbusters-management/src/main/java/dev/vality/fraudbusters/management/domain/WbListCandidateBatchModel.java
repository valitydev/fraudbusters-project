package dev.vality.fraudbusters.management.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WbListCandidateBatchModel {

    private String id;
    private String source;
    private LocalDateTime insertTime;
    private Integer size;
    private String fields;
}
