import { ListType } from '../../../shared/constants/list-type';
import { SearchParams } from '../../../shared/model/search-params';

export interface SearchListsParams extends SearchParams {
    listNames?: string[];
    listType: ListType;
}
