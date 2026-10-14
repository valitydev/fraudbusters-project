package dev.vality.fraudbusters.management.converter.payment;

import dev.vality.damsel.fraudbusters.*;
import dev.vality.fraudbusters.management.domain.payment.CheckedDataSetModel;
import dev.vality.fraudbusters.management.utils.DataSourceBeanUtils;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestHistoricalDataSetCheckResultToTestCheckedDataSetModelConverter {

    HistoricalDataSetCheckResultToTestCheckedDataSetModelConverter converter =
            new HistoricalDataSetCheckResultToTestCheckedDataSetModelConverter(
                    new PaymentToTestPaymentModelConverter());

    @Test
    void convert() {
        CheckedDataSetModel model = converter.convert(new HistoricalDataSetCheckResult()
                .setHistoricalTransactionCheck(Set.of(new HistoricalTransactionCheck()
                        .setTransaction(DataSourceBeanUtils.createDamselPayment())
                        .setCheckResult(new CheckResult().setConcreteCheckResult(new ConcreteCheckResult()
                                .setResultStatus(ResultStatus.accept(new Accept())))))));

        assertEquals("accept", model.getCheckedPaymentModels().get(0).getResultStatus());
    }

}
