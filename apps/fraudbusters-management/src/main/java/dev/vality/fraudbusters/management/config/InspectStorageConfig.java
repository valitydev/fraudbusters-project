package dev.vality.fraudbusters.management.config;

import dev.vality.damsel.fraudbusters.OperationType;
import dev.vality.fraudbusters.management.converter.CommandToGroupModelConverter;
import dev.vality.fraudbusters.management.converter.CommandToTemplateModelConverter;
import dev.vality.fraudbusters.management.converter.payment.CommandToPaymentGroupReferenceModelConverter;
import dev.vality.fraudbusters.management.converter.payment.CommandToPaymentReferenceModelConverter;
import dev.vality.fraudbusters.management.converter.payment.EventToListRecordConverter;
import dev.vality.fraudbusters.management.converter.payout.CommandToPayoutGroupReferenceModelConverter;
import dev.vality.fraudbusters.management.converter.payout.CommandToPayoutReferenceModelConverter;
import dev.vality.fraudbusters.management.converter.payout.EventToPayoutListRecordConverter;
import dev.vality.fraudbusters.management.dao.GroupDao;
import dev.vality.fraudbusters.management.dao.ReferenceDao;
import dev.vality.fraudbusters.management.dao.TemplateDao;
import dev.vality.fraudbusters.management.dao.payment.group.PaymentGroupReferenceDao;
import dev.vality.fraudbusters.management.dao.payment.wblist.WbListDao;
import dev.vality.fraudbusters.management.dao.payout.group.PayoutGroupDao;
import dev.vality.fraudbusters.management.dao.payout.group.PayoutGroupReferenceDao;
import dev.vality.fraudbusters.management.dao.payout.wblist.PayoutListDao;
import dev.vality.fraudbusters.management.dao.payout.reference.PayoutReferenceDao;
import dev.vality.fraudbusters.management.dao.payout.template.PayoutTemplateDao;
import dev.vality.fraudbusters.management.domain.payment.PaymentReferenceModel;
import dev.vality.fraudbusters.management.listener.InspectStorage;
import dev.vality.fraudbusters.management.listener.ModelProjection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InspectStorageConfig {

    @Bean
    public InspectStorage paymentInspectStorage(
            TemplateDao paymentTemplateDao,
            GroupDao paymentGroupDao,
            ReferenceDao<PaymentReferenceModel> referenceDao,
            PaymentGroupReferenceDao groupReferenceDao,
            WbListDao wbListDao,
            CommandToTemplateModelConverter templateConverter,
            CommandToGroupModelConverter groupConverter,
            CommandToPaymentReferenceModelConverter referenceConverter,
            CommandToPaymentGroupReferenceModelConverter groupReferenceConverter,
            EventToListRecordConverter listRecordConverter) {
        return new InspectStorage(OperationType.payment,
                ModelProjection.of(templateConverter, paymentTemplateDao),
                ModelProjection.of(groupConverter, paymentGroupDao),
                ModelProjection.of(referenceConverter, referenceDao),
                ModelProjection.of(groupReferenceConverter, groupReferenceDao),
                ModelProjection.ofList(listRecordConverter, wbListDao));
    }

    @Bean
    public InspectStorage payoutInspectStorage(
            PayoutTemplateDao payoutTemplateDao,
            PayoutGroupDao payoutGroupDao,
            PayoutReferenceDao payoutReferenceDao,
            PayoutGroupReferenceDao payoutGroupReferenceDao,
            PayoutListDao payoutListDao,
            CommandToTemplateModelConverter templateConverter,
            CommandToGroupModelConverter groupConverter,
            CommandToPayoutReferenceModelConverter referenceConverter,
            CommandToPayoutGroupReferenceModelConverter groupReferenceConverter,
            EventToPayoutListRecordConverter listRecordConverter) {
        return new InspectStorage(OperationType.payout,
                ModelProjection.of(templateConverter, payoutTemplateDao),
                ModelProjection.of(groupConverter, payoutGroupDao),
                ModelProjection.of(referenceConverter, payoutReferenceDao),
                ModelProjection.of(groupReferenceConverter, payoutGroupReferenceDao),
                ModelProjection.ofList(listRecordConverter, payoutListDao));
    }

}
