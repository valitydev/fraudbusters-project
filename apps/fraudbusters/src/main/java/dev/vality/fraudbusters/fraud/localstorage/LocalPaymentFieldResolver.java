package dev.vality.fraudbusters.fraud.localstorage;

import dev.vality.fraudbusters.fraud.constant.PaymentCheckedField;
import dev.vality.fraudbusters.fraud.model.FieldModel;
import dev.vality.fraudbusters.fraud.model.PaymentModel;
import dev.vality.fraudbusters.fraud.payment.resolver.DatabasePaymentFieldResolver;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static dev.vality.fraudbusters.fraud.filter.PaymentFieldValueResolver.GLOBAL_USER_ID;

@RequiredArgsConstructor
public class LocalPaymentFieldResolver {

    private final DatabasePaymentFieldResolver databasePaymentFieldResolver;

    public FieldModel resolve(PaymentCheckedField field, PaymentModel model) {
        FieldModel fieldModel = databasePaymentFieldResolver.resolve(field, model);
        return new FieldModel(resolveName(field), fieldModel.getValue());
    }

    public FieldModel resolve(PaymentCheckedField field, PaymentModel model, List<PaymentCheckedField> groups) {
        FieldModel resolved = databasePaymentFieldResolver.resolve(field, model, groups);
        return new FieldModel(resolveName(field, groups, null), resolved.getValue());
    }

    public String resolveName(PaymentCheckedField field) {
        return field.name();
    }

    public String resolveName(PaymentCheckedField field, List<PaymentCheckedField> groups,
                              PaymentCheckedField primaryField) {
        return databasePaymentFieldResolver.usesGlobalUserIdentity(field, groups, primaryField)
                ? GLOBAL_USER_ID : resolveName(field);
    }

    @NotNull
    public List<FieldModel> resolveListFields(PaymentModel model, List<PaymentCheckedField> fields) {
        return resolveListFields(model, fields, null);
    }

    @NotNull
    public List<FieldModel> resolveListFields(PaymentModel model, List<PaymentCheckedField> fields,
                                              PaymentCheckedField primaryField) {
        if (fields != null) {
            return fields.stream()
                    .map(field -> primaryField == PaymentCheckedField.PARTY_ID
                            ? resolve(field, model) : resolve(field, model, fields))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
