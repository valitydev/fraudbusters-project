package dev.vality.fraudbusters.management.dao.payment.reference;

import dev.vality.fraudbusters.management.dao.ReferenceDao;
import dev.vality.fraudbusters.management.domain.payment.PaymentReferenceModel;
import dev.vality.fraudbusters.management.domain.request.FilterRequest;

import java.util.List;

public interface PaymentReferenceDao extends ReferenceDao<PaymentReferenceModel> {

    List<PaymentReferenceModel> getListByTFilters(String partyId, String shopId, Integer limit);

    List<PaymentReferenceModel> filterReferences(FilterRequest filterRequest);

    List<PaymentReferenceModel> getByPartyAndShop(String partyId, String shopId);

    Integer countFilterModel(String searchValue);

    Boolean isReferenceExistForPartyAndShop(String partyId, String shopId);

}
