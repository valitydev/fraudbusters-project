package dev.vality.fraudbusters.fraud.model;

import lombok.Value;

import java.util.List;

@Value
public class AggregationFields {

    FieldModel field;
    List<FieldModel> groups;
    String countedField;

}
