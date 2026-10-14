package dev.vality.fraudbusters.management.service.payment;

import dev.vality.damsel.fraudbusters.FraudPayment;
import dev.vality.damsel.fraudbusters.PaymentServiceSrv;
import dev.vality.fraudbusters.management.utils.parser.CsvFraudPaymentParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentLoadDataService {

    private final PaymentServiceSrv.Iface paymentServiceSrv;
    private final CsvFraudPaymentParser csvFraudPaymentParser;

    public void loadFraudPayments(MultipartFile file, String userName) {
        if (csvFraudPaymentParser.hasCsvFormat(file)) {
            try {
                List<FraudPayment> fraudPayments = csvFraudPaymentParser.parse(file.getInputStream());
                log.debug("PaymentLoadDataResource loadFraudOperation initiator: {} fraudPaymentRecords: {}",
                        userName, fraudPayments);
                paymentServiceSrv.insertFraudPayments(fraudPayments);
                log.debug("PaymentLoadDataResource loaded fraudPayments: {}", fraudPayments);
            } catch (IOException | TException e) {
                log.error("PaymentLoadDataResource error when loadFraudOperation e: ", e);
                throw new RuntimeException(e);
            }
        }
    }

}
