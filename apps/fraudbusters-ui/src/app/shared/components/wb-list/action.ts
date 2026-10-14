import { SortOrder } from '../../constants/sort-order';

export enum ActionType {
    CreateRow = 'createRow',
    RemoveRow = 'removeRow',
    SortRow = 'sortRow',
}

export interface Action {
    type: ActionType;
    rowId?: string;
    sortDirection?: SortOrder;
}
