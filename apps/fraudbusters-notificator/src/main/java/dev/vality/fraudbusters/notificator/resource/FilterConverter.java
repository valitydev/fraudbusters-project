package dev.vality.fraudbusters.notificator.resource;

import dev.vality.damsel.fraudbusters_notificator.Filter;
import dev.vality.damsel.fraudbusters_notificator.Page;
import dev.vality.fraudbusters.notificator.service.dto.FilterDto;
import org.springframework.stereotype.Component;

@Component
class FilterConverter {

    public FilterDto convert(Page page, Filter filter) {
        FilterDto filterDto = new FilterDto();
        filterDto.setSearchFiled(filter.getSearchField());
        if (page.getSize() > 0) {
            filterDto.setSize(page.getSize());
        }
        try {
            filterDto.setContinuationId(Long.parseLong(page.getContinuationId()));
        } catch (NumberFormatException e) {
            filterDto.setContinuationString(page.getContinuationId());
        }
        return filterDto;
    }
}
