package dev.vality.fraudbusters.mg.connector.mapper.impl;

import dev.vality.damsel.domain.DomainObject;
import dev.vality.damsel.domain.WalletAccount;
import dev.vality.damsel.domain.WalletConfig;
import dev.vality.damsel.domain.WalletConfigObject;
import dev.vality.damsel.domain_config_v2.VersionedObject;
import dev.vality.damsel.domain_config_v2.VersionedObjectInfo;
import dev.vality.fistful.base.*;
import dev.vality.fistful.destination.DestinationState;
import dev.vality.fistful.withdrawal.Change;
import dev.vality.fistful.withdrawal.StatusChange;
import dev.vality.fistful.withdrawal.TimestampedChange;
import dev.vality.fistful.withdrawal.status.Status;
import dev.vality.fraudbusters.mg.connector.utils.InvoiceTestConstant;

public class WithdrawalBeanUtils {

    public static final long WALLET_ACCOUNT_ID = 123;
    public static final String RUB = "RUB";

    public static DestinationState createDestinationState() {
        final Resource resource = new Resource();
        resource.setBankCard(new ResourceBankCard()
                .setBankCard(new BankCard()
                        .setBankName(InvoiceTestConstant.BANK_NAME)
                        .setBin(InvoiceTestConstant.CARD_BIN)
                        .setCategory(InvoiceTestConstant.CARD_CATEGORY)
                        .setIssuerCountry(Residence.PAN)
                        .setPaymentSystem(new PaymentSystemRef("asd"))
                        .setToken(InvoiceTestConstant.CARD_TOKEN_PROVIDER)
                        .setMaskedPan(InvoiceTestConstant.CARD_MASKED_PAN)
                        .setCardType(CardType.debit)
                        .setCardholderName(InvoiceTestConstant.CARDHOLDER_NAME)));
        return new DestinationState().setResource(resource);
    }

    public static TimestampedChange createStatusChange(Status failed) {
        final Change change = new Change();
        final TimestampedChange timestampedChange = new TimestampedChange();
        change.setStatusChanged(new StatusChange()
                .setStatus(failed));

        timestampedChange.setChange(change);
        return timestampedChange;
    }

    public static WalletConfigObject createWalletConfigObject() {
        return new WalletConfigObject()
                .setData(new WalletConfig()
                        .setAccount(new WalletAccount()
                                .setCurrency(new dev.vality.damsel.domain.CurrencyRef("RUB"))
                                .setSettlement(WALLET_ACCOUNT_ID)));
    }

    public static VersionedObject createVersionedObject(DomainObject domainObject) {
        VersionedObject versionedObject = new VersionedObject();
        versionedObject.setObject(domainObject);
        versionedObject.setInfo(new VersionedObjectInfo()
                .setVersion(1L));
        return versionedObject;
    }

    public static Cash createCash() {
        return new Cash()
                .setAmount(100L)
                .setCurrency(new CurrencyRef(RUB));
    }

}
