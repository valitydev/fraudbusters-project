package dev.vality.fraudbusters.management.dao.payment.candidate;

import dev.vality.fraudbusters.management.TestObjectFactory;
import dev.vality.fraudbusters.management.config.PostgresqlJooqITest;
import dev.vality.fraudbusters.management.domain.tables.records.WbListCandidateRecord;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static dev.vality.fraudbusters.management.domain.tables.WbListCandidate.WB_LIST_CANDIDATE;
import static dev.vality.fraudbusters.management.domain.tables.WbListCandidateBatch.WB_LIST_CANDIDATE_BATCH;
import static org.junit.jupiter.api.Assertions.assertEquals;

@PostgresqlJooqITest
@ContextConfiguration(classes = {WbListCandidateDaoImpl.class})
class FingerprintCandidateListNameMigrationTest {

    private static final String MIGRATION = "db/migration/V23__fix_fingerprint_candidate_list_name.sql";
    private static final String MISSPELLED = "FINGERPRRINT";
    private static final String FINGERPRINT = "FINGERPRINT";

    @Autowired
    DSLContext dslContext;

    private String batchId;

    @BeforeEach
    void setUp() {
        dslContext.deleteFrom(WB_LIST_CANDIDATE).execute();
        var batchRecord = TestObjectFactory.testWbListCandidateBatchRecord();
        dslContext.insertInto(WB_LIST_CANDIDATE_BATCH)
                .set(batchRecord)
                .execute();
        batchId = batchRecord.getId();
    }

    @Test
    void movesPendingCandidateToFingerprintList() throws IOException {
        Long id = insertCandidate(MISSPELLED, "fp-pending", false);

        applyMigration();

        assertEquals(FINGERPRINT, findListName(id));
    }

    @Test
    void keepsApprovedCandidateInMisspelledList() throws IOException {
        Long id = insertCandidate(MISSPELLED, "fp-approved", true);

        applyMigration();

        assertEquals(MISSPELLED, findListName(id));
    }

    @Test
    void keepsPendingCandidateWhoseValueIsAlreadyInFingerprintList() throws IOException {
        Long misspelledId = insertCandidate(MISSPELLED, "fp-duplicate", false);
        Long correctId = insertCandidate(FINGERPRINT, "fp-duplicate", false);

        applyMigration();

        assertEquals(MISSPELLED, findListName(misspelledId));
        assertEquals(FINGERPRINT, findListName(correctId));
    }

    private Long insertCandidate(String listName, String value, boolean approved) {
        WbListCandidateRecord record = TestObjectFactory.testWbListCandidateRecord();
        record.setBatchId(batchId);
        record.setListName(listName);
        record.setValue(value);
        record.setApproved(approved);
        return dslContext.insertInto(WB_LIST_CANDIDATE)
                .set(record)
                .returning(WB_LIST_CANDIDATE.ID)
                .fetchOne()
                .getId();
    }

    private void applyMigration() throws IOException {
        dslContext.execute(new ClassPathResource(MIGRATION).getContentAsString(StandardCharsets.UTF_8));
    }

    private String findListName(Long id) {
        return dslContext.select(WB_LIST_CANDIDATE.LIST_NAME)
                .from(WB_LIST_CANDIDATE)
                .where(WB_LIST_CANDIDATE.ID.eq(id))
                .fetchOne(WB_LIST_CANDIDATE.LIST_NAME);
    }
}
