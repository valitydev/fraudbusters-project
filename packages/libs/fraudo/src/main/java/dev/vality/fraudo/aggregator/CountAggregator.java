package dev.vality.fraudo.aggregator;

import dev.vality.fraudo.model.TimeWindow;

import java.util.List;

public interface CountAggregator<T, U> {

    Integer count(U checkedField, T model, TimeWindow timeWindow, List<U> fields);

}
