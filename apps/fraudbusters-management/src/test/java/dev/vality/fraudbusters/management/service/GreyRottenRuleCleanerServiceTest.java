package dev.vality.fraudbusters.management.service;

import dev.vality.damsel.wb_list.Command;
import dev.vality.damsel.wb_list.ListType;
import dev.vality.damsel.wb_list.Row;
import dev.vality.fraudbusters.management.converter.payment.WbListRecordToRowConverter;
import dev.vality.fraudbusters.management.dao.payment.wblist.WbListDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static dev.vality.fraudbusters.management.TestObjectFactory.createWbListRecords;
import static dev.vality.fraudbusters.management.TestObjectFactory.randomString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GreyRottenRuleCleanerServiceTest {


    public static final int FRESH_PERIOD = 2;
    private GreyRottenRuleCleanerService greyRottenRuleCleanerService;

    @Mock
    private WbListCommandService wbListCommandService;

    @Mock
    private WbListDao wbListDao;

    @BeforeEach
    void setUp() {
        WbListRecordToRowConverter wbListRecordToRowConverter = new WbListRecordToRowConverter();
        greyRottenRuleCleanerService = new GreyRottenRuleCleanerService(wbListDao, wbListCommandService,
                wbListRecordToRowConverter);
        ReflectionTestUtils.setField(greyRottenRuleCleanerService, "freshPeriod", FRESH_PERIOD);
    }

    @Test
    void notExistRottenRecords() {
        when(wbListDao.getRottenRecords(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        greyRottenRuleCleanerService.clean();

        verify(wbListCommandService, times(0))
                .sendCommandSync(any(Row.class), any(ListType.class), any(Command.class), anyString());
    }

    @Test
    void cleanRottenRecords() {
        when(wbListDao.getRottenRecords(any(LocalDateTime.class)))
                .thenReturn(List.of(createWbListRecords(randomString())));

        greyRottenRuleCleanerService.clean();

        verify(wbListCommandService, atLeastOnce())
                .sendCommandSync(any(Row.class), any(ListType.class), any(Command.class), anyString());
    }
}


