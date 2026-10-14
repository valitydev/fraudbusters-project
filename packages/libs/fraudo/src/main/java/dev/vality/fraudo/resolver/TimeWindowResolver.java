package dev.vality.fraudo.resolver;

import dev.vality.fraudo.model.TimeWindow;

public interface TimeWindowResolver<T> {

    TimeWindow resolve(T ctx);

}
