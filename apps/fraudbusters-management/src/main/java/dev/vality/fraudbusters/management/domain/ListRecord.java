package dev.vality.fraudbusters.management.domain;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ListRecord {

    @NotNull
    private String listName;
    @NotNull
    private String value;

}
