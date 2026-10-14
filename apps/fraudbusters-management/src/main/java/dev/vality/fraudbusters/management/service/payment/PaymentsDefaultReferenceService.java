package dev.vality.fraudbusters.management.service.payment;

import dev.vality.fraudbusters.management.dao.payment.DefaultPaymentReferenceDaoImpl;
import dev.vality.fraudbusters.management.domain.payment.DefaultPaymentReferenceModel;
import dev.vality.fraudbusters.management.utils.DateTimeUtils;
import dev.vality.fraudbusters.management.utils.UserInfoService;
import dev.vality.swag.fraudbusters.management.model.PaymentReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentsDefaultReferenceService {

    private final DefaultPaymentReferenceDaoImpl defaultPaymentReferenceDao;
    private final UserInfoService userInfoService;

    public String insertDefaultReference(PaymentReference paymentReference) {
        var uid = UUID.randomUUID().toString();
        var defaultReferenceModel = DefaultPaymentReferenceModel.builder()
                .id(uid)
                .lastUpdateDate(paymentReference.getLastUpdateDate() != null
                        ? paymentReference.getLastUpdateDate().format(DateTimeUtils.DATE_TIME_FORMATTER)
                        : LocalDateTime.now().format(DateTimeUtils.DATE_TIME_FORMATTER))
                .modifiedByUser(userInfoService.getUserName())
                .partyId(paymentReference.getPartyId())
                .shopId(paymentReference.getShopId())
                .templateId(paymentReference.getTemplateId())
                .build();
        defaultPaymentReferenceDao.insert(defaultReferenceModel);
        return uid;
    }

}
