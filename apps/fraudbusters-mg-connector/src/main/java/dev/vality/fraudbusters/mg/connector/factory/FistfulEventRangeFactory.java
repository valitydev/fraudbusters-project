package dev.vality.fraudbusters.mg.connector.factory;


import dev.vality.fistful.base.EventRange;
import org.springframework.stereotype.Service;

@Service
public class FistfulEventRangeFactory {

    public EventRange create(long eventNumber) {
        return new EventRange()
                .setLimit((int) eventNumber);
    }

}
