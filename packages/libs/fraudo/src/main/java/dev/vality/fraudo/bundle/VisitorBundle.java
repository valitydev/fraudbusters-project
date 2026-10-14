package dev.vality.fraudo.bundle;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VisitorBundle<T, U> {

    private AggregatorBundle<T, U> aggregatorBundle;
    private ResolverBundle<T, U> resolverBundle;
    private FinderBundle<T, U> finderBundle;

}
