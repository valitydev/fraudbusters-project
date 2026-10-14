package dev.vality.fraudo.bundle;

import dev.vality.fraudo.finder.InListFinder;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FinderBundle<T, U> {

    private InListFinder<T, U> listFinder;

}
