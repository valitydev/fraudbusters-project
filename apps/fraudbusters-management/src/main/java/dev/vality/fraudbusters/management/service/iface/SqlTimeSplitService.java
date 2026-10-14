package dev.vality.fraudbusters.management.service.iface;

import dev.vality.fraudbusters.management.domain.TimeSplitInfo;
import dev.vality.swag.fraudbusters.management.model.SplitUnit;

import java.util.Map;

public interface SqlTimeSplitService {

    TimeSplitInfo getSplitInfo(SplitUnit splitUnit);

    Long calculateSplitOffset(Map<String, String> row, SplitUnit splitUnit);
}
