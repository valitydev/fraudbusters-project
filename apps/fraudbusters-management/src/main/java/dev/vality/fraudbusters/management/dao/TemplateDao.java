package dev.vality.fraudbusters.management.dao;

import dev.vality.fraudbusters.management.domain.TemplateModel;
import dev.vality.fraudbusters.management.domain.request.FilterRequest;

import java.util.List;

public interface TemplateDao {

    void insert(TemplateModel listRecord);

    void remove(String id);

    void remove(TemplateModel listRecord);

    TemplateModel getById(String id);

    List<String> getListNames(String idRegexp);

    List<TemplateModel> filterModel(FilterRequest filterRequest);

    Integer countFilterModel(String id);
}
