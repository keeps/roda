package org.roda.wui.client.common.lists;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;

public class AIPTopList extends AIPList {
  @Override
  protected void adjustOptions(AsyncTableCellOptions<IndexedAIP> options) {
    super.adjustOptions(options);

    List<String> fieldsToReturn = new ArrayList<>(options.getFieldsToReturn());
    fieldsToReturn.addAll(RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
    options.withFieldsToReturn(fieldsToReturn);
  }
}
