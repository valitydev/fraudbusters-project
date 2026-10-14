package dev.vality.fraudbusters.management.validator;

import dev.vality.fraudbusters.management.exception.SaveRowsException;
import dev.vality.swag.fraudbusters.management.model.PaymentCountInfo;
import dev.vality.swag.fraudbusters.management.model.PaymentListRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ListRowValidator {

    public void validate(List<PaymentCountInfo> records) {
        Optional<PaymentCountInfo> invalidData = records.stream()
                .filter(this::isEmptyRequiredFields)
                .findFirst();
        if (invalidData.isPresent()) {
            log.error("Has invalid data: {}", invalidData.get());
            throw new SaveRowsException("Has invalid data: " + invalidData);
        }
    }

    private boolean isEmptyRequiredFields(PaymentCountInfo paymentCountInfo) {
        PaymentListRecord listRecord = paymentCountInfo.getListRecord();
        return !StringUtils.hasText(listRecord.getListName()) || !StringUtils.hasText(listRecord.getValue());
    }

}
