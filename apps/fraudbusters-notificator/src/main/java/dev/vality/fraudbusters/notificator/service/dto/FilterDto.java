package dev.vality.fraudbusters.notificator.service.dto;

import lombok.Data;

@Data
public class FilterDto {

    private static final Long DEFAULT_PAGE_SIZE = 10L;

    private String searchFiled;
    private Long size = DEFAULT_PAGE_SIZE;
    private Long continuationId;
    private String continuationString;

}
