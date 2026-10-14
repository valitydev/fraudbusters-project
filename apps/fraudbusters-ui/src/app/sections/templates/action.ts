import { SortOrder } from '../../shared/constants/sort-order';

export enum ActionType {
    CreateTemplate = 'createTemplate',
    EditTemplate = 'editTemplate',
    RemoveTemplate = 'removeTemplate',
    SortTemplates = 'sortTemplates',
}

export interface Action {
    type: ActionType;
    templateID?: string;
    sortDirection?: SortOrder;
}
