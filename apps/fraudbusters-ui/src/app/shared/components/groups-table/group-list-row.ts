import { Group } from '../../../api/fb-management/swagger-codegen/model/group';
import { PriorityId } from '../../../api/fb-management/swagger-codegen/model/priorityId';
import { catalogTimestamp } from '../../utils/catalog';

export interface GroupListRow extends Group {
    lastUpdate: number | null;
    priorityTemplates: PriorityId[];
}

export function groupListRow(group: Group): GroupListRow {
    const priorityTemplates = [...(group.priorityTemplates ?? [])].sort(
        (left, right) => left.priority - right.priority
    );
    const dates = priorityTemplates
        .map(({ lastUpdateTime }) => catalogTimestamp(lastUpdateTime))
        .filter((date) => date !== null);
    return { ...group, priorityTemplates, lastUpdate: dates.length ? Math.max(...dates) : null };
}
