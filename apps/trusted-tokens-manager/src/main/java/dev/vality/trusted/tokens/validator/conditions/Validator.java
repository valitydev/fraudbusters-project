package dev.vality.trusted.tokens.validator.conditions;

import dev.vality.trusted.tokens.Condition;
import dev.vality.trusted.tokens.InvalidRequest;

import java.util.List;

public interface Validator {

    void validateConditions(List<Condition> conditions) throws InvalidRequest;

}
