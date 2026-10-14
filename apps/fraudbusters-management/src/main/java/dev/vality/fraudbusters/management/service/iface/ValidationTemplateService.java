package dev.vality.fraudbusters.management.service.iface;

import dev.vality.damsel.fraudbusters.Template;
import dev.vality.damsel.fraudbusters.TemplateValidateError;

import java.util.List;

public interface ValidationTemplateService {

    List<TemplateValidateError> validateTemplate(Template template);

}
