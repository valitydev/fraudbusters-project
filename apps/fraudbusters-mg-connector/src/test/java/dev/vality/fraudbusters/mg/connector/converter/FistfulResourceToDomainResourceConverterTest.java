package dev.vality.fraudbusters.mg.connector.converter;

import dev.vality.damsel.fraudbusters.Resource;
import dev.vality.fistful.base.ResourceBankCard;
import dev.vality.fistful.base.ResourceCryptoWallet;
import dev.vality.fistful.base.ResourceDigitalWallet;
import dev.vality.fraudbusters.mg.connector.exception.UnknownResourceException;
import dev.vality.fraudbusters.mg.connector.utils.BuildUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class FistfulResourceToDomainResourceConverterTest {

    @Autowired
    private FistfulResourceToDomainResourceConverter converter;

    @Test
    void convertResourceBankCardTest() {
        ResourceBankCard resourceBankCard = new ResourceBankCard();
        resourceBankCard.setBankCard(BuildUtils.buildFistfulBankCard());
        dev.vality.fistful.base.Resource baseResource = new dev.vality.fistful.base.Resource();
        baseResource.setBankCard(resourceBankCard);

        Resource resource = converter.convert(baseResource);
        assertNotNull(resource);
        assertTrue(resource.isSetBankCard());
    }

    @Test
    void convertResourceCryptoWalletTest() {
        ResourceCryptoWallet resourceCryptoWallet = new ResourceCryptoWallet();
        resourceCryptoWallet.setCryptoWallet(BuildUtils.buildFistfulCryptoWallet());
        dev.vality.fistful.base.Resource baseResource = new dev.vality.fistful.base.Resource();
        baseResource.setCryptoWallet(resourceCryptoWallet);

        Resource resource = converter.convert(baseResource);
        assertNotNull(resource);
        assertTrue(resource.isSetCryptoWallet());
    }

    @Test
    void convertResourceDigitalWalletTest() {
        ResourceDigitalWallet resourceDigitalWallet = new ResourceDigitalWallet();
        resourceDigitalWallet.setDigitalWallet(BuildUtils.buildFistfulDigitalWallet());
        dev.vality.fistful.base.Resource baseResource = new dev.vality.fistful.base.Resource();
        baseResource.setDigitalWallet(resourceDigitalWallet);

        Resource resource = converter.convert(baseResource);
        assertNotNull(resource);
        assertTrue(resource.isSetDigitalWallet());
    }

    @Test
    void convertUnknownResourceExceptionTest() {
        assertThrows(UnknownResourceException.class, () -> {
            converter.convert(new dev.vality.fistful.base.Resource());
        });
    }

}