package dev.vality.fraudbusters.api.converter;

import dev.vality.damsel.fraudbusters.ShopContext;
import dev.vality.swag.fraudbusters.model.Merchant;
import dev.vality.swag.fraudbusters.model.Shop;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ShopContextToMerchantConverter implements Converter<ShopContext, Merchant> {

    @Override
    public Merchant convert(ShopContext shopContext) {
        Shop swaggerShop = new Shop();
        swaggerShop.id(shopContext.getShopId());
        return new Merchant()
                .id(shopContext.getPartyId())
                .shop(swaggerShop);
    }
}

