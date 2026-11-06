package dev.vality.fraudbusters.management.dao.payout.group;

import dev.vality.fraudbusters.management.dao.AbstractDao;
import dev.vality.fraudbusters.management.dao.CommandDao;
import dev.vality.fraudbusters.management.domain.GroupModel;
import dev.vality.fraudbusters.management.domain.PriorityIdModel;
import dev.vality.fraudbusters.management.domain.tables.pojos.PayoutGroup;
import dev.vality.mapper.RecordRowMapper;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static dev.vality.fraudbusters.management.domain.tables.PayoutGroup.PAYOUT_GROUP;

@Component
public class PayoutGroupDao extends AbstractDao implements CommandDao<GroupModel> {

    private final RowMapper<PayoutGroup> groupRowMapper;

    public PayoutGroupDao(DataSource dataSource) {
        super(dataSource);
        groupRowMapper = new RecordRowMapper<>(PAYOUT_GROUP, PayoutGroup.class);
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

    /**
     * The group with its templates in priority order, or {@code null} when the group has no templates.
     */
    public GroupModel getById(String groupId) {
        List<GroupModel> groups = toGroups(fetch(getDslContext()
                .selectFrom(PAYOUT_GROUP)
                .where(PAYOUT_GROUP.GROUP_ID.eq(groupId))
                .orderBy(PAYOUT_GROUP.PRIORITY), groupRowMapper));
        return groups.isEmpty() ? null : groups.get(0);
    }

    /** Groups whose ID or one of whose template IDs matches the LIKE pattern. */
    public List<GroupModel> filterGroup(String searchPattern) {
        var matchingGroups = getDslContext()
                .selectDistinct(PAYOUT_GROUP.GROUP_ID)
                .from(PAYOUT_GROUP)
                .where(StringUtils.hasText(searchPattern)
                        ? PAYOUT_GROUP.GROUP_ID.like(searchPattern).or(PAYOUT_GROUP.TEMPLATE_ID.like(searchPattern))
                        : DSL.noCondition());
        return toGroups(fetch(getDslContext()
                .selectFrom(PAYOUT_GROUP)
                .where(PAYOUT_GROUP.GROUP_ID.in(matchingGroups))
                .orderBy(PAYOUT_GROUP.GROUP_ID, PAYOUT_GROUP.PRIORITY), groupRowMapper));
    }

    private List<GroupModel> toGroups(List<PayoutGroup> records) {
        Map<String, GroupModel> groups = new LinkedHashMap<>();
        for (PayoutGroup record : records) {
            groups.computeIfAbsent(record.getGroupId(), id -> GroupModel.builder()
                            .groupId(id)
                            .priorityTemplates(new ArrayList<>())
                            .build())
                    .getPriorityTemplates()
                    .add(PriorityIdModel.builder()
                            .id(record.getTemplateId())
                            .priority(record.getPriority())
                            .build());
        }
        return new ArrayList<>(groups.values());
    }

}
