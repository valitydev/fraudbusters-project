package dev.vality.fraudbusters.management.service;

import dev.vality.damsel.wb_list.Command;
import dev.vality.damsel.wb_list.ListType;
import dev.vality.damsel.wb_list.Row;
import dev.vality.fraud_data_crawler.FraudDataCandidate;
import dev.vality.fraudbusters.management.TestObjectFactory;
import dev.vality.fraudbusters.management.config.PostgresqlJooqITest;
import dev.vality.fraudbusters.management.converter.candidate.WbListCandidateToRowConverter;
import dev.vality.fraudbusters.management.dao.payment.candidate.WbListCandidateDao;
import dev.vality.fraudbusters.management.dao.payment.candidate.WbListCandidateDaoImpl;
import dev.vality.fraudbusters.management.domain.request.FilterRequest;
import dev.vality.fraudbusters.management.domain.response.FilterResponse;
import dev.vality.fraudbusters.management.domain.tables.pojos.WbListCandidate;
import dev.vality.fraudbusters.management.domain.tables.records.WbListCandidateRecord;
import dev.vality.fraudbusters.management.service.iface.WbListCandidateService;
import org.apache.thrift.TBase;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static dev.vality.fraudbusters.management.domain.tables.WbListCandidate.WB_LIST_CANDIDATE;
import static dev.vality.fraudbusters.management.domain.tables.WbListCandidateBatch.WB_LIST_CANDIDATE_BATCH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@PostgresqlJooqITest
@ContextConfiguration(classes = {WbListCandidateDaoImpl.class})
class WbListCandidateServiceImplTest {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private WbListCandidateDao wbListCandidateDao;

    private WbListCandidateService wbListCandidateService;

    @Mock
    private KafkaTemplate<String, TBase> kafkaTemplate;

    @Mock
    private WbListCommandService wbListCommandService;
    @Mock
    private CompletableFuture<SendResult<String, TBase>> listenableFuture;

    @BeforeEach
    void setUp() {
        WbListCandidateToRowConverter converter = new WbListCandidateToRowConverter();
        wbListCandidateService = new WbListCandidateServiceImpl(
                wbListCandidateDao,
                kafkaTemplate,
                wbListCommandService,
                converter
        );
        ReflectionTestUtils.setField(wbListCandidateService, "topicCandidate", "wb-list-candidate");
    }

    @Test
    void sendToCandidate() {
        int count = 3;
        List<FraudDataCandidate> fraudDataCandidates = TestObjectFactory.testFraudDataCandidates(count);
        when(kafkaTemplate.send(anyString(), any(), any())).thenReturn(listenableFuture);
        String key = wbListCandidateService.sendToCandidate(fraudDataCandidates);

        assertEquals(fraudDataCandidates.get(0).getBatchId(), key);
        verify(kafkaTemplate, times(count)).send(anyString(), anyString(), any(FraudDataCandidate.class));
    }

    @Test
    void getList() {
        var batchRecord = TestObjectFactory.testWbListCandidateBatchRecord();
        dslContext.insertInto(WB_LIST_CANDIDATE_BATCH)
                .set(batchRecord)
                .execute();
        WbListCandidateRecord record1 = TestObjectFactory.testWbListCandidateRecord();
        record1.setApproved(Boolean.FALSE);
        record1.setBatchId(batchRecord.getId());
        WbListCandidateRecord record2 = TestObjectFactory.testWbListCandidateRecord();
        record2.setApproved(Boolean.FALSE);
        record2.setListName(record1.getListName());
        record2.setBatchId(batchRecord.getId());
        WbListCandidateRecord record3 = TestObjectFactory.testWbListCandidateRecord();
        record3.setApproved(Boolean.FALSE);
        record3.setListName(record1.getListName());
        record3.setBatchId(batchRecord.getId());
        dslContext.insertInto(WB_LIST_CANDIDATE)
                .set(record1)
                .newRecord()
                .set(record2)
                .newRecord()
                .set(record3)
                .execute();
        List<Long> ids = dslContext.fetch(WB_LIST_CANDIDATE).stream()
                .map(WbListCandidateRecord::getId)
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
        FilterRequest filter = new FilterRequest();
        int size = ids.size() - 1;
        filter.setSize(size);

        FilterResponse<WbListCandidate> filterResponse = wbListCandidateService.getList(filter);

        assertEquals(size, filterResponse.getResult().size());
        assertTrue(filterResponse.getNumericLastId() > 0);
        Long lastId = ids.get(ids.size() - 2);
        assertEquals(lastId, filterResponse.getNumericLastId());
    }

    @Test
    void approve() {
        var batchRecord = TestObjectFactory.testWbListCandidateBatchRecord();
        dslContext.insertInto(WB_LIST_CANDIDATE_BATCH)
                .set(batchRecord)
                .execute();
        WbListCandidateRecord record1 = TestObjectFactory.testWbListCandidateRecord();
        record1.setApproved(Boolean.FALSE);
        record1.setBatchId(batchRecord.getId());
        WbListCandidateRecord record2 = TestObjectFactory.testWbListCandidateRecord();
        record2.setApproved(Boolean.FALSE);
        record2.setBatchId(batchRecord.getId());
        WbListCandidateRecord record3 = TestObjectFactory.testWbListCandidateRecord();
        record3.setApproved(Boolean.FALSE);
        record3.setBatchId(batchRecord.getId());
        dslContext.insertInto(WB_LIST_CANDIDATE)
                .set(record1)
                .newRecord()
                .set(record2)
                .newRecord()
                .set(record3)
                .execute();
        List<Long> ids = dslContext.fetch(WB_LIST_CANDIDATE).stream()
                .map(WbListCandidateRecord::getId)
                .collect(Collectors.toList());
        when(wbListCommandService.sendCommandSync(any(Row.class), any(ListType.class), any(Command.class), anyString()))
                .thenReturn(TestObjectFactory.randomString());
        String initiator = "initiator";

        wbListCandidateService.approve(ids, initiator);

        verify(wbListCommandService, times(3))
                .sendCommandSync(any(Row.class), any(ListType.class), any(Command.class), anyString());
        assertTrue(dslContext.fetch(WB_LIST_CANDIDATE).stream()
                .allMatch(WbListCandidateRecord::getApproved));
    }
}