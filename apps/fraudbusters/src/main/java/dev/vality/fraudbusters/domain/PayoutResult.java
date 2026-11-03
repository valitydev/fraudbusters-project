package dev.vality.fraudbusters.domain;

import dev.vality.fraudbusters.fraud.model.PayoutModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayoutResult {

    private PayoutModel payoutModel;
    private CheckedResultModel resultModel;

}
