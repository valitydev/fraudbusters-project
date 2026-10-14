package dev.vality.fraudo.resolver;

public interface CountryResolver<T> {

    String resolveCountry(T checkedField, String value);

}
