package dev.vality.fraudbusters.management.dao.payout.template;

import dev.vality.fraudbusters.management.dao.AbstractDao;
import dev.vality.fraudbusters.management.dao.CommandDao;
import dev.vality.fraudbusters.management.domain.TemplateModel;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static dev.vality.fraudbusters.management.domain.tables.PayoutTemplate.PAYOUT_TEMPLATE;

@Component
public class PayoutTemplateDao extends AbstractDao implements CommandDao<TemplateModel> {

    public PayoutTemplateDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void insert(TemplateModel templateModel) {
        execute(getDslContext()
                .insertInto(PAYOUT_TEMPLATE)
                .set(PAYOUT_TEMPLATE.ID, templateModel.getId())
                .set(PAYOUT_TEMPLATE.TEMPLATE, templateModel.getTemplate())
                .onConflict(PAYOUT_TEMPLATE.ID)
                .doUpdate()
                .set(PAYOUT_TEMPLATE.TEMPLATE, templateModel.getTemplate()));
    }

    @Override
    public void remove(TemplateModel templateModel) {
        execute(getDslContext()
                .delete(PAYOUT_TEMPLATE)
                .where(PAYOUT_TEMPLATE.ID.eq(templateModel.getId())));
    }

}
