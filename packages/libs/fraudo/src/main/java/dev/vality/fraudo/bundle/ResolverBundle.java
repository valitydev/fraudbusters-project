package dev.vality.fraudo.bundle;

import dev.vality.fraudo.payment.resolver.CustomerTypeResolver;
import dev.vality.fraudo.payment.resolver.PaymentGroupResolver;
import dev.vality.fraudo.payment.resolver.PaymentTimeWindowResolver;
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
    private PaymentGroupResolver<T, U> paymentGroupResolver;
    private PaymentTimeWindowResolver timeWindowResolver;
    private PaymentTypeResolver<T> paymentTypeResolver;
    private CustomerTypeResolver<T> customerTypeResolver;

}
