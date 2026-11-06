package dev.vality.fraudbusters.fraud.payout.finder;

import dev.vality.damsel.wb_list.IdInfo;
import dev.vality.damsel.wb_list.ListType;
import dev.vality.damsel.wb_list.PayoutId;
import dev.vality.damsel.wb_list.Row;
import dev.vality.damsel.wb_list.WbListServiceSrv;
import dev.vality.fraudbusters.exception.RuleFunctionException;
import dev.vality.fraudbusters.fraud.constant.PayoutCheckedField;
import dev.vality.fraudbusters.fraud.model.FieldModel;
import dev.vality.fraudbusters.fraud.model.PayoutModel;
import dev.vality.fraudbusters.fraud.resolver.HistoryFieldResolver;
import dev.vality.fraudbusters.repository.clickhouse.impl.PayoutHistoryRepository;
import dev.vality.fraudbusters.util.TimestampUtil;
import dev.vality.fraudo.finder.InListFinder;
import dev.vality.fraudo.model.Pair;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class PayoutInListFinderImpl implements InListFinder<PayoutModel, PayoutCheckedField> {

    private final WbListServiceSrv.Iface lists;
    private final PayoutHistoryRepository history;
    private final HistoryFieldResolver<PayoutModel, PayoutCheckedField> historyFieldResolver;

    @Override
    public Boolean findInBlackList(List<Pair<PayoutCheckedField, String>> fields, PayoutModel model) {
        return isInList(fields, model, ListType.black);
    }

    @Override
    public Boolean findInWhiteList(List<Pair<PayoutCheckedField, String>> fields, PayoutModel model) {
        return isInList(fields, model, ListType.white);
    }

    @Override
    public Boolean findInList(String name, List<Pair<PayoutCheckedField, String>> fields, PayoutModel model) {
        return isInList(fields, model, ListType.naming, name);
    }

    @Override
    public Boolean findInGreyList(List<Pair<PayoutCheckedField, String>> fields, PayoutModel model) {
        try {
            for (var field : fields) {
                if (field.getFirst() == null || !StringUtils.hasText(field.getSecond())) {
                    continue;
                }
                var result = lists.getRowInfo(createRow(ListType.grey, field, model));
                if (!result.isSetRowInfo() || !result.getRowInfo().isSetCountInfo()) {
                    continue;
                }
                var info = result.getRowInfo().getCountInfo();
                Instant from = info.isSetStartCountTime()
                        ? TimestampUtil.parseInstantFromString(info.getStartCountTime()) : Instant.EPOCH;
                Instant to = TimestampUtil.parseInstantFromString(info.getTimeToLive());
                Instant now = TimestampUtil.instantFromModel(model);
                if (from.isAfter(now) || !to.isAfter(now) || !from.isBefore(to)) {
                    continue;
                }
                List<FieldModel> scope = new ArrayList<>();
                if (StringUtils.hasText(model.getPartyId())) {
                    scope.add(historyFieldResolver.resolve(PayoutCheckedField.PARTY_ID, model));
                }
                if (StringUtils.hasText(model.getWalletId())) {
                    scope.add(historyFieldResolver.resolve(PayoutCheckedField.WALLET_ID, model));
                }
                Integer count = history.countOperationByFieldWithGroupBy(
                        historyFieldResolver.resolve(field.getFirst()),
                        field.getSecond(),
                        from.toEpochMilli(),
                        now.toEpochMilli(),
                        scope
                );
                if (count + 1 <= info.getCount()) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            throw new RuleFunctionException(e);
        }
    }

    private boolean isInList(List<Pair<PayoutCheckedField, String>> fields, PayoutModel model, ListType type) {
        return isInList(fields, model, type, null);
    }

    private boolean isInList(List<Pair<PayoutCheckedField, String>> fields, PayoutModel model,
                             ListType type, String name) {
        try {
            List<Row> rows = fields.stream()
                    .filter(field -> field.getFirst() != null && StringUtils.hasText(field.getSecond()))
                    .map(field -> {
                        Row row = createRow(type, field, model);
                        return name == null ? row : row.setListName(name);
                    })
                    .toList();
            return !rows.isEmpty() && lists.isAnyExist(rows);
        } catch (Exception e) {
            throw new RuleFunctionException(e);
        }
    }

    private Row createRow(ListType type, Pair<PayoutCheckedField, String> field, PayoutModel model) {
        return new Row()
                .setId(IdInfo.payout_id(new PayoutId()
                        .setPartyId(model.getPartyId())
                        .setWalletId(model.getWalletId())))
                .setListType(type)
                .setListName(field.getFirst().name())
                .setValue(field.getSecond());
    }
}
