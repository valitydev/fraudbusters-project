package dev.vality.fraudbusters.management.dao.payment.candidate;

import dev.vality.fraudbusters.management.TestObjectFactory;
import dev.vality.fraudbusters.management.config.PostgresqlJooqITest;
import dev.vality.fraudbusters.management.domain.WbListCandidateBatchModel;
import dev.vality.fraudbusters.management.domain.request.FilterRequest;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static dev.vality.fraudbusters.management.domain.tables.WbListCandidate.WB_LIST_CANDIDATE;
import static dev.vality.fraudbusters.management.domain.tables.WbListCandidateBatch.WB_LIST_CANDIDATE_BATCH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@PostgresqlJooqITest
@ContextConfiguration(classes = {WbListCandidateBatchDaoImpl.class})
class WbListCandidateBatchDaoImplTest {

    @Autowired
    WbListCandidateBatchDao wbListCandidateBatchDao;

    @Autowired
    DSLContext dslContext;

    @Test
    void save() {
        var candidateBatch = TestObjectFactory.testWbListCandidateBatch();

        wbListCandidateBatchDao.save(candidateBatch);

        assertEquals(1, dslContext.fetchCount(WB_LIST_CANDIDATE_BATCH));

        wbListCandidateBatchDao.save(candidateBatch);

        assertEquals(1, dslContext.fetchCount(WB_LIST_CANDIDATE_BATCH));
    }

    @Test
    void getListWithoutFilter() {
        var batchRecord1 = TestObjectFactory.testWbListCandidateBatchRecord();
        var batchRecord2 = TestObjectFactory.testWbListCandidateBatchRecord();

        dslContext.insertInto(WB_LIST_CANDIDATE_BATCH)
                .set(batchRecord1)
                .newRecord()
                .set(batchRecord2)
                .execute();

        var record1 = TestObjectFactory.testWbListCandidateRecord();
        record1.setBatchId(batchRecord1.getId());
        var record2 = TestObjectFactory.testWbListCandidateRecord();
        record2.setBatchId(batchRecord1.getId());
        var record3 = TestObjectFactory.testWbListCandidateRecord();
        record3.setBatchId(batchRecord2.getId());

        dslContext.insertInto(WB_LIST_CANDIDATE)
                .set(record1)
                .newRecord()
                .set(record2)
                .newRecord()
                .set(record3)
                .execute();

        List<WbListCandidateBatchModel> list = wbListCandidateBatchDao.getList(new FilterRequest());

        assertEquals(2, list.size());

        WbListCandidateBatchModel wbListCandidateBatchModel1 = list.stream()
                .filter(wbListCandidateBatchModel -> wbListCandidateBatchModel.getId().equals(batchRecord1.getId()))
                .findFirst()
                .get();

        assertEquals(2, wbListCandidateBatchModel1.getSize());
        assertTrue(wbListCandidateBatchModel1.getFields().contains(record1.getListName()));
        assertTrue(wbListCandidateBatchModel1.getFields().contains(record2.getListName()));
        assertEquals(batchRecord1.getSource(), wbListCandidateBatchModel1.getSource());

        WbListCandidateBatchModel wbListCandidateBatchModel2 = list.stream()
                .filter(wbListCandidateBatchModel -> wbListCandidateBatchModel.getId().equals(batchRecord2.getId()))
                .findFirst()
                .get();

        assertEquals(1, wbListCandidateBatchModel2.getSize());
        assertTrue(wbListCandidateBatchModel2.getFields().contains(record3.getListName()));
        assertEquals(batchRecord2.getSource(), wbListCandidateBatchModel2.getSource());
    }
}