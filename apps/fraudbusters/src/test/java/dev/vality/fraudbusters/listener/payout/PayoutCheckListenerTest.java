package dev.vality.fraudbusters.listener.payout;

import dev.vality.fraudbusters.domain.PayoutResult;
import dev.vality.fraudbusters.repository.Repository;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PayoutCheckListenerTest {

    @SuppressWarnings("unchecked")
    private final Repository<PayoutResult> repository = mock(Repository.class);
    private final PayoutCheckListener listener = new PayoutCheckListener(repository);

    @Test
    void storesReadableResultsOfTheBatch() throws InterruptedException {
        PayoutResult first = new PayoutResult();
        PayoutResult second = new PayoutResult();

        listener.listen(Arrays.asList(first, null, second), 0, 10L);

        verify(repository).insertBatch(List.of(first, second));
    }

    @Test
    void rethrowsSoTheBatchIsRedelivered() {
        doThrow(new IllegalStateException("clickhouse is down")).when(repository).insertBatch(anyList());

        assertThrows(IllegalStateException.class, () -> listener.listen(List.of(new PayoutResult()), 0, 10L));
    }
}
