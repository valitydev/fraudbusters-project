package dev.vality.fraudbusters.service.dto;

import dev.vality.damsel.fraudbusters.Withdrawal;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class HistoricalWithdrawalsDto {

    private List<Withdrawal> withdrawals;
    private String lastId;

}
