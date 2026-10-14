package dev.vality.fraudbusters.notificator.service.factory;

import dev.vality.fraudbusters.notificator.utils.DateUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Service
public class CsvAttachmentFactory implements AttachmentFactory {

    public static final String FILE_POSTFIX = ".csv";

    @Override
    public String create(List<Map<String, String>> list) {
        List<String> headers = list.stream().flatMap(map -> map.keySet().stream()).distinct().collect(toList());
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < headers.size(); i++) {
            sb.append(headers.get(i));
            sb.append(i == headers.size() - 1 ? "\n" : ",");
        }
        for (Map<String, String> map : list) {
            for (int i = 0; i < headers.size(); i++) {
                sb.append(map.get(headers.get(i)));
                sb.append(i == headers.size() - 1 ? "\n" : ",");
            }
        }
        return sb.toString();
    }

    @Override
    public String createNameOfAttachment(String name) {
        return name + "_" + DateUtils.timeToString(LocalDateTime.now()) + FILE_POSTFIX;
    }
}
