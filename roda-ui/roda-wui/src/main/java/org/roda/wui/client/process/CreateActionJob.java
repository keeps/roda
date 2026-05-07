package org.roda.wui.client.process;

import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.wui.client.common.utils.PluginUtils;

import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class CreateActionJob extends CreateSelectedJob {

  public CreateActionJob() {
    super(PluginUtils.getPluginTypesWithoutIngestAndInternal(), false);
  }

  @Override
  public boolean updateObjectList() {
    SelectedItems<?> selected = getSelected();
    boolean isEmpty = false;

    if (selected != null) {
      if (selected instanceof SelectedItemsList || selected instanceof SelectedItemsFilter) {
        if (selected instanceof SelectedItemsList) {
          List<String> ids = ((SelectedItemsList<?>) selected).getIds();
          isEmpty = ids.isEmpty();
        }
      } else {
        isEmpty = true;
      }
    } else {
      isEmpty = true;
    }
    return isEmpty;
  }
}
