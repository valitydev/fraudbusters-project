package dev.vality.fraudo.bundle;

import dev.vality.fraudo.payment.resolver.CustomerTypeResolver;
import dev.vality.fraudo.resolver.impl.GroupFieldsResolverImpl;
import dev.vality.fraudo.resolver.impl.TimeWindowResolverImpl;
import dev.vality.fraudo.payment.resolver.PaymentTypeResolver;
import dev.vality.fraudo.resolver.CountryResolver;
import dev.vality.fraudo.resolver.FieldResolver;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResolverBundle<T, U> {

    private CountryResolver<U> countryResolver;
    private FieldResolver<T, U> fieldPairResolver;
    private GroupFieldsResolverImpl<T, U> groupFieldsResolver;
    private TimeWindowResolverImpl timeWindowResolver;
    private PaymentTypeResolver<T> paymentTypeResolver;
    private CustomerTypeResolver<T> customerTypeResolver;

}
