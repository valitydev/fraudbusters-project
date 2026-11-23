package dev.vality.fraudbusters.service.dto;

import dev.vality.fraudbusters.domain.PayoutResult;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class HistoricalPayoutChecksDto {

    private List<PayoutResult> payoutChecks;
    private String lastId;

}
