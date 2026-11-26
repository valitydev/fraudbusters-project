package dev.vality.fraudbusters.management.service;

import dev.vality.fraudbusters.management.dao.CatalogMetadataDao;
import dev.vality.fraudbusters.management.dao.CatalogMetadataDao.ListNameCount;
import dev.vality.fraudbusters.management.dao.payment.DefaultPaymentReferenceDaoImpl;
import dev.vality.fraudbusters.management.dao.payment.group.PaymentGroupReferenceDao;
import dev.vality.fraudbusters.management.dao.payment.reference.PaymentReferenceDao;
import dev.vality.fraudbusters.management.dao.payment.template.PaymentTemplateDao;
import dev.vality.fraudbusters.management.dao.payout.group.PayoutGroupReferenceDao;
import dev.vality.fraudbusters.management.dao.payout.reference.PayoutReferenceDao;
import dev.vality.fraudbusters.management.dao.payout.template.PayoutTemplateDao;
import dev.vality.fraudbusters.management.domain.enums.ListType;
import dev.vality.swag.fraudbusters.management.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CatalogMetadataService {

    private final CatalogMetadataDao metadataDao;
    private final PaymentTemplateDao paymentTemplateDao;
    private final PaymentReferenceDao paymentReferenceDao;
    private final DefaultPaymentReferenceDaoImpl defaultPaymentReferenceDao;
    private final PaymentGroupReferenceDao paymentGroupReferenceDao;
    private final PayoutTemplateDao payoutTemplateDao;
    private final PayoutReferenceDao payoutReferenceDao;
    private final PayoutGroupReferenceDao payoutGroupReferenceDao;

    public PaymentTemplatesMetadata getPaymentTemplatesMetadata() {
        return new PaymentTemplatesMetadata(new PaymentTemplatesMetadataCounts()
                .templates(paymentTemplateDao.countFilterModel(null))
                .references(paymentReferenceDao.countFilterModel(null))
                .defaultReferences(defaultPaymentReferenceDao.countFilterModel(null)));
    }

    public PayoutTemplatesMetadata getPayoutTemplatesMetadata() {
        return new PayoutTemplatesMetadata(new PayoutTemplatesMetadataCounts()
                .templates(payoutTemplateDao.countFilterModel(null))
                .references(payoutReferenceDao.countFilter(null, null)));
    }

    public GroupsMetadata getPaymentGroupsMetadata() {
        return new GroupsMetadata(new GroupsMetadataCounts()
                .groups(metadataDao.countPaymentGroups())
                .references(paymentGroupReferenceDao.countFilterReference(null)));
    }

    public GroupsMetadata getPayoutGroupsMetadata() {
        return new GroupsMetadata(new GroupsMetadataCounts()
                .groups(metadataDao.countPayoutGroups())
                .references(payoutGroupReferenceDao.countFilter(null)));
    }

    public PaymentListsMetadata getPaymentListsMetadata() {
        var metadata = listMetadata(metadataDao.countPaymentListRows());
        var counts = new PaymentListsMetadataCounts()
                .black(metadata.count(ListType.black))
                .white(metadata.count(ListType.white))
                .grey(metadata.count(ListType.grey))
                .candidateBatches(metadataDao.countCandidateBatches());
        return new PaymentListsMetadata(counts, metadata.listNames());
    }

    public PayoutListsMetadata getPayoutListsMetadata() {
        var metadata = listMetadata(metadataDao.countPayoutListRows());
        var counts = new PayoutListsMetadataCounts()
                .black(metadata.count(ListType.black))
                .white(metadata.count(ListType.white))
                .grey(metadata.count(ListType.grey))
                .named(metadata.count(ListType.naming));
        return new PayoutListsMetadata(counts, metadata.listNames().named(metadata.names(ListType.naming)));
    }

    private ListMetadata listMetadata(List<ListNameCount> rows) {
        Map<ListType, Integer> counts = new EnumMap<>(ListType.class);
        Map<ListType, List<String>> names = new EnumMap<>(ListType.class);
        for (ListNameCount row : rows) {
            counts.merge(row.type(), row.count(), Integer::sum);
            names.computeIfAbsent(row.type(), type -> new ArrayList<>()).add(row.name());
        }
        return new ListMetadata(counts, names);
    }

    private record ListMetadata(Map<ListType, Integer> counts, Map<ListType, List<String>> namesByType) {

        int count(ListType type) {
            return counts.getOrDefault(type, 0);
        }

        List<String> names(ListType type) {
            return namesByType.getOrDefault(type, List.of());
        }

        CatalogListNames listNames() {
            return new CatalogListNames(names(ListType.black), names(ListType.white), names(ListType.grey));
        }
    }
}
