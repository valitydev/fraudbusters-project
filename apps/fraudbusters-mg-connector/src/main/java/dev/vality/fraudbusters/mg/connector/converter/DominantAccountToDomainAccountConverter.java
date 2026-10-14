package dev.vality.fraudbusters.mg.connector.converter;

import dev.vality.damsel.domain.WalletAccount;
import dev.vality.damsel.fraudbusters.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DominantAccountToDomainAccountConverter
        implements Converter<WalletAccount, Account> {

    @Override
    public Account convert(WalletAccount source) {
        log.debug("Start convert wallet account : {}", source);
        var account = new Account();
        account.setCurrency(source.getCurrency());
        account.setId(String.valueOf(source.getSettlement()));
        log.debug("Finish convert WalletAccount : {} to domainAccount: {}", source, account);
        return account;
    }

}
