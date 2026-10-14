package dev.vality.fraudbusters.management.domain.response;

import dev.vality.fraudbusters.management.domain.ErrorTemplateModel;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ValidateTemplatesResponse {

    private List<ErrorTemplateModel> validateResults;

}
