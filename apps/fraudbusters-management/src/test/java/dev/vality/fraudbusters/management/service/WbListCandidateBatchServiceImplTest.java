package dev.vality.fraudbusters.management.service;

import dev.vality.fraudbusters.management.TestObjectFactory;
import dev.vality.fraudbusters.management.config.PostgresqlJooqITest;
import dev.vality.fraudbusters.management.dao.payment.candidate.WbListCandidateBatchDao;
import dev.vality.fraudbusters.management.dao.payment.candidate.WbListCandidateBatchDaoImpl;
import dev.vality.fraudbusters.management.domain.WbListCandidateBatchModel;
import dev.vality.fraudbusters.management.domain.request.FilterRequest;
import dev.vality.fraudbusters.management.domain.response.FilterResponse;
import dev.vality.fraudbusters.management.domain.tables.records.WbListCandidateBatchRecord;
import dev.vality.fraudbusters.management.service.iface.WbListCandidateBatchService;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static dev.vality.fraudbusters.management.domain.tables.WbListCandidate.WB_LIST_CANDIDATE;
import static dev.vality.fraudbusters.management.domain.tables.WbListCandidateBatch.WB_LIST_CANDIDATE_BATCH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@PostgresqlJooqITest
@ContextConfiguration(classes = {WbListCandidateBatchDaoImpl.class})
class WbListCandidateBatchServiceImplTest {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private WbListCandidateBatchDao wbListCandidateBatchDao;

    private WbListCandidateBatchService wbListCandidateBatchService;

    @BeforeEach
    void setUp() {
        wbListCandidateBatchService = new WbListCandidateBatchServiceImpl(wbListCandidateBatchDao);
    }

    @Test
    void save() {
        String id = TestObjectFactory.randomString();
        String source = TestObjectFactory.randomString();

        wbListCandidateBatchService.save(id, source);

        WbListCandidateBatchRecord record = dslContext.fetchAny(WB_LIST_CANDIDATE_BATCH);
        assertEquals(id, record.getId());
        assertEquals(source, record.getSource());
    }

    @Test
    void getList() {
        var batchRecord1 = TestObjectFactory.testWbListCandidateBatchRecord();
        batchRecord1.setId("a");
        var batchRecord2 = TestObjectFactory.testWbListCandidateBatchRecord();
        batchRecord2.setId("b");
        var batchRecord3 = TestObjectFactory.testWbListCandidateBatchRecord();
        batchRecord3.setId("c");

        dslContext.insertInto(WB_LIST_CANDIDATE_BATCH)
                .set(batchRecord1)
                .newRecord()
                .set(batchRecord2)
                .newRecord()
                .set(batchRecord3)
                .execute();

        var record1 = TestObjectFactory.testWbListCandidateRecord();
        record1.setBatchId(batchRecord1.getId());
        var record2 = TestObjectFactory.testWbListCandidateRecord();
        record2.setBatchId(batchRecord2.getId());
        var record3 = TestObjectFactory.testWbListCandidateRecord();
        record3.setBatchId(batchRecord3.getId());

        dslContext.insertInto(WB_LIST_CANDIDATE)
                .set(record1)
                .newRecord()
                .set(record2)
                .newRecord()
                .set(record3)
                .execute();

        FilterRequest filter = new FilterRequest();
        filter.setSize(2);

        FilterResponse<WbListCandidateBatchModel> filterResponse = wbListCandidateBatchService.getList(filter);

        assertEquals(2, filterResponse.getResult().size());
        assertNotNull(filterResponse.getLastId());
        assertEquals(batchRecord2.getId(), filterResponse.getLastId());
    }
}