package dev.vality.fraudbusters.management.resource.payment;

import dev.vality.damsel.wb_list.Command;
import dev.vality.damsel.wb_list.ListType;
import dev.vality.fraud_data_crawler.FraudDataCandidate;
import dev.vality.fraudbusters.management.converter.candidate.ChargebacksToFraudDataCandidatesConverter;
import dev.vality.fraudbusters.management.converter.candidate.WbListCandidateToWbListRecordConverter;
import dev.vality.fraudbusters.management.converter.payment.CandidateBatchModelToCandidateBatchConverter;
import dev.vality.fraudbusters.management.domain.WbListCandidateBatchModel;
import dev.vality.fraudbusters.management.domain.request.FilterRequest;
import dev.vality.fraudbusters.management.domain.response.FilterResponse;
import dev.vality.fraudbusters.management.domain.tables.pojos.WbListCandidate;
import dev.vality.fraudbusters.management.exception.SaveRowsException;
import dev.vality.fraudbusters.management.service.WbListCommandService;
import dev.vality.fraudbusters.management.service.iface.WbListCandidateBatchService;
import dev.vality.fraudbusters.management.service.iface.WbListCandidateService;
import dev.vality.fraudbusters.management.service.payment.PaymentsListsService;
import dev.vality.fraudbusters.management.utils.PagingDataUtils;
import dev.vality.fraudbusters.management.utils.ParametersService;
import dev.vality.fraudbusters.management.utils.PaymentCountInfoGenerator;
import dev.vality.fraudbusters.management.utils.UserInfoService;
import dev.vality.fraudbusters.management.validator.ListRowValidator;
import dev.vality.swag.fraudbusters.management.api.PaymentsListsApi;
import dev.vality.swag.fraudbusters.management.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PaymentsListsResource implements PaymentsListsApi {

    public static final String UNKNOWN = "UNKNOWN";

    private final WbListCommandService wbListCommandService;
    private final PaymentCountInfoGenerator paymentCountInfoGenerator;
    private final UserInfoService userInfoService;
    private final ParametersService parametersService;
    private final PaymentsListsService paymentsListsService;
    private final WbListCandidateService wbListCandidateService;
    private final WbListCandidateBatchService wbListCandidateBatchService;
    private final WbListCandidateToWbListRecordConverter candidateConverter;
    private final CandidateBatchModelToCandidateBatchConverter candidateBatchConverter;
    private final ChargebacksToFraudDataCandidatesConverter chargebacksConverter;
    private final ListRowValidator listRowValidator;

    @Override
    @PreAuthorize("hasAnyRole('fraud-monitoring', 'fraud-officer')")
    public ResponseEntity<Void> approveListCandidates(IdListRequest listRequest) {
        List<Long> ids = listRequest.getIds();
        log.info("approveFraudCandidates with ids: {}", Arrays.toString(ids.toArray()));
        wbListCandidateService.approve(ids, userInfoService.getUserName());
        log.info("Success approveFraudCandidates with ids: {}", Arrays.toString(ids.toArray()));
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-monitoring', 'fraud-officer')")
    public ResponseEntity<WbListRecordsResponse> filterLists(List<String> listNames,
                                                             String listType, String lastId,
                                                             String sortOrder, String searchValue,
                                                             String sortBy, String sortFieldValue,
                                                             Integer size) {
        var filterRequest = FilterRequest.builder()
                .lastId(lastId)
                .sortOrder(PagingDataUtils.getSortOrder(sortOrder))
                .searchValue(searchValue)
                .sortBy(sortBy)
                .sortFieldValue(sortFieldValue)
                .size(size)
                .build();
        String userName = userInfoService.getUserName();
        log.info("filterList initiator: {} listType: {} listNames: {} filterRequest: {}",
                userName, listType, listNames, filterRequest);
        WbListRecordsResponse result = paymentsListsService.filterLists(listNames, listType, filterRequest);
        return ResponseEntity.ok(result
        );
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-monitoring', 'fraud-officer')")
    public ResponseEntity<ListResponse> getAvailableListNames() {
        var listResponse = new ListResponse();
        listResponse.setResult(parametersService.getAvailableListNames());
        return ResponseEntity.ok().body(listResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-monitoring', 'fraud-officer')")
    public ResponseEntity<ListResponse> insertRow(RowListRequest request) {
        log.info("insertRowsToList initiator: {} request {}", userInfoService.getUserName(), request);
        try {
            listRowValidator.validate(request.getRecords());
            List<String> ids = wbListCommandService.sendListRecords(
                    request.getRecords(),
                    ListType.valueOf(request.getListType().getValue()),
                    paymentCountInfoGenerator::initRow,
                    Command.CREATE,
                    userInfoService.getUserName());
            return ResponseEntity.ok().body(new ListResponse()
                    .result(ids));
        } catch (SaveRowsException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-monitoring', 'fraud-officer')")
    public ResponseEntity<WbListCandidatesResponse> listCandidates(Long lastId,
                                                                   String sortOrder,
                                                                   String searchValue,
                                                                   String sortBy,
                                                                   String sortFieldValue,
                                                                   Integer size) {
        FilterRequest filter = FilterRequest.builder()
                .numericLastId(lastId)
                .sortOrder(PagingDataUtils.getSortOrder(sortOrder))
                .searchValue(searchValue)
                .sortBy(sortBy)
                .sortFieldValue(sortFieldValue)
                .size(size)
                .build();
        log.info("listCandidates with filter: {}", filter);
        FilterResponse<WbListCandidate> filterResponse = wbListCandidateService.getList(filter);
        WbListCandidatesResponse result = new WbListCandidatesResponse()
                .lastId(filterResponse.getNumericLastId())
                .source(filterResponse.getResult().size() > 0
                        ? filterResponse.getResult().get(0).getSource()
                        : UNKNOWN)
                .result(candidateConverter.toWbListRecord(filterResponse.getResult()));
        log.info("Success listCandidates with result: {}", result);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<WbListCandidatesBatchesResponse> listCandidatesBatches(String lastId,
                                                                                 String sortOrder,
                                                                                 String searchValue,
                                                                                 String sortBy,
                                                                                 String sortFieldValue,
                                                                                 Integer size) {
        FilterRequest filter = FilterRequest.builder()
                .lastId(lastId)
                .sortOrder(PagingDataUtils.getSortOrder(sortOrder))
                .searchValue(searchValue)
                .sortBy(sortBy)
                .sortFieldValue(sortFieldValue)
                .size(size)
                .build();
        log.info("listCandidatesBatches with filter: {}", filter);
        FilterResponse<WbListCandidateBatchModel> filterResponse = wbListCandidateBatchService.getList(filter);
        WbListCandidatesBatchesResponse result = new WbListCandidatesBatchesResponse()
                .lastId(filterResponse.getLastId())
                .result(candidateBatchConverter.toWbListCandidateBatch(filterResponse.getResult()));
        log.info("Success listCandidatesBatches with result: {}", result);
        return ResponseEntity.ok(result);
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-monitoring', 'fraud-officer')")
    public ResponseEntity<ListResponse> getCurrentListNames(String listType) {
        var listResponse = new ListResponse();
        listResponse.setResult(paymentsListsService.getCurrentListNames(listType));
        return ResponseEntity.ok().body(listResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-monitoring', 'fraud-officer')")
    public ResponseEntity<Void> insertFromCsv(String listType, MultipartFile file) {
        String userName = userInfoService.getUserName();
        log.info("Insert from csv initiator: {} listType: {}", userName, listType);
        paymentsListsService.insertCsv(listType, file, userName);
        return ResponseEntity.ok().build();
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-monitoring', 'fraud-officer')")
    public ResponseEntity<Void> deleteByCsv(String listType, MultipartFile file) {
        String userName = userInfoService.getUserName();
        log.info("Delete by csv initiator: {} listType: {}", userName, listType);
        paymentsListsService.deleteByCsv(listType, file, userName);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> insertListCandidates(WbListCandidatesRequest wbListCandidatesRequest) {
        String batchId = UUID.randomUUID().toString();
        log.info("insertListCandidates with request: {}, batchId: {}", wbListCandidatesRequest, batchId);
        List<FraudDataCandidate> fraudDataCandidates =
                chargebacksConverter.toCandidates(wbListCandidatesRequest.getRecords(), batchId);
        String key = wbListCandidateService.sendToCandidate(fraudDataCandidates);
        log.info("Success insertListCandidates with key: {}", key);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-monitoring', 'fraud-officer')")
    public ResponseEntity<IdResponse> removeRow(String id) {
        String idMessage = paymentsListsService.removeListRecord(id);
        return ResponseEntity.ok().body(
                new IdResponse()
                        .id(idMessage)
        );
    }

}
