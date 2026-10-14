package dev.vality.fraudbusters.management.kafka;

import dev.vality.fraud_data_crawler.FraudDataCandidate;
import dev.vality.fraudbusters.management.TestObjectFactory;
import dev.vality.fraudbusters.management.config.KafkaITest;
import dev.vality.fraudbusters.management.domain.tables.pojos.WbListCandidate;
import dev.vality.fraudbusters.management.service.iface.WbListCandidateBatchService;
import dev.vality.fraudbusters.management.service.iface.WbListCandidateService;
import dev.vality.fraudbusters.management.utils.TestKafkaProducer;
import org.apache.thrift.TBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.*;

@KafkaITest
@SpringBootTest
class WbListCandidateTest {

    @Value("${kafka.topic.wblist.candidate}")
    public String topicCandidate;
    @MockitoBean
    public WbListCandidateService wbListCandidateService;

    @MockitoBean
    public WbListCandidateBatchService wbListCandidateBatchService;

    @Autowired
    private TestKafkaProducer<TBase<?, ?>> testKafkaProducer;

    @Test
    void listenCandidate() throws ExecutionException, InterruptedException {
        FraudDataCandidate fraudDataCandidate = TestObjectFactory.testFraudDataCandidate();
        doNothing().when(wbListCandidateService).save(any(WbListCandidate.class));
        doNothing().when(wbListCandidateBatchService).save(anyString(), anyString());

        testKafkaProducer.send(topicCandidate, fraudDataCandidate);

        verify(wbListCandidateService, timeout(5000).times(1)).save(any(WbListCandidate.class));
    }
}
