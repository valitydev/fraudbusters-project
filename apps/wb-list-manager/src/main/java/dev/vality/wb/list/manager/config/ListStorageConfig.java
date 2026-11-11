package dev.vality.wb.list.manager.config;

import dev.vality.wb.list.manager.repository.ListRepository;
import dev.vality.wb.list.manager.storage.ListStorage;
import dev.vality.wb.list.manager.storage.PaymentListStorage;
import dev.vality.wb.list.manager.storage.PayoutListStorage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class ListStorageConfig {

    @Bean
    public ListRepository listRepository(JdbcTemplate jdbcTemplate) {
        return new ListRepository(jdbcTemplate, "wb_list.row");
    }

    @Bean
    public ListRepository payoutListRepository(JdbcTemplate jdbcTemplate) {
        return new ListRepository(jdbcTemplate, "wb_list.payout_row");
    }

    @Bean
    public ListStorage paymentListStorage(ListRepository listRepository) {
        return new PaymentListStorage(listRepository);
    }

    @Bean
    public ListStorage payoutListStorage(ListRepository payoutListRepository) {
        return new PayoutListStorage(payoutListRepository);
    }

}
