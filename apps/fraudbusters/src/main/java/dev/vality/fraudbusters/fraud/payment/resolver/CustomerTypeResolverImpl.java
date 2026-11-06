package dev.vality.fraudbusters.fraud.payment.resolver;

import dev.vality.fraudbusters.exception.RuleFunctionException;
import dev.vality.fraudbusters.fraud.model.PaymentModel;
import dev.vality.fraudbusters.fraud.pool.CardTokenPool;
import dev.vality.fraudo.payment.resolver.CustomerTypeResolver;
import dev.vality.trusted.tokens.TrustedTokensSrv;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerTypeResolverImpl implements CustomerTypeResolver<PaymentModel> {

    private final CardTokenPool cardTokenPool;

    private final TrustedTokensSrv.Iface trustedTokensSrv;

    @Override
    public Boolean isTrusted(PaymentModel paymentModel) {
        return cardTokenPool.isExist(paymentModel.getCardToken());
    }

    @Override
    public Boolean isTrusted(PaymentModel model, String templateName) {
        try {
            return trustedTokensSrv.isTokenTrustedByConditionTemplateName(model.getCardToken(), templateName);
        } catch (TException e) {
            throw new RuleFunctionException(e);
        }
    }

}
