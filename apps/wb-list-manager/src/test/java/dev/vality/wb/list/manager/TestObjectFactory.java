package dev.vality.wb.list.manager;

import dev.vality.damsel.wb_list.*;
import dev.vality.wb.list.manager.utils.ChangeCommandWrapper;

import java.util.UUID;

public abstract class TestObjectFactory {

    public static Row testRow() {
        Row row = new Row();
        row.setId(IdInfo.payment_id(new PaymentId()
                .setShopId(randomString())
                .setPartyId(randomString())
        ));
        row.setListType(ListType.black);
        row.setListName(randomString());
        row.setValue(randomString());
        row.setPartyId(row.getId().getPaymentId().getPartyId());
        row.setShopId(row.getId().getPaymentId().getShopId());
        return row;
    }

    public static Row testRowWithEmptyListName() {
        Row row = new Row();
        row.setId(IdInfo.payment_id(new PaymentId()
                .setShopId(randomString())
                .setPartyId(randomString())
        ));
        row.setListType(ListType.black);
        row.setListName("");
        row.setValue(randomString());
        row.setPartyId(row.getId().getPaymentId().getPartyId());
        row.setShopId(row.getId().getPaymentId().getShopId());
        return row;
    }

    public static String randomString() {
        return UUID.randomUUID().toString();
    }

    public static ChangeCommandWrapper testCommand() {
        ChangeCommandWrapper changeCommand = new ChangeCommandWrapper();
        changeCommand.setCommand(Command.CREATE);
        dev.vality.damsel.wb_list.Row row = testRow();
        changeCommand.setRow(row);
        return changeCommand;
    }

}
