package dev.vality.fraudbusters.management.resource;

import dev.vality.fraudbusters.management.dao.CatalogMetadataDao;
import dev.vality.fraudbusters.management.dao.CatalogMetadataDao.ListNameCount;
import dev.vality.fraudbusters.management.dao.payment.DefaultPaymentReferenceDaoImpl;
import dev.vality.fraudbusters.management.dao.payment.group.PaymentGroupReferenceDao;
import dev.vality.fraudbusters.management.dao.payment.reference.PaymentReferenceDao;
import dev.vality.fraudbusters.management.dao.payment.template.PaymentTemplateDao;
import dev.vality.fraudbusters.management.dao.payout.group.PayoutGroupReferenceDao;
import dev.vality.fraudbusters.management.dao.payout.reference.PayoutReferenceDao;
import dev.vality.fraudbusters.management.dao.payout.template.PayoutTemplateDao;
import dev.vality.swag.fraudbusters.management.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CatalogMetadataResource {

    private final CatalogMetadataDao metadataDao;
    private final PaymentTemplateDao paymentTemplateDao;
    private final PaymentReferenceDao paymentReferenceDao;
    private final DefaultPaymentReferenceDaoImpl defaultPaymentReferenceDao;
    private final PaymentGroupReferenceDao paymentGroupReferenceDao;
    private final PayoutTemplateDao payoutTemplateDao;
    private final PayoutReferenceDao payoutReferenceDao;
    private final PayoutGroupReferenceDao payoutGroupReferenceDao;

    @GetMapping(value = "/payments/catalog/templates/metadata", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('fraud-officer')")
    public PaymentTemplatesMetadata getPaymentTemplatesMetadata() {
        return new PaymentTemplatesMetadata(new PaymentTemplatesMetadataCounts()
                .templates(paymentTemplateDao.countFilterModel(null))
                .references(paymentReferenceDao.countFilterModel(null))
                .defaultReferences(defaultPaymentReferenceDao.countFilterModel(null)));
    }

    @GetMapping(value = "/payouts/catalog/templates/metadata", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('fraud-officer')")
    public PayoutTemplatesMetadata getPayoutTemplatesMetadata() {
        return new PayoutTemplatesMetadata(new PayoutTemplatesMetadataCounts()
                .templates(payoutTemplateDao.countFilterModel(null))
                .references(payoutReferenceDao.countFilter(null, null)));
    }

    @GetMapping(value = "/payments/catalog/groups/metadata", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('fraud-officer')")
    public GroupsMetadata getPaymentGroupsMetadata() {
        return new GroupsMetadata(new GroupsMetadataCounts()
                .groups(metadataDao.countPaymentGroups())
                .references(paymentGroupReferenceDao.countFilterReference(null)));
    }

    @GetMapping(value = "/payouts/catalog/groups/metadata", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('fraud-officer')")
    public GroupsMetadata getPayoutGroupsMetadata() {
        return new GroupsMetadata(new GroupsMetadataCounts()
                .groups(metadataDao.countPayoutGroups())
                .references(payoutGroupReferenceDao.countFilter(null)));
    }

    @GetMapping(value = "/payments/catalog/lists/metadata", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('fraud-monitoring', 'fraud-officer')")
    public PaymentListsMetadata getPaymentListsMetadata() {
        var metadata = listMetadata(metadataDao.countPaymentListRows());
        var counts = new PaymentListsMetadataCounts()
                .black(metadata.count("black"))
                .white(metadata.count("white"))
                .grey(metadata.count("grey"))
                .candidateBatches(metadataDao.countCandidateBatches());
        return new PaymentListsMetadata(counts, metadata.listNames());
    }

    @GetMapping(value = "/payouts/catalog/lists/metadata", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('fraud-monitoring', 'fraud-officer')")
    public PayoutListsMetadata getPayoutListsMetadata() {
        var metadata = listMetadata(metadataDao.countPayoutListRows());
        var counts = new PayoutListsMetadataCounts()
                .black(metadata.count("black"))
                .white(metadata.count("white"))
                .grey(metadata.count("grey"))
                .named(metadata.count("naming"));
        return new PayoutListsMetadata(counts, metadata.listNames().named(metadata.names("naming")));
    }

    private ListMetadata listMetadata(List<ListNameCount> rows) {
        Map<String, Integer> counts = new HashMap<>();
        Map<String, List<String>> names = new HashMap<>();
        for (ListNameCount row : rows) {
            counts.merge(row.type(), row.count(), Integer::sum);
            names.computeIfAbsent(row.type(), type -> new ArrayList<>()).add(row.name());
        }
        return new ListMetadata(counts, names);
    }

    private record ListMetadata(Map<String, Integer> counts, Map<String, List<String>> namesByType) {

        int count(String type) {
            return counts.getOrDefault(type, 0);
        }

        List<String> names(String type) {
            return namesByType.getOrDefault(type, List.of());
        }

        CatalogListNames listNames() {
            return new CatalogListNames(names("black"), names("white"), names("grey"));
        }
    }
}
