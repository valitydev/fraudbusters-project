package dev.vality.fraudbusters.management.resource.payment;

import dev.vality.damsel.fraudbusters.HistoricalDataServiceSrv;
import dev.vality.damsel.fraudbusters.HistoricalDataSetCheckResult;
import dev.vality.fraudbusters.management.converter.payment.*;
import dev.vality.fraudbusters.management.domain.payment.CheckedDataSetModel;
import dev.vality.fraudbusters.management.domain.payment.DataSetModel;
import dev.vality.fraudbusters.management.domain.request.FilterRequest;
import dev.vality.fraudbusters.management.service.payment.PaymentsDataSetService;
import dev.vality.fraudbusters.management.utils.PagingDataUtils;
import dev.vality.fraudbusters.management.utils.UserInfoService;
import dev.vality.swag.fraudbusters.management.api.PaymentsDataSetApi;
import dev.vality.swag.fraudbusters.management.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PaymentDataSetsResource implements PaymentsDataSetApi {

    private final UserInfoService userInfoService;
    private final PaymentsDataSetService paymentsDataSetService;
    private final DataSetModelToDataSetApiConverter dataSetModelToDataSetApiConverter;
    private final CheckedDataSetModelToCheckedDataSetApiConverter checkedDataSetModelToCheckedDataSetApiConverter;
    private final DataSetToTestDataSetModelConverter dataSetToTestDataSetModelConverter;
    private final HistoricalDataServiceSrv.Iface historicalDataServiceSrv;
    private final ApplyRuleOnHistoricalRequestToEmulationRuleApplyRequestConverter applyConverter;
    private final HistoricalDataSetCheckResultToTestCheckedDataSetModelConverter dataSetCheckResultToDaoModelConverter;

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<CheckedDataSet> getCheckedDataSet(String id) {
        String userName = userInfoService.getUserName();
        log.info("getCheckedDataSet initiator: {} id: {}", userName, id);
        var dataSet = paymentsDataSetService.getCheckedDataSet(id);
        log.info("getCheckedDataSet succeeded checkedDataSet: {}", dataSet);
        return ResponseEntity.ok(checkedDataSetModelToCheckedDataSetApiConverter.convert(dataSet));
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<String> applyRuleOnHistoricalDataSet(ApplyRuleOnHistoricalDataSetRequest request) {
        String userName = userInfoService.getUserName();
        log.info("applyRuleOnHistoricalDataSet initiator: {} request: {}", userName, request);
        try {
            var historicalDataSetCheckResult = historicalDataServiceSrv
                    .applyRuleOnHistoricalDataSet(applyConverter.convert(request));
            CheckedDataSetModel dataSetModel =
                    createCheckedDataSet(request, userName, historicalDataSetCheckResult);
            Long resultId = paymentsDataSetService.insertCheckedDataSet(dataSetModel, userName);
            log.info("applyRuleOnHistoricalDataSet resultId: {}", resultId);
            return ResponseEntity.ok().body(String.valueOf(resultId));
        } catch (TException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResponseEntity<DataSetsResponse> filterDataSets(String continuationId, String sortOrder,
                                                           String sortBy, Integer size,
                                                           String from, String to,
                                                           String dataSetName) {
        var filterRequest = FilterRequest.builder()
                .searchValue(dataSetName)
                .lastId(continuationId)
                .size(size)
                .sortBy(sortBy)
                .sortOrder(PagingDataUtils.getSortOrder(sortOrder))
                .build();
        String userName = userInfoService.getUserName();
        log.info("filterDataSets initiator: {} filterRequest: {}", userName, filterRequest);
        List<DataSetModel> dataSetModels = paymentsDataSetService.filterDataSets(from, to, filterRequest);
        return ResponseEntity.ok(new DataSetsResponse()
                .continuationId(buildContinuationId(size, dataSetModels))
                .result(dataSetModels.stream()
                        .map(dataSetModelToDataSetApiConverter::convert)
                        .collect(Collectors.toList())
                ));
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<DataSet> getDataSet(String setId) {
        String userName = userInfoService.getUserName();
        log.info("getDataSet initiator: {} id: {}", userName, setId);
        var dataSet = paymentsDataSetService.getDataSet(setId);
        log.info("getDataSet succeeded dataSet: {}", dataSet);
        return ResponseEntity.ok(dataSetModelToDataSetApiConverter.convert(dataSet));
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<IdResponse> insertDataSet(DataSet dataSet) {
        String userName = userInfoService.getUserName();
        log.info("insertDataSet initiator: {} dataSet: {}", userName, dataSet);
        DataSetModel dataSetModel = dataSetToTestDataSetModelConverter.convert(dataSet);
        dataSetModel.setLastModificationInitiator(userName);
        Long id = paymentsDataSetService.insertDataSet(dataSetModel);
        log.info("insertDataSet succeeded dataSet: {}", dataSet);
        return ResponseEntity.ok(new IdResponse()
                .id(String.valueOf(id))
        );
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<IdResponse> removeDataSet(String id) {
        String userName = userInfoService.getUserName();
        log.info("removeDataSet initiator: {} id: {}", userName, id);
        paymentsDataSetService.removeDataSet(id, userName);
        log.info("removeDataSet succeeded id: {}", id);
        return ResponseEntity.ok(new IdResponse()
                .id(id));
    }

    private CheckedDataSetModel createCheckedDataSet(ApplyRuleOnHistoricalDataSetRequest request,
                                                     String userName,
                                                     HistoricalDataSetCheckResult historicalDataSetCheckResult) {
        CheckedDataSetModel dataSetModel =
                dataSetCheckResultToDaoModelConverter.convert(historicalDataSetCheckResult);
        dataSetModel.setInitiator(userName);
        PaymentReference reference = request.getReference();
        if (reference != null) {
            dataSetModel.setPartyId(reference.getPartyId());
            dataSetModel.setShopId(reference.getShopId());
        }
        dataSetModel.setCheckingTimestamp(request.getRuleSetTimestamp());
        dataSetModel.setTestDataSetId(request.getDataSetId());
        dataSetModel.setTemplate(request.getTemplate());
        return dataSetModel;
    }

    private String buildContinuationId(Integer filterSize, List<DataSetModel> dataSetModels) {
        if (dataSetModels.size() == filterSize) {
            var lastDataSet = dataSetModels.get(dataSetModels.size() - 1);
            return lastDataSet.getId();
        }
        return null;
    }

}
