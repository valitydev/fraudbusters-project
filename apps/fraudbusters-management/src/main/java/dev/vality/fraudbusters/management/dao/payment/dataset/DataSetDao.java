package dev.vality.fraudbusters.management.dao.payment.dataset;

import dev.vality.fraudbusters.management.domain.payment.DataSetModel;
import dev.vality.fraudbusters.management.domain.request.FilterRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DataSetDao {

    Optional<Long> insert(DataSetModel dataSetModel);

    void remove(Long id);

    DataSetModel getById(Long id);

    List<DataSetModel> filter(LocalDateTime from, LocalDateTime to, FilterRequest filterRequest);

}
