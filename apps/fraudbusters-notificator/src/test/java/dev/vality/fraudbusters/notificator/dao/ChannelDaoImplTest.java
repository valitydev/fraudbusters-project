package dev.vality.fraudbusters.notificator.dao;

import dev.vality.fraudbusters.notificator.utils.TestObjectsFactory;
import dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.Channel;
import dev.vality.fraudbusters.notificator.dao.domain.tables.records.ChannelRecord;
import dev.vality.fraudbusters.notificator.service.VaultSecretService;
import dev.vality.fraudbusters.notificator.service.dto.FilterDto;
import dev.vality.testcontainers.annotations.postgresql.PostgresqlTestcontainer;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.util.List;
import java.util.stream.Collectors;

import static dev.vality.fraudbusters.notificator.dao.domain.Tables.CHANNEL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

@ActiveProfiles("test")
@PostgresqlTestcontainer
@SpringBootTest
class ChannelDaoImplTest {

    @Container
    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer<>("postgres:14-alpine");

    @Autowired
    DSLContext dslContext;

    @Autowired
    ChannelDao channelDao;

    @MockitoBean
    private VaultSecretService vaultSecretService;

    @BeforeEach
    void setUp() {
        Mockito.when(vaultSecretService.getBotToken()).thenReturn("test");
    }

    @Test
    void getAll() {
        ChannelRecord channel1 = TestObjectsFactory.testChannelRecord();
        ChannelRecord channel2 = TestObjectsFactory.testChannelRecord();
        dslContext.insertInto(CHANNEL)
                .set(channel1)
                .newRecord()
                .set(channel2)
                .execute();

        List<Channel> all = channelDao.getAll(new FilterDto());

        assertEquals(2, all.size());
        List<String> names = all.stream()
                .map(Channel::getName)
                .collect(Collectors.toList());
        assertThat(names, hasItems(channel1.getName(), channel2.getName()));
    }

    @Test
    void getAllWithFilterContinuationId() {
        ChannelRecord channel1 = TestObjectsFactory.testChannelRecord();
        channel1.setName("a");
        ChannelRecord channel2 = TestObjectsFactory.testChannelRecord();
        channel2.setName("b");
        ChannelRecord channel3 = TestObjectsFactory.testChannelRecord();
        channel3.setName("c");
        dslContext.insertInto(CHANNEL)
                .set(channel1)
                .newRecord()
                .set(channel2)
                .newRecord()
                .set(channel3)
                .execute();

        FilterDto filter = new FilterDto();
        filter.setContinuationString(channel1.getName());
        List<Channel> all = channelDao.getAll(filter);

        assertEquals(2, all.size());
        assertIterableEquals(List.of(channel2.getName(), channel3.getName()),
                all.stream()
                        .map(Channel::getName)
                        .collect(Collectors.toList()));
    }

    @Test
    void getAllWithFilterSearchField() {
        ChannelRecord channel1 = TestObjectsFactory.testChannelRecord();
        ChannelRecord channel2 = TestObjectsFactory.testChannelRecord();
        channel2.setName(channel1.getName() + channel2.getName());
        ChannelRecord channel3 = TestObjectsFactory.testChannelRecord();
        channel3.setName(channel3.getName() + channel1.getName());
        ChannelRecord channel4 = TestObjectsFactory.testChannelRecord();
        channel4.setName(channel4.getName() + channel1.getName() + channel4.getName());
        ChannelRecord channel5 = TestObjectsFactory.testChannelRecord();
        dslContext.insertInto(CHANNEL)
                .set(channel1)
                .newRecord()
                .set(channel2)
                .newRecord()
                .set(channel3)
                .newRecord()
                .set(channel4)
                .newRecord()
                .set(channel5)
                .execute();

        FilterDto filter = new FilterDto();
        filter.setSearchFiled(channel1.getName());
        List<Channel> all = channelDao.getAll(filter);

        assertEquals(4, all.size());
        assertIterableEquals(List.of(channel1.getDestination(), channel2.getDestination(), channel3.getDestination(),
                        channel4.getDestination()),
                all.stream()
                        .map(Channel::getDestination)
                        .collect(Collectors.toList()));
    }

    @Test
    void getAllWithFilterSize() {
        ChannelRecord channel1 = TestObjectsFactory.testChannelRecord();
        channel1.setName("a");
        ChannelRecord channel2 = TestObjectsFactory.testChannelRecord();
        channel2.setName("b");
        ChannelRecord channel3 = TestObjectsFactory.testChannelRecord();
        channel3.setName("c");
        dslContext.insertInto(CHANNEL)
                .set(channel1)
                .newRecord()
                .set(channel2)
                .newRecord()
                .set(channel3)
                .execute();

        FilterDto filter = new FilterDto();
        filter.setSize(2L);
        List<Channel> all = channelDao.getAll(filter);

        assertEquals(2, all.size());
        assertIterableEquals(List.of(channel1.getName(), channel2.getName()),
                all.stream()
                        .map(Channel::getName)
                        .collect(Collectors.toList()));
    }
}