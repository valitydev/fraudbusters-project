package dev.vality.fraudbusters.management.dao.group;

import dev.vality.fraudbusters.management.config.PostgresqlJooqITest;
import dev.vality.fraudbusters.management.dao.payment.group.GroupReferenceDaoImpl;
import dev.vality.fraudbusters.management.dao.payment.group.PaymentGroupReferenceDao;
import dev.vality.fraudbusters.management.domain.payment.PaymentGroupReferenceModel;
import dev.vality.fraudbusters.management.domain.request.FilterRequest;
import org.jooq.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@PostgresqlJooqITest
@ContextConfiguration(classes = {GroupReferenceDaoImpl.class})
public class GroupReferenceDaoImplTest {

    public static final String PARTY_ID = "partyId";
    public static final String SHOP_ID = "shopId";
    public static final String GROUP_ID = "groupId";
    @Autowired
    PaymentGroupReferenceDao groupReferenceDao;

    @Test
    void insert() {
        PaymentGroupReferenceModel referenceModel = new PaymentGroupReferenceModel();
        referenceModel.setPartyId(PARTY_ID);
        referenceModel.setShopId(SHOP_ID);
        referenceModel.setGroupId(GROUP_ID);
        groupReferenceDao.insert(referenceModel);

        List<PaymentGroupReferenceModel> byId = groupReferenceDao.getByGroupId(GROUP_ID);
        assertEquals(PARTY_ID, byId.get(0).getPartyId());

        groupReferenceDao.remove(referenceModel);

        byId = groupReferenceDao.getByGroupId(GROUP_ID);
        assertTrue(byId.isEmpty());

        groupReferenceDao.insert(referenceModel);

        List<PaymentGroupReferenceModel> paymentGroupReferenceModels =
                groupReferenceDao.filterReference(FilterRequest.builder()
                        .searchValue(GROUP_ID)
                        .size(1)
                        .build()
                );
        assertEquals(PARTY_ID, paymentGroupReferenceModels.get(0).getPartyId());

        //check size
        referenceModel.setShopId(SHOP_ID + "2");
        groupReferenceDao.insert(referenceModel);
        paymentGroupReferenceModels = groupReferenceDao.filterReference(FilterRequest.builder()
                .searchValue(GROUP_ID)
                .size(1)
                .build()
        );
        assertEquals(1, paymentGroupReferenceModels.size());

        paymentGroupReferenceModels = groupReferenceDao.filterReference(FilterRequest.builder()
                .searchValue(GROUP_ID)
                .size(2)
                .build()
        );
        assertEquals(2, paymentGroupReferenceModels.size());
        Integer integer = groupReferenceDao.countFilterReference(GROUP_ID);
        assertEquals(Integer.valueOf(2), integer);

        //check pagination
        referenceModel.setShopId(SHOP_ID + "3");
        groupReferenceDao.insert(referenceModel);
        paymentGroupReferenceModels = groupReferenceDao.filterReference(FilterRequest.builder()
                .searchValue(GROUP_ID)
                .size(1)
                .build()
        );
        System.out.println(paymentGroupReferenceModels);

        List<PaymentGroupReferenceModel> secondPage = groupReferenceDao.filterReference(FilterRequest.builder()
                .searchValue(GROUP_ID)
                .lastId(paymentGroupReferenceModels.get(0).getId())
                .sortFieldValue(GROUP_ID)
                .size(1)
                .build()
        );
        assertNotEquals(paymentGroupReferenceModels.get(0).getShopId(), secondPage.get(0).getShopId());
        integer = groupReferenceDao.countFilterReference(GROUP_ID);
        assertEquals(Integer.valueOf(3), integer);

        secondPage = groupReferenceDao.filterReference(FilterRequest.builder()
                .searchValue(GROUP_ID)
                .lastId(secondPage.get(0).getId())
                .sortFieldValue(GROUP_ID)
                .size(1)
                .build());
        assertEquals(1, secondPage.size());
        integer = groupReferenceDao.countFilterReference(GROUP_ID);
        assertEquals(Integer.valueOf(3), integer);

        //sorting check
        referenceModel.setGroupId(GROUP_ID + "2");
        groupReferenceDao.insert(referenceModel);
        secondPage = groupReferenceDao.filterReference(FilterRequest.builder()
                .size(3)
                .sortOrder(SortOrder.ASC)
                .build());
        paymentGroupReferenceModels = groupReferenceDao.filterReference(FilterRequest.builder()
                .size(3)
                .sortOrder(SortOrder.DESC)
                .build());
        assertNotEquals(paymentGroupReferenceModels.get(0).getShopId(), secondPage.get(0).getShopId());

        assertNotEquals(paymentGroupReferenceModels.get(0).getGroupId(), secondPage.get(0).getGroupId());

        paymentGroupReferenceModels = groupReferenceDao.filterReference(FilterRequest.builder()
                .searchValue(GROUP_ID)
                .lastId(paymentGroupReferenceModels.get(0).getId())
                .sortFieldValue(paymentGroupReferenceModels.get(0).getGroupId())
                .size(1)
                .sortOrder(SortOrder.DESC)
                .build());
        assertEquals(paymentGroupReferenceModels.get(0).getShopId(), SHOP_ID + 2);

        paymentGroupReferenceModels = groupReferenceDao.filterReference(FilterRequest.builder()
                .lastId(paymentGroupReferenceModels.get(0).getId())
                .sortFieldValue(paymentGroupReferenceModels.get(0).getGroupId())
                .size(1)
                .sortOrder(SortOrder.DESC)
                .build());
        assertEquals(paymentGroupReferenceModels.get(0).getShopId(), SHOP_ID);

        System.out.println(paymentGroupReferenceModels);
    }
}
