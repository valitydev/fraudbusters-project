package dev.vality.fraudbusters.api.converter;

import dev.vality.damsel.fraudbusters.Error;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ErrorToInternalDtoConverter
        implements Converter<dev.vality.swag.fraudbusters.model.Error, Error> {

    @Override
    public Error convert(dev.vality.swag.fraudbusters.model.Error error) {
        if (error == null) {
            return null;
        }
        return new Error()
                .setErrorCode(error.getErrorCode())
                .setErrorReason(error.getErrorMessage());
    }

}
