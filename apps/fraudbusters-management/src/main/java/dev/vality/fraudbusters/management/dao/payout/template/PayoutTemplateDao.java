package dev.vality.fraudbusters.management.dao.payout.template;

import dev.vality.fraudbusters.management.dao.AbstractDao;
import dev.vality.fraudbusters.management.dao.CommandDao;
import dev.vality.fraudbusters.management.domain.TemplateModel;
import dev.vality.fraudbusters.management.domain.request.FilterRequest;
import dev.vality.mapper.RecordRowMapper;
import org.jooq.Condition;
import org.jooq.impl.DSL;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.List;

import static dev.vality.fraudbusters.management.domain.tables.PayoutTemplate.PAYOUT_TEMPLATE;

@Component
public class PayoutTemplateDao extends AbstractDao implements CommandDao<TemplateModel> {

    private final RowMapper<TemplateModel> templateRowMapper;

    public PayoutTemplateDao(DataSource dataSource) {
        super(dataSource);
        templateRowMapper = new RecordRowMapper<>(PAYOUT_TEMPLATE, TemplateModel.class);
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

    public TemplateModel getById(String id) {
        return fetchOne(getDslContext()
                .selectFrom(PAYOUT_TEMPLATE)
                .where(PAYOUT_TEMPLATE.ID.eq(id)), templateRowMapper);
    }

    public List<String> getListNames(String idPattern) {
        return fetch(getDslContext()
                .select(PAYOUT_TEMPLATE.ID)
                .from(PAYOUT_TEMPLATE)
                .where(StringUtils.hasText(idPattern) ? PAYOUT_TEMPLATE.ID.like(idPattern) : DSL.noCondition())
                .orderBy(PAYOUT_TEMPLATE.ID), (resultSet, i) -> resultSet.getString(PAYOUT_TEMPLATE.ID.getName()));
    }

    /**
     * The search value is a LIKE pattern on the template ID; pages continue after {@code lastId}.
     */
    public List<TemplateModel> filterModel(FilterRequest filterRequest) {
        var where = getDslContext()
                .selectFrom(PAYOUT_TEMPLATE)
                .where(searchCondition(filterRequest.getSearchValue()));
        var sorted = addSortCondition(PAYOUT_TEMPLATE.ID, filterRequest.getSortOrder(), where);
        return fetch(addSeekIfNeed(filterRequest.getLastId(), filterRequest.getSize(), sorted), templateRowMapper);
    }

    public Integer countFilterModel(String searchValue) {
        return fetchOne(getDslContext()
                .selectCount()
                .from(PAYOUT_TEMPLATE)
                .where(searchCondition(searchValue)), Integer.class);
    }

    private Condition searchCondition(String searchValue) {
        return StringUtils.hasText(searchValue) ? PAYOUT_TEMPLATE.ID.like(searchValue) : DSL.noCondition();
    }

}
