package dev.vality.fraudbusters.service.dto;

import dev.vality.fraudbusters.domain.PayoutResult;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class HistoricalWithdrawalChecksDto {

    private List<PayoutResult> withdrawalChecks;
    private String lastId;

}
