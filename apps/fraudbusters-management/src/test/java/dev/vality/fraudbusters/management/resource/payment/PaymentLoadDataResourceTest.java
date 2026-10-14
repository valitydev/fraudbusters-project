package dev.vality.fraudbusters.management.resource.payment;

import dev.vality.damsel.fraudbusters.PaymentServiceSrv;
import dev.vality.fraudbusters.management.config.converter.JwtAuthConverter;
import dev.vality.fraudbusters.management.service.payment.PaymentLoadDataService;
import dev.vality.fraudbusters.management.utils.UserInfoService;
import dev.vality.fraudbusters.management.utils.parser.CsvFraudPaymentParser;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PaymentLoadDataResource.class, CsvFraudPaymentParser.class,
        UserInfoService.class, PaymentLoadDataService.class})
public class PaymentLoadDataResourceTest {

    @Autowired
    PaymentLoadDataResource paymentLoadDataResource;
    @MockitoBean
    PaymentServiceSrv.Iface paymentServiceSrv;
    @MockitoBean
    JwtAuthConverter jwtAuthConverter;

    @Test
    void loadFraudOperation() throws IOException, TException {
        File file = new File("src/test/resources/csv/test.csv");
        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile =
                new MockMultipartFile("file", file.getName(), "text/csv", IOUtils.toByteArray(input));

        paymentLoadDataResource.loadFraudPayments(multipartFile);

        Mockito.verify(paymentServiceSrv, Mockito.times(1)).insertFraudPayments(any());
    }
}
