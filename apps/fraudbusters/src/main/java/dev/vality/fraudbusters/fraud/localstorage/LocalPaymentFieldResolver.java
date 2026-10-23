package dev.vality.fraudbusters.fraud.localstorage;

import dev.vality.fraudbusters.fraud.constant.PaymentCheckedField;
import dev.vality.fraudbusters.fraud.model.AggregationFields;
import dev.vality.fraudbusters.fraud.model.FieldModel;
import dev.vality.fraudbusters.fraud.model.PaymentModel;
import dev.vality.fraudbusters.fraud.payment.resolver.DatabasePaymentFieldResolver;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.IntStream;

import static dev.vality.fraudbusters.fraud.filter.PaymentFieldValueResolver.GLOBAL_USER_ID;

@RequiredArgsConstructor
public class LocalPaymentFieldResolver {

    private final DatabasePaymentFieldResolver databasePaymentFieldResolver;

    public AggregationFields resolveAggregation(PaymentCheckedField field, PaymentModel model,
                                                 List<PaymentCheckedField> groups) {
        return resolveAggregation(field, model, groups, null);
    }

    public AggregationFields resolveAggregation(PaymentCheckedField field, PaymentModel model,
                                                 List<PaymentCheckedField> groups, PaymentCheckedField countedField) {
        var resolved = databasePaymentFieldResolver.resolveAggregation(field, model, groups, countedField);
        var localGroups = IntStream.range(0, resolved.getGroups().size())
                .mapToObj(i -> localField(groups.get(i), resolved.getGroups().get(i))).toList();
        return new AggregationFields(localField(field, resolved.getField()), localGroups,
                countedField == null ? null : localName(countedField, resolved.getCountedField()));
    }

    private FieldModel localField(PaymentCheckedField field, FieldModel resolved) {
        return new FieldModel(localName(field, resolved.getName()), resolved.getValue());
    }

    private String localName(PaymentCheckedField field, String resolvedName) {
        return field == PaymentCheckedField.USER_ID && !resolvedName.equals(databasePaymentFieldResolver.resolve(field))
                ? GLOBAL_USER_ID : field.name();
    }
}
