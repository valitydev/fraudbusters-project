package dev.vality.fraudbusters.management.converter.payment;


import dev.vality.fraudbusters.management.domain.TemplateModel;
import dev.vality.swag.fraudbusters.management.model.Template;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TemplateModelToTemplateConverter {

    Template destinationToSource(TemplateModel destination);

}
