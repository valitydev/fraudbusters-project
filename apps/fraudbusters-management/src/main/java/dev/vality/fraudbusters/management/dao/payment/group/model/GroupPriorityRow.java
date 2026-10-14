package dev.vality.fraudbusters.management.dao.payment.group.model;

import dev.vality.fraudbusters.management.domain.PriorityIdModel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GroupPriorityRow {

    private String groupId;
    private PriorityIdModel priorityIdModel;

}
