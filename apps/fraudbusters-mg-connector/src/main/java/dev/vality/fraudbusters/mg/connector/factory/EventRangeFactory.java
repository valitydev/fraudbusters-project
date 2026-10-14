package dev.vality.fraudbusters.mg.connector.factory;


import dev.vality.damsel.payment_processing.EventRange;
import org.springframework.stereotype.Service;

@Service
public class EventRangeFactory {

    public EventRange create(long eventNumber) {
        return new EventRange()
                .setLimit((int) eventNumber);
    }

}
