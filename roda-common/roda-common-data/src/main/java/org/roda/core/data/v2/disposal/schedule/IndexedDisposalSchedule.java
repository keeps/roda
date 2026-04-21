package org.roda.core.data.v2.disposal.schedule;

import org.roda.core.data.v2.index.IsIndexed;

import java.util.List;
import java.util.Map;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class IndexedDisposalSchedule implements IsIndexed {
    @Override
    public String getUUID() {
        return "";
    }

    @Override
    public List<String> toCsvHeaders() {
        return List.of();
    }

    @Override
    public List<Object> toCsvValues() {
        return List.of();
    }

    @Override
    public List<String> liteFields() {
        return List.of();
    }

    @Override
    public Map<String, Object> getFields() {
        return Map.of();
    }

    @Override
    public void setFields(Map<String, Object> fields) {

    }

    @Override
    public String getId() {
        return "";
    }
}
