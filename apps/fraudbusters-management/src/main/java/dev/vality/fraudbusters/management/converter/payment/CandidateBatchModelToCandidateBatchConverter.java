package dev.vality.fraudbusters.management.converter.payment;

import dev.vality.fraudbusters.management.domain.WbListCandidateBatchModel;
import dev.vality.swag.fraudbusters.management.model.WbListCandidateBatch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class CandidateBatchModelToCandidateBatchConverter {

    public List<WbListCandidateBatch> toWbListCandidateBatch(List<WbListCandidateBatchModel> source) {
        return source.stream()
                .map(this::toWbListCandidateBatch)
                .collect(Collectors.toList());
    }


    public WbListCandidateBatch toWbListCandidateBatch(WbListCandidateBatchModel batchModel) {
        return new WbListCandidateBatch()
                .id(batchModel.getId())
                .createdAt(batchModel.getInsertTime())
                .source(batchModel.getSource())
                .size(batchModel.getSize())
                .fields(batchModel.getFields());
    }
}
