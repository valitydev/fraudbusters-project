package dev.vality.fraudbusters.management.service.payment;

import dev.vality.damsel.wb_list.Command;
import dev.vality.fraudbusters.management.converter.payment.WbListRecordToRowConverter;
import dev.vality.fraudbusters.management.converter.payment.WbListRecordsModelToWbListRecordConverter;
import dev.vality.fraudbusters.management.dao.payment.wblist.WbListDao;
import dev.vality.fraudbusters.management.domain.enums.ListType;
import dev.vality.fraudbusters.management.domain.request.FilterRequest;
import dev.vality.fraudbusters.management.domain.tables.pojos.WbListRecords;
import dev.vality.fraudbusters.management.exception.NotFoundException;
import dev.vality.fraudbusters.management.service.WbListCommandService;
import dev.vality.fraudbusters.management.utils.PaymentCountInfoGenerator;
import dev.vality.fraudbusters.management.utils.UserInfoService;
import dev.vality.fraudbusters.management.utils.parser.CsvPaymentCountInfoParser;
import dev.vality.fraudbusters.management.validator.ListRowValidator;
import dev.vality.swag.fraudbusters.management.model.PaymentCountInfo;
import dev.vality.swag.fraudbusters.management.model.WbListRecordsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentsListsService {

    private final WbListDao wbListDao;
    private final WbListCommandService wbListCommandService;
    private final WbListRecordToRowConverter wbListRecordToRowConverter;
    private final PaymentCountInfoGenerator paymentCountInfoGenerator;
    private final UserInfoService userInfoService;
    private final CsvPaymentCountInfoParser csvPaymentCountInfoParser;
    private final WbListRecordsModelToWbListRecordConverter wbListRecordsModelToWbListRecordConverter;
    private final ListRowValidator listRowValidator;

    public WbListRecordsResponse filterLists(List<String> listNames, String listType,
                                             FilterRequest filterRequest) {
        List<WbListRecords> wbListRecords =
                wbListDao.filterListRecords(ListType.valueOf(listType), listNames, filterRequest);
        Integer count =
                wbListDao.countFilterRecords(ListType.valueOf(listType), listNames, filterRequest.getSearchValue());
        return new WbListRecordsResponse()
                .count(count)
                .result(wbListRecords.stream()
                        .map(wbListRecordsModelToWbListRecordConverter::convert)
                        .collect(Collectors.toList()));
    }

    public void insertCsv(String listType, MultipartFile file, String initiator) {
        if (csvPaymentCountInfoParser.hasCsvFormat(file)) {
            try {
                List<PaymentCountInfo> paymentCountInfos = csvPaymentCountInfoParser.parse(file.getInputStream());
                log.info("Insert from csv paymentCountInfos size: {}", paymentCountInfos.size());
                listRowValidator.validate(paymentCountInfos);
                wbListCommandService.sendListRecords(
                        paymentCountInfos,
                        dev.vality.damsel.wb_list.ListType.valueOf(listType),
                        paymentCountInfoGenerator::initRow,
                        Command.CREATE,
                        initiator);
                log.info("Insert loaded fraudPayments: {}", paymentCountInfos);
            } catch (IOException e) {
                log.error("Insert error when loadFraudOperation e: ", e);
                throw new RuntimeException(e);
            }
        }
    }

    public void deleteByCsv(String listType, MultipartFile file, String initiator) {
        if (csvPaymentCountInfoParser.hasCsvFormat(file)) {
            try {
                List<PaymentCountInfo> paymentCountInfos = csvPaymentCountInfoParser.parse(file.getInputStream());
                log.info("Delete by csv paymentCountInfos size: {}", paymentCountInfos.size());
                listRowValidator.validate(paymentCountInfos);
                wbListCommandService.sendListRecords(
                        paymentCountInfos,
                        dev.vality.damsel.wb_list.ListType.valueOf(listType),
                        paymentCountInfoGenerator::initRow,
                        Command.DELETE,
                        initiator);
                log.info("Delete by csv fraudPayments: {}", paymentCountInfos);
            } catch (IOException e) {
                log.error("Error when deleteByCsv e: ", e);
                throw new RuntimeException(e);
            }
        }
    }

    public String removeListRecord(String id) {
        var wbListRecord = wbListDao.getById(id);
        if (wbListRecord == null) {
            log.error("List remove record not fount: {}", id);
            throw new NotFoundException(String.format("List record not found with id: %s", id));
        }
        log.info("removeRowFromList initiator: {} record {}", userInfoService.getUserName(), wbListRecord);
        var row = wbListRecordToRowConverter.convert(wbListRecord);
        return wbListCommandService.sendCommandSync(row,
                dev.vality.damsel.wb_list.ListType.valueOf(wbListRecord.getListType().name()),
                Command.DELETE,
                userInfoService.getUserName());
    }

    public List<String> getCurrentListNames(String listType) {
        return wbListDao.getCurrentListNames(ListType.valueOf(listType));
    }

}
