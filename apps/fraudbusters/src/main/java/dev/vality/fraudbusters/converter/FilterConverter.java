package dev.vality.fraudbusters.converter;

import dev.vality.damsel.fraudbusters.Filter;
import dev.vality.damsel.fraudbusters.Page;
import dev.vality.damsel.fraudbusters.Sort;
import dev.vality.fraudbusters.constant.FilterField;
import dev.vality.fraudbusters.constant.FraudResultField;
import dev.vality.fraudbusters.constant.PaymentField;
import dev.vality.fraudbusters.constant.PayoutCheckField;
import dev.vality.fraudbusters.constant.PayoutField;
import dev.vality.fraudbusters.constant.SortOrder;
import dev.vality.fraudbusters.service.dto.FieldType;
import dev.vality.fraudbusters.service.dto.FilterDto;
import dev.vality.fraudbusters.service.dto.SearchFieldDto;
import dev.vality.fraudbusters.service.dto.SortDto;
import dev.vality.fraudo.constant.ResultStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class FilterConverter {

    /** Columns that fraud.payout stores; payout history has no shop, invoice or antifraud rule. */
    private static final Set<FilterField> PAYOUT_FIELDS = Set.of(
            PaymentField.ID,
            PaymentField.PARTY_ID,
            PayoutField.WALLET_ID,
            PaymentField.STATUS,
            PaymentField.EMAIL,
            PaymentField.BANK_COUNTRY,
            PaymentField.CARD_TOKEN,
            PaymentField.FINGERPRINT,
            PaymentField.TERMINAL,
            PaymentField.LAST_DIGITS,
            PaymentField.CUSTOMER_ID,
            PaymentField.USER_ID
    );

    public FilterDto convert(Filter filter, Page page, Sort sort) {
        return buildFilter(filter, page, sort, false);
    }

    public FilterDto convertFraudResults(Filter filter, Page page, Sort sort) {
        return buildFilter(filter, page, sort, true);
    }

    public FilterDto convertPayouts(Filter filter, Page page, Sort sort) {
        FilterDto filterDto = buildFilter(filter, page, sort, false);
        Set<SearchFieldDto> searchFields = filterDto.getSearchFields().stream()
                .filter(searchField -> PAYOUT_FIELDS.contains(searchField.getField()))
                .collect(Collectors.toCollection(HashSet::new));
        if (filter.isSetWalletId() && StringUtils.hasLength(filter.getWalletId())) {
            addSearchField(searchFields, PayoutField.WALLET_ID, FieldType.STRING, filter.getWalletId());
        }
        filterDto.setSearchFields(searchFields);
        return filterDto;
    }

    public FilterDto convertPayoutChecks(Filter filter, Page page, Sort sort) {
        FilterDto filterDto = buildFilter(filter, page, sort, true);
        filterDto.setSearchFields(assemblePayoutCheckSearchFields(filter));
        return filterDto;
    }

    private Set<SearchFieldDto> assemblePayoutCheckSearchFields(Filter filter) {
        String resultStatus = StringUtils.hasLength(filter.getStatus())
                ? resolveResultStatus(filter.getStatus())
                : null;
        return Stream.of(
                        buildSearchField(PayoutCheckField.ID, FieldType.STRING, filter.getPaymentId()),
                        buildSearchField(PayoutCheckField.PARTY_ID, FieldType.STRING, filter.getPartyId()),
                        buildSearchField(PayoutCheckField.WALLET_ID, FieldType.STRING, filter.getWalletId()),
                        buildSearchField(PayoutCheckField.EMAIL, FieldType.STRING, filter.getEmail()),
                        buildSearchField(PayoutCheckField.CARD_TOKEN, FieldType.STRING, filter.getCardToken()),
                        buildSearchField(PayoutCheckField.FINGERPRINT, FieldType.STRING, filter.getFingerprint()),
                        buildSearchField(PayoutCheckField.LAST_DIGITS, FieldType.STRING, filter.getMaskedPan()),
                        buildSearchField(PayoutCheckField.CUSTOMER_ID, FieldType.STRING, filter.getCustomerId()),
                        buildSearchField(PayoutCheckField.USER_ID, FieldType.STRING, filter.getUserId()),
                        buildSearchField(PayoutCheckField.RESULT_STATUS, FieldType.ENUM, resultStatus),
                        buildSearchField(PayoutCheckField.CHECKED_RULE, FieldType.ENUM, filter.getRule()),
                        buildSearchField(PayoutCheckField.CHECKED_TEMPLATE, FieldType.ENUM, filter.getTemplate()))
                .flatMap(Optional::stream)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private static Optional<SearchFieldDto> buildSearchField(FilterField field, FieldType type, String value) {
        if (!StringUtils.hasLength(value)) {
            return Optional.empty();
        }
        return Optional.of(SearchFieldDto.builder()
                .field(field)
                .type(type)
                .value(value)
                .build());
    }

    private FilterDto buildFilter(Filter filter, Page page, Sort sort, boolean fraudResults) {
        FilterDto filterDto = new FilterDto();
        Set<SearchFieldDto> searchFields = assembleSearchFields(filter, fraudResults);
        filterDto.setSearchFields(searchFields);
        filterDto.setLastId(page.getContinuationId());
        if (page.getSize() > 0) {
            filterDto.setSize(page.getSize());
        }
        if (filter.isSetInterval()) {
            filterDto.setTimeFrom(filter.getInterval().getLowerBound().getBoundTime());
            filterDto.setTimeTo(filter.getInterval().getUpperBound().getBoundTime());
        }
        SortDto sortDto = new SortDto();
        sortDto.setField(sort.getField());
        sortDto.setOrder(Objects.nonNull(sort.getOrder())
                ? SortOrder.valueOf(sort.getOrder().name())
                : null);
        filterDto.setSort(sortDto);
        return filterDto;
    }

    private Set<SearchFieldDto> assembleSearchFields(Filter filter, boolean fraudResults) {
        Set<SearchFieldDto> searchFields = new HashSet<>();
        if (filter.isSetCardToken() && StringUtils.hasLength(filter.getCardToken())) {
            addSearchField(searchFields, PaymentField.CARD_TOKEN, FieldType.STRING, filter.getCardToken());
        }
        if (filter.isSetEmail() && StringUtils.hasLength(filter.getEmail())) {
            addSearchField(searchFields, PaymentField.EMAIL, FieldType.STRING, filter.getEmail());
        }
        if (filter.isSetStatus() && StringUtils.hasLength(filter.getStatus())) {
            if (fraudResults) {
                addSearchField(searchFields, FraudResultField.RESULT_STATUS, FieldType.ENUM,
                        resolveResultStatus(filter.getStatus()));
            } else {
                addSearchField(searchFields, PaymentField.STATUS, FieldType.ENUM, filter.getStatus());
            }
        }
        if (filter.isSetShopId() && StringUtils.hasLength(filter.getShopId())) {
            addSearchField(searchFields, PaymentField.SHOP_ID, FieldType.STRING, filter.getShopId());
        }
        if (filter.isSetPartyId() && StringUtils.hasLength(filter.getPartyId())) {
            addSearchField(searchFields, PaymentField.PARTY_ID, FieldType.STRING, filter.getPartyId());
        }
        if (filter.isSetProviderCountry() && StringUtils.hasLength(filter.getProviderCountry())) {
            addSearchField(searchFields, PaymentField.BANK_COUNTRY, FieldType.STRING, filter.getProviderCountry());
        }
        if (filter.isSetFingerprint() && StringUtils.hasLength(filter.getFingerprint())) {
            addSearchField(searchFields, PaymentField.FINGERPRINT, FieldType.STRING, filter.getFingerprint());
        }
        if (filter.isSetCustomerId() && StringUtils.hasLength(filter.getCustomerId())) {
            addSearchField(searchFields, PaymentField.CUSTOMER_ID, FieldType.STRING, filter.getCustomerId());
        }
        if (filter.isSetUserId() && StringUtils.hasLength(filter.getUserId())) {
            addSearchField(searchFields, PaymentField.USER_ID, FieldType.STRING, filter.getUserId());
        }
        if (filter.isSetTerminal() && StringUtils.hasLength(filter.getTerminal())) {
            addSearchField(searchFields, PaymentField.TERMINAL, FieldType.STRING, filter.getTerminal());
        }
        if (filter.isSetPaymentId() && StringUtils.hasLength(filter.getPaymentId())) {
            addSearchField(searchFields, PaymentField.ID, FieldType.STRING, filter.getPaymentId());
        }
        if (filter.isSetMaskedPan() && StringUtils.hasLength(filter.getMaskedPan())) {
            addSearchField(searchFields, PaymentField.LAST_DIGITS, FieldType.STRING, filter.getMaskedPan());
        }
        if (filter.isSetInvoiceId() && StringUtils.hasLength(filter.getInvoiceId())) {
            addSearchField(searchFields, PaymentField.INVOICE_ID, FieldType.STRING, filter.getInvoiceId());
        }
        if (filter.isSetTemplate() && StringUtils.hasLength(filter.getTemplate())) {
            addSearchField(searchFields, FraudResultField.CHECKED_TEMPLATE, FieldType.STRING, filter.getTemplate());
        }
        if (filter.isSetRule() && StringUtils.hasLength(filter.getRule())) {
            addSearchField(searchFields, FraudResultField.CHECKED_RULE, FieldType.STRING, filter.getRule());
        }
        return searchFields;
    }

    private String resolveResultStatus(String value) {
        String trimmed = value.trim();
        ResultStatus byValue = ResultStatus.getByValue(trimmed);
        if (byValue != null) {
            return byValue.name();
        }
        return Arrays.stream(ResultStatus.values())
                .filter(status -> status.name().equalsIgnoreCase(trimmed))
                .findFirst()
                .map(ResultStatus::name)
                .orElse(value);
    }

    private void addSearchField(Set<SearchFieldDto> searchFields,
                                FilterField field,
                                FieldType type,
                                String filterValue) {
        searchFields.add(
                SearchFieldDto.builder()
                        .field(field)
                        .type(type)
                        .value(filterValue)
                        .build()
        );
    }

}
