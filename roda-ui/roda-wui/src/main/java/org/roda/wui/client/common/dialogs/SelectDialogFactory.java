/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.exceptions.NotFoundException;

public class SelectDialogFactory {

  @SuppressWarnings("rawtypes")
  public SelectDialog getSelectDialog(Class actualClass, String title, Filter filter) throws NotFoundException {
    if (actualClass.getSimpleName().equals("AIP")) {
      boolean justActive = true;
      return new SelectAipDialog(title, filter, justActive);
    } else if (actualClass.getSimpleName().equals("Format")) {
      return new SelectFormatDialog(title, filter);
    } else if (actualClass.getSimpleName().equals("Agent")) {
      return new SelectAgentDialog(title, filter);
    } else if (actualClass.getSimpleName().equals("Risk")) {
      return new SelectRiskDialog(title, filter);
    } else {
      throw new NotFoundException();
    }
  }

}
