package dev.vality.fraudbusters.mg.connector.converter;

import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.domain.CountryCode;
import dev.vality.damsel.domain.PaymentSystemRef;
import dev.vality.damsel.fraudbusters.*;
import dev.vality.fraudbusters.mg.connector.constant.PaymentSystemType;
import dev.vality.fraudbusters.mg.connector.exception.UnknownResourceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FistfulResourceToDomainResourceConverter
        implements Converter<dev.vality.fistful.base.Resource, Resource> {

    public static final PaymentSystemRef DEFAULT_PAYMENT_SYSTEM =
            new PaymentSystemRef(PaymentSystemType.visa.name());
    public static final String UNKNOWN = "UNKNOWN";

    @Override
    public Resource convert(dev.vality.fistful.base.Resource fistfulResource) {
        log.debug("Start convert fistfulResource : {}", fistfulResource);
        Resource resource = new Resource();
        if (fistfulResource.isSetBankCard()) {
            BankCard bankCard = convertBankCard(fistfulResource.getBankCard().getBankCard());
            resource.setBankCard(bankCard);
        } else if (fistfulResource.isSetCryptoWallet()) {
            CryptoWallet cryptoWallet = new CryptoWallet()
                    .setId(fistfulResource.getCryptoWallet().getCryptoWallet().getId())
                    .setCurrency(fistfulResource.getCryptoWallet().getCryptoWallet().getCurrency().id);
            resource.setCryptoWallet(cryptoWallet);
        } else if (fistfulResource.isSetDigitalWallet()) {
            DigitalWallet digitalWallet = new DigitalWallet()
                    .setId(fistfulResource.getDigitalWallet().getDigitalWallet().getId());
            if (fistfulResource.getDigitalWallet().getDigitalWallet().getPaymentService() != null) {
                digitalWallet.setDigitalDataProvider(fistfulResource.getDigitalWallet()
                        .getDigitalWallet().getPaymentService().getId());
            }
            resource.setDigitalWallet(digitalWallet);
        } else if (fistfulResource.isSetGeneric()) {
            GenericPaymentTool genericPaymentTool = new GenericPaymentTool();
            genericPaymentTool.setId(fistfulResource.getGeneric().getGeneric().getProvider().getId());
            if (fistfulResource.getGeneric().getGeneric().isSetData()) {
                genericPaymentTool.setContent(new Content()
                        .setData(fistfulResource.getGeneric().getGeneric().getData().getData())
                        .setType(fistfulResource.getGeneric().getGeneric().getData().getType())
                );
            }
            resource.setGeneric(genericPaymentTool);
        } else {
            log.error("Unknown resource type: {}", fistfulResource);
            throw new UnknownResourceException();
        }
        log.debug("Finish convert fistfulResource : {} to domainResource: {}", fistfulResource, resource);
        return resource;
    }

    private BankCard convertBankCard(dev.vality.fistful.base.BankCard bankCardFrom) {
        BankCard bankCard = new BankCard();
        bankCard.setToken(bankCardFrom.getToken());
        bankCard.setIssuerCountry(bankCardFrom.isSetIssuerCountry()
                ? CountryCode.valueOf(bankCardFrom.getIssuerCountry().name())
                : null);
        bankCard.setPaymentSystem(bankCardFrom.isSetPaymentSystem()
                ? new PaymentSystemRef(bankCardFrom.getPaymentSystem().getId())
                : DEFAULT_PAYMENT_SYSTEM);
        bankCard.setLastDigits(bankCardFrom.getMaskedPan() != null
                ? bankCardFrom.getMaskedPan()
                : UNKNOWN);
        bankCard.setBin(bankCardFrom.getBin() != null
                ? bankCardFrom.getBin()
                : UNKNOWN);
        bankCard.setCategory(bankCardFrom.getCategory());
        bankCard.setBankName(bankCardFrom.getBankName());
        return bankCard;
    }
}
