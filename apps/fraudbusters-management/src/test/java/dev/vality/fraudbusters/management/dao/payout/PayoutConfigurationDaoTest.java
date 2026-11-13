package dev.vality.fraudbusters.management.dao.payout;

import dev.vality.fraudbusters.management.config.PostgresqlJooqITest;
import dev.vality.fraudbusters.management.dao.payout.group.PayoutGroupDao;
import dev.vality.fraudbusters.management.dao.payout.group.PayoutGroupReferenceDao;
import dev.vality.fraudbusters.management.dao.payout.reference.PayoutReferenceDao;
import dev.vality.fraudbusters.management.dao.payout.template.PayoutTemplateDao;
import dev.vality.fraudbusters.management.dao.payout.wblist.PayoutListDao;
import dev.vality.fraudbusters.management.domain.GroupModel;
import dev.vality.fraudbusters.management.domain.PriorityIdModel;
import dev.vality.fraudbusters.management.domain.TemplateModel;
import dev.vality.fraudbusters.management.domain.enums.ListType;
import dev.vality.fraudbusters.management.domain.payout.PayoutGroupReferenceModel;
import dev.vality.fraudbusters.management.domain.payout.PayoutReferenceModel;
import dev.vality.fraudbusters.management.domain.request.FilterRequest;
import dev.vality.fraudbusters.management.domain.tables.pojos.PayoutListRecord;
import org.jooq.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@PostgresqlJooqITest
@ContextConfiguration(classes = {PayoutTemplateDao.class, PayoutReferenceDao.class, PayoutGroupDao.class,
        PayoutGroupReferenceDao.class, PayoutListDao.class})
class PayoutConfigurationDaoTest {

    @Autowired
    private PayoutTemplateDao templateDao;
    @Autowired
    private PayoutReferenceDao referenceDao;
    @Autowired
    private PayoutGroupDao groupDao;
    @Autowired
    private PayoutGroupReferenceDao groupReferenceDao;
    @Autowired
    private PayoutListDao listDao;

    @Test
    void templatesAreSearchedPagedAndNamed() {
        List.of("payout-a", "payout-b", "payout-c", "other").forEach(id -> templateDao.insert(TemplateModel.builder()
                .id(id)
                .template("rule:1=1->accept;")
                .build()));

        var firstPage = templateDao.filterModel(FilterRequest.builder()
                .searchValue("payout-%")
                .sortOrder(SortOrder.ASC)
                .size(2)
                .build());
        assertEquals(List.of("payout-a", "payout-b"), firstPage.stream()
                .map(TemplateModel::getId)
                .toList());
        var secondPage = templateDao.filterModel(FilterRequest.builder()
                .searchValue("payout-%")
                .sortOrder(SortOrder.ASC)
                .lastId("payout-b")
                .size(2)
                .build());
        assertEquals(List.of("payout-c"), secondPage.stream()
                .map(TemplateModel::getId)
                .toList());
        assertEquals(3, templateDao.countFilterModel("payout-%"));
        assertEquals(List.of("payout-a", "payout-b", "payout-c"), templateDao.getListNames("payout-%"));
        assertEquals("rule:1=1->accept;", templateDao.getById("other").getTemplate());
        assertNull(templateDao.getById("missing"));
    }

    @Test
    void referencesAreFoundByScopeAndSearch() {
        referenceDao.insert(reference("global-template", "", "", true));
        referenceDao.insert(reference("party-template", "party-1", "", false));
        referenceDao.insert(reference("wallet-template", "party-1", "wallet-1", false));
        referenceDao.insert(reference("other-template", "party-2", "", false));

        assertEquals("wallet-template", referenceDao.getByScope("party-1", "wallet-1", false).getTemplateId());
        assertEquals("global-template", referenceDao.getByScope("", "", true).getTemplateId());
        assertNull(referenceDao.getByScope("party-1", "wallet-2", false));

        var partyOne = referenceDao.filter("%party-1%", null, null, null, null);
        assertEquals(Set.of("party-template", "wallet-template"), partyOne.stream()
                .map(PayoutReferenceModel::getTemplateId)
                .collect(Collectors.toSet()));
        assertEquals(2, referenceDao.countFilter("%party-1%", null));
        assertEquals(List.of("global-template"), referenceDao.filter(null, true, null, null, null).stream()
                .map(PayoutReferenceModel::getTemplateId)
                .toList());
        assertEquals(1, referenceDao.filter(null, false, null, null, 1).size());
        assertEquals(3, referenceDao.countFilter(null, false));
    }

    @Test
    void groupsKeepTemplatePriorityAndAreFoundByTemplate() {
        groupDao.replaceGroupTemplates(GroupModel.builder()
                .groupId("group-1")
                .priorityTemplates(List.of(priority("template-b", 2L), priority("template-a", 1L)))
                .build());
        groupDao.replaceGroupTemplates(GroupModel.builder()
                .groupId("group-2")
                .priorityTemplates(List.of(priority("template-c", 1L)))
                .build());

        var group = groupDao.getById("group-1");
        assertEquals(List.of("template-a", "template-b"), group.getPriorityTemplates().stream()
                .map(PriorityIdModel::getId)
                .toList());
        assertNull(groupDao.getById("missing"));
        assertEquals(List.of("group-2"), groupDao.filterGroup("%template-c%").stream()
                .map(GroupModel::getGroupId)
                .toList());
        var matched = groupDao.filterGroup("%group-1%");
        assertEquals(1, matched.size());
        assertEquals(2, matched.get(0).getPriorityTemplates().size());
        assertEquals(2, groupDao.filterGroup(null).size());
    }

    @Test
    void groupReferencesAreSearchedByPartyWalletAndGroup() {
        groupReferenceDao.insert(groupReference("group-global", "", ""));
        groupReferenceDao.insert(groupReference("group-party", "party-1", ""));
        groupReferenceDao.insert(groupReference("group-wallet", "party-1", "wallet-1"));

        assertEquals(3, groupReferenceDao.countFilter(null));
        assertEquals(Set.of("group-party", "group-wallet"), groupReferenceDao.filter("%party-1%", null, null, null)
                .stream()
                .map(PayoutGroupReferenceModel::getGroupId)
                .collect(Collectors.toSet()));
        assertEquals(1, groupReferenceDao.filter("%wallet-1%", null, null, null).size());
        assertEquals(1, groupReferenceDao.filter(null, null, null, 1).size());
    }

    @Test
    void listRecordsAreFilteredByTypeNameAndSearch() {
        listDao.saveListRecord(listRecord(ListType.black, "EMAIL", "a@example.com", "", "", null));
        listDao.saveListRecord(listRecord(ListType.black, "CARD_TOKEN", "token-1", "party-1", "wallet-1", null));
        listDao.saveListRecord(listRecord(ListType.grey, "CARD_TOKEN", "token-2", "party-1", "",
                "{\"count\":5,\"time_to_live\":\"2026-02-01T00:00:00Z\"}"));

        assertEquals(2, listDao.countFilter(ListType.black, null, null));
        assertEquals(List.of("token-1"), listDao.filter(ListType.black, List.of("CARD_TOKEN"), null, null, null, null)
                .stream()
                .map(PayoutListRecord::getValue)
                .toList());
        assertEquals(List.of("token-1"), listDao.filter(ListType.black, null, "%wallet-1%", null, null, null).stream()
                .map(PayoutListRecord::getValue)
                .toList());
        var grey = listDao.filter(ListType.grey, null, null, null, null, null);
        assertEquals(1, grey.size());
        assertTrue(grey.get(0).getRowInfo().contains("\"count\":5"));
        assertEquals(List.of("CARD_TOKEN", "EMAIL"), listDao.getCurrentListNames(ListType.black));
        assertEquals(List.of(), listDao.getCurrentListNames(ListType.white));
    }

    @Test
    void referencesArePagedByIdAndKeepWhoChangedThem() {
        referenceDao.insert(reference("template-1", "party-1", "", false));
        referenceDao.insert(reference("template-2", "party-2", "", false));
        referenceDao.insert(reference("template-3", "party-3", "", false));

        var firstPage = referenceDao.filter(null, false, null, SortOrder.ASC, 2);
        var secondPage = referenceDao.filter(null, false, firstPage.get(1).getId(), SortOrder.ASC, 2);
        assertEquals(2, firstPage.size());
        assertEquals(Set.of("template-1", "template-2", "template-3"), Stream.concat(
                        firstPage.stream(), secondPage.stream())
                .map(PayoutReferenceModel::getTemplateId)
                .collect(Collectors.toSet()));

        var created = referenceDao.getByScope("party-1", "", false);
        assertNotNull(created.getId());
        assertNotNull(created.getLastUpdateDate());
        var changed = reference("template-2", "party-1", "", false);
        changed.setModifiedByUser("second-user");
        referenceDao.insert(changed);
        var updated = referenceDao.getByScope("party-1", "", false);
        assertEquals(created.getId(), updated.getId());
        assertEquals("template-2", updated.getTemplateId());
        assertEquals("second-user", updated.getModifiedByUser());
    }

    @Test
    void groupReferencesArePagedById() {
        groupReferenceDao.insert(groupReference("group-1", "party-1", ""));
        groupReferenceDao.insert(groupReference("group-2", "party-2", ""));

        var firstPage = groupReferenceDao.filter(null, null, SortOrder.DESC, 1);
        var secondPage = groupReferenceDao.filter(null, firstPage.get(0).getId(), SortOrder.DESC, 1);
        assertEquals(1, secondPage.size());
        assertTrue(firstPage.get(0).getId().compareTo(secondPage.get(0).getId()) > 0);
    }

    @Test
    void listRecordsGetIdsAndKeepWhoCreatedThem() {
        var record = listRecord(ListType.black, "EMAIL", "a@example.com", "party-1", "", null);
        record.setCreatedByUser("first-user");
        listDao.saveListRecord(record);
        listDao.saveListRecord(listRecord(ListType.black, "EMAIL", "b@example.com", "party-1", "", null));

        var firstPage = listDao.filter(ListType.black, null, null, null, SortOrder.ASC, 1);
        var secondPage = listDao.filter(ListType.black, null, null, firstPage.get(0).getId(), SortOrder.ASC, 1);
        assertEquals(1, secondPage.size());
        assertTrue(firstPage.get(0).getId().compareTo(secondPage.get(0).getId()) < 0);

        var stored = listDao.filter(ListType.black, null, "a@example.com", null, null, null).get(0);
        assertEquals("first-user", stored.getCreatedByUser());
        assertNotNull(stored.getInsertTime());
        assertEquals("a@example.com", listDao.findById(stored.getId()).getValue());
        assertNull(listDao.findById("missing"));
    }

    @Test
    void templatesAndGroupsKeepWhoChangedThem() {
        templateDao.insert(TemplateModel.builder()
                .id("template-1")
                .template("rule:1=1->accept;")
                .modifiedByUser("first-user")
                .build());
        templateDao.insert(TemplateModel.builder()
                .id("template-1")
                .template("rule:1=1->decline;")
                .modifiedByUser("second-user")
                .build());
        var template = templateDao.getById("template-1");
        assertEquals("second-user", template.getModifiedByUser());
        assertNotNull(template.getLastUpdateDate());

        groupDao.replaceGroupTemplates(GroupModel.builder()
                .groupId("group-1")
                .priorityTemplates(List.of(priority("template-1", 1L)))
                .modifiedByUser("first-user")
                .build());
        assertEquals("first-user", groupDao.getById("group-1").getModifiedByUser());
    }

    private static PayoutReferenceModel reference(String templateId, String partyId, String walletId,
                                                  boolean global) {
        var model = new PayoutReferenceModel();
        model.setId(UUID.randomUUID().toString());
        model.setTemplateId(templateId);
        model.setPartyId(partyId);
        model.setWalletId(walletId);
        model.setIsGlobal(global);
        return model;
    }

    private static PayoutGroupReferenceModel groupReference(String groupId, String partyId, String walletId) {
        var model = new PayoutGroupReferenceModel();
        model.setId(UUID.randomUUID().toString());
        model.setGroupId(groupId);
        model.setPartyId(partyId);
        model.setWalletId(walletId);
        return model;
    }

    private static PriorityIdModel priority(String id, long priority) {
        return PriorityIdModel.builder()
                .id(id)
                .priority(priority)
                .build();
    }

    private static PayoutListRecord listRecord(ListType type, String name, String value, String partyId,
                                               String walletId, String rowInfo) {
        var record = new PayoutListRecord();
        record.setId(UUID.randomUUID().toString());
        record.setListType(type);
        record.setListName(name);
        record.setValue(value);
        record.setPartyId(partyId);
        record.setWalletId(walletId);
        record.setRowInfo(rowInfo);
        return record;
    }
}
