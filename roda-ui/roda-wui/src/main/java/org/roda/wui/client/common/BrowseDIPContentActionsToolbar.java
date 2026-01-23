/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SimplePanel;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.DisseminationActions;
import org.roda.wui.client.common.actions.DisseminationFileActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.common.client.tools.ConfigurationManager;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Eduardo Teixeira <eteixeira@keep.pt>
 */
public class BrowseDIPContentActionsToolbar extends ActionsToolbar {
  protected IndexedDIP dip;
  protected DIPFile dipFile;
  protected Permissions dipPermissions;
  protected AsyncCallback<Actionable.ActionImpact> actionCallback;

  public void setObjectsAndBuild(IndexedDIP dip, DIPFile dipFile, Permissions permissions,
    AsyncCallback<Actionable.ActionImpact> actionCallback) {
    this.dip = dip;
    this.dipFile = dipFile;
    this.dipPermissions = permissions;
    this.actionCallback = actionCallback;

    buildIcon();
    buildTags();
    buildActions();
  }

  public void buildActions() {
    this.actions.clear();
    List<Actionable.Action<IndexedDIP>> actionsToshow = new ArrayList<>();
    DisseminationActions dipActions = DisseminationActions.get(this.dipPermissions);
    if(this.dipFile != null) {
      DisseminationFileActions dipFileActions = DisseminationFileActions.get(this.dipPermissions);
      this.actions.add(new ActionableWidgetBuilder<DIPFile>(dipFileActions).withActionCallback(this.actionCallback)
        .buildGroupedListWithObjects(new ActionableObject<>(this.dipFile), java.util.Collections.emptyList(),
          List.of(DisseminationFileActions.DisseminationFileAction.DOWNLOAD)));

      actionsToshow.add(DisseminationActions.DisseminationAction.REMOVE);
      actionsToshow.add(DisseminationActions.DisseminationAction.NEW_PROCESS);
      //keeping ui consistency
      SimplePanel divider = new SimplePanel();
      divider.addStyleName("verticalDivider");
      this.actions.add(divider);

    } else {
      actionsToshow.add(DisseminationActions.DisseminationAction.DOWNLOAD);
      actionsToshow.add(DisseminationActions.DisseminationAction.REMOVE);
      actionsToshow.add(DisseminationActions.DisseminationAction.NEW_PROCESS);
    }


    this.actions.add(new ActionableWidgetBuilder<IndexedDIP>(dipActions).withActionCallback(this.actionCallback)
      .buildGroupedListWithObjects(new ActionableObject<>(this.dip), actionsToshow, actionsToshow));

  };

  public void buildIcon() {
    setIcon(ConfigurationManager.getString(RodaConstants.UI_ICONS_CLASS, IndexedDIP.class.getSimpleName()));
  }

  public void buildTags() {
    // do nothing
  }

}
