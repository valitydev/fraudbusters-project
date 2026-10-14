package dev.vality.fraudbusters.mg.connector.domain;

import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.machinegun.eventsink.MachineEvent;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MgEventWrapper {

    private InvoiceChange change;
    private MachineEvent event;

}
