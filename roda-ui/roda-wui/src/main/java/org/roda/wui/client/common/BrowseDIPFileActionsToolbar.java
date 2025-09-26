package org.roda.wui.client.common;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.wui.client.common.actions.DisseminationFileActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.common.client.tools.ConfigurationManager;

/**
 *
 * @author Eduardo Teixeira <eteixeira@keep.pt>
 */
public class BrowseDIPFileActionsToolbar extends BrowseObjectActionsToolbar<DIPFile> {
  public void buildIcon() {
    setIcon(ConfigurationManager.getString(RodaConstants.UI_ICONS_CLASS, DIPFile.class.getSimpleName()));
  }

  public void buildTags() {
    // do nothing
  }

  public void buildActions() {
    this.actions.clear();
    // AIP actions
    DisseminationFileActions dipFileActions;
    dipFileActions = DisseminationFileActions.get(actionPermissions);
    this.actions.add(new ActionableWidgetBuilder<DIPFile>(dipFileActions).withActionCallback(actionCallback)
      .buildGroupedListWithObjects(new ActionableObject<>(object), new ArrayList<>(),
        List.of(DisseminationFileActions.DisseminationFileAction.DOWNLOAD)));

  }
}
