package dev.vality.fraudbusters.fraud.payout;

import dev.vality.damsel.wb_list.IdInfo;
import dev.vality.damsel.wb_list.ListType;
import dev.vality.damsel.wb_list.PayoutId;
import dev.vality.damsel.wb_list.Row;
import dev.vality.damsel.wb_list.WbListServiceSrv;
import dev.vality.fraudbusters.domain.TimeBound;
import dev.vality.fraudbusters.exception.RuleFunctionException;
import dev.vality.fraudbusters.fraud.constant.PaymentCheckedField;
import dev.vality.fraudbusters.fraud.model.FieldModel;
import dev.vality.fraudbusters.fraud.model.PaymentModel;
import dev.vality.fraudbusters.repository.clickhouse.impl.PayoutHistoryRepository;
import dev.vality.fraudbusters.util.TimestampUtil;
import dev.vality.fraudo.finder.InListFinder;
import dev.vality.fraudo.model.Pair;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PayoutInListFinder implements InListFinder<PaymentModel, PaymentCheckedField> {

    private final WbListServiceSrv.Iface lists;
    private final PayoutHistoryRepository history;

    @Override
    public Boolean findInBlackList(List<Pair<PaymentCheckedField, String>> fields, PaymentModel model) {
        return check(fields, model, ListType.black);
    }

    @Override
    public Boolean findInWhiteList(List<Pair<PaymentCheckedField, String>> fields, PaymentModel model) {
        return check(fields, model, ListType.white);
    }

    @Override
    public Boolean findInList(String name, List<Pair<PaymentCheckedField, String>> fields, PaymentModel model) {
        return check(fields, model, ListType.naming, name);
    }

    @Override
    public Boolean findInGreyList(List<Pair<PaymentCheckedField, String>> fields, PaymentModel model) {
        try {
            for (var field : fields) {
                if (field.getFirst() == null || !StringUtils.hasText(field.getSecond())) {
                    continue;
                }
                var result = lists.getRowInfo(row(ListType.grey, field, model));
                if (!result.isSetRowInfo() || !result.getRowInfo().isSetCountInfo()) {
                    continue;
                }
                var info = result.getRowInfo().getCountInfo();
                Instant from = info.isSetStartCountTime()
                        ? TimestampUtil.parseInstantFromString(info.getStartCountTime()) : Instant.EPOCH;
                Instant to = TimestampUtil.parseInstantFromString(info.getTimeToLive());
                Instant now = TimestampUtil.instantFromPaymentModel(model);
                if (from.isAfter(now) || !to.isAfter(now) || !from.isBefore(to)) {
                    continue;
                }
                List<FieldModel> filters = new ArrayList<>();
                filters.add(new FieldModel(PayoutFields.column(field.getFirst()), field.getSecond()));
                if (StringUtils.hasText(model.getPartyId())) {
                    filters.add(PayoutFields.resolve(PaymentCheckedField.PARTY_ID, model));
                }
                if (StringUtils.hasText(model.getWalletId())) {
                    filters.add(PayoutFields.resolve(PaymentCheckedField.WALLET_ID, model));
                }
                double count = history.aggregate("count()", filters,
                        TimeBound.builder().left(from).right(now).build(), null, null);
                if (count + 1 <= info.getCount()) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            throw new RuleFunctionException(e);
        }
    }

    private boolean check(List<Pair<PaymentCheckedField, String>> fields, PaymentModel model, ListType type) {
        return check(fields, model, type, null);
    }

    private boolean check(List<Pair<PaymentCheckedField, String>> fields, PaymentModel model,
                          ListType type, String name) {
        try {
            List<Row> rows = fields.stream()
                    .filter(field -> field.getFirst() != null && StringUtils.hasText(field.getSecond()))
                    .map(field -> {
                        Row row = row(type, field, model);
                        return name == null ? row : row.setListName(name);
                    }).toList();
            return !rows.isEmpty() && lists.isAnyExist(rows);
        } catch (Exception e) {
            throw new RuleFunctionException(e);
        }
    }

    private Row row(ListType type, Pair<PaymentCheckedField, String> field, PaymentModel model) {
        PayoutFields.column(field.getFirst());
        return new Row().setId(IdInfo.payout_id(new PayoutId()
                        .setPartyId(model.getPartyId()).setWalletId(model.getWalletId())))
                .setListType(type).setListName(field.getFirst().name()).setValue(field.getSecond());
    }
}
