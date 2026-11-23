package dev.vality.fraudbusters.service.dto;

import dev.vality.damsel.fraudbusters.Payout;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class HistoricalPayoutsDto {

    private List<Payout> payouts;
    private String lastId;

}
