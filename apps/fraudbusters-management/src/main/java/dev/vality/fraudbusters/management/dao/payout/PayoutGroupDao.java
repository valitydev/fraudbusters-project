package dev.vality.fraudbusters.management.dao.payout;

import dev.vality.fraudbusters.management.dao.AbstractDao;
import dev.vality.fraudbusters.management.dao.CommandDao;
import dev.vality.fraudbusters.management.domain.GroupModel;
import dev.vality.fraudbusters.management.domain.PriorityIdModel;
import org.jooq.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.util.Comparator;
import java.util.List;

import static dev.vality.fraudbusters.management.domain.tables.PayoutGroup.PAYOUT_GROUP;

@Component
public class PayoutGroupDao extends AbstractDao implements CommandDao<GroupModel> {

    public PayoutGroupDao(DataSource dataSource) {
        super(dataSource);
    }

    /** Replaces the whole group; a repeated template is kept at its highest priority. */
    @Override
    @Transactional
    public void insert(GroupModel groupModel) {
        remove(groupModel);
        if (CollectionUtils.isEmpty(groupModel.getPriorityTemplates())) {
            return;
        }
        List<Query> inserts = groupModel.getPriorityTemplates().stream()
                .sorted(Comparator.comparing(PriorityIdModel::getPriority))
                .<Query>map(priorityId -> getDslContext()
                        .insertInto(PAYOUT_GROUP)
                        .set(PAYOUT_GROUP.GROUP_ID, groupModel.getGroupId())
                        .set(PAYOUT_GROUP.TEMPLATE_ID, priorityId.getId())
                        .set(PAYOUT_GROUP.PRIORITY, priorityId.getPriority())
                        .onConflict(PAYOUT_GROUP.GROUP_ID, PAYOUT_GROUP.TEMPLATE_ID)
                        .doNothing())
                .toList();
        batchExecute(inserts);
    }

    @Override
    public void remove(GroupModel groupModel) {
        execute(getDslContext()
                .delete(PAYOUT_GROUP)
                .where(PAYOUT_GROUP.GROUP_ID.eq(groupModel.getGroupId())));
    }

}
