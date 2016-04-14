package org.roda.wui.client.common.dialogs;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.exceptions.NotFoundException;

public class SelectDialogFactory {

  @SuppressWarnings("rawtypes")
  public SelectDialog getSelectDialog(Class actualClass, String title, Filter filter) throws NotFoundException {
    if (actualClass.getSimpleName().equals("AIP")) {
      return new SelectAipDialog(title, filter);
    } else if (actualClass.getSimpleName().equals("Format")) {
      return new SelectFormatDialog(title, filter);
    } else if (actualClass.getSimpleName().equals("Agent")) {
      return new SelectAgentDialog(title, filter);
    } else {
      throw new NotFoundException();
    }
  }

}
