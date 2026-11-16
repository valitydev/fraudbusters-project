package dev.vality.fraudbusters.management.service.payout;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.CommandType;
import dev.vality.fraudbusters.management.converter.payout.PayoutReferenceModelToPayoutReferenceConverter;
import dev.vality.fraudbusters.management.converter.payout.PayoutReferenceToCommandConverter;
import dev.vality.fraudbusters.management.dao.payout.reference.PayoutReferenceDao;
import dev.vality.fraudbusters.management.domain.payout.PayoutReferenceModel;
import dev.vality.fraudbusters.management.exception.NotFoundException;
import dev.vality.fraudbusters.management.utils.FilterRequestUtils;
import dev.vality.fraudbusters.management.utils.PagingDataUtils;
import dev.vality.fraudbusters.management.utils.PayoutScope;
import dev.vality.swag.fraudbusters.management.model.PayoutReference;
import dev.vality.swag.fraudbusters.management.model.PayoutReferencesResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PayoutReferencesService {

    private final PayoutReferenceDao payoutReferenceDao;
    private final PayoutCommandService payoutCommandService;
    private final PayoutReferenceToCommandConverter payoutReferenceToCommandConverter;
    private final PayoutReferenceModelToPayoutReferenceConverter payoutReferenceModelToPayoutReferenceConverter;

    public PayoutReferencesResponse filterReferences(String searchValue, Boolean isGlobal, String lastId,
                                                     String sortOrder, Integer size) {
        String pattern = FilterRequestUtils.prepareSearchValue(searchValue);
        return new PayoutReferencesResponse()
                .count(payoutReferenceDao.countFilter(pattern, isGlobal))
                .result(payoutReferenceDao.filter(pattern, isGlobal, lastId, PagingDataUtils.getSortOrder(sortOrder),
                                size)
                        .stream()
                        .map(payoutReferenceModelToPayoutReferenceConverter::convert)
                        .toList());
    }

    public List<String> insertReferences(List<PayoutReference> references, String initiator) {
        return references.stream()
                .map(payoutReferenceToCommandConverter::convert)
                .map(command -> payoutCommandService.sendReference(command, CommandType.CREATE, initiator))
                .toList();
    }

    public String removeReference(String partyId, String walletId, Boolean isGlobal, String initiator) {
        boolean global = Boolean.TRUE.equals(isGlobal);
        PayoutReferenceModel stored = payoutReferenceDao.getByScope(
                global ? "" : PayoutScope.replaceNullWithEmpty(partyId),
                global ? "" : PayoutScope.replaceNullWithEmpty(walletId),
                global);
        if (stored == null) {
            throw new NotFoundException("Payout reference not found");
        }
        return payoutCommandService.sendReference(
                payoutReferenceToCommandConverter.convert(
                        payoutReferenceModelToPayoutReferenceConverter.convert(stored)),
                CommandType.DELETE, initiator);
    }
}
