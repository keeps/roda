package org.roda.wui.client.browse.tabs;

import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.EmptyKeyFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.wui.client.browse.DipFilePreview;
import org.roda.wui.client.browse.DipUrlPreview;
import org.roda.wui.client.browse.EditPermissionsTab;
import org.roda.wui.client.browse.Viewers;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.DisseminationActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.lists.DIPFileList;
import org.roda.wui.client.common.lists.LogEntryList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.model.BrowseDIPResponse;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.common.utils.PermissionClientUtils;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class BrowseDIPTabs extends Tabs {
  public void init(Viewers viewers, BrowseDIPResponse browseDIPResponse,
    AsyncCallback<Actionable.ActionImpact> actionCallback) {
    IndexedDIP dip = browseDIPResponse.getDip();
    DIPFile dipFile = browseDIPResponse.getDipFile();

    // DIPFile preview
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.descriptiveMetadataTab()), new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        if (dipFile != null) {
          return new DipFilePreview(viewers, dipFile);
        } else if (dip.getOpenExternalURL() != null) {
          return new DipUrlPreview(viewers, dip);
        } else {
          final Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.DIPFILE_DIP_ID, dip.getId()),
            new EmptyKeyFilterParameter(RodaConstants.DIPFILE_PARENT_UUID));

          ListBuilder<DIPFile> dipFileListBuilder = new ListBuilder<>(DIPFileList::new,
            new AsyncTableCellOptions<>(DIPFile.class, "BrowseDIP_dipFiles").withFilter(filter)
              .withSummary(messages.allOfAObject(DIPFile.class.getName())).bindOpener());

          return new SearchWrapper(false).createListAndSearchPanel(dipFileListBuilder);
        }
      }
    });

    // Logs
    // Check if user has permissions to see the logs'
    if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_FIND_LOG_ENTRY)) {
      createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.auditLogsTab()), new TabContentBuilder() {
        @Override
        public Widget buildTabWidget() {
          SearchWrapper auditLogs = new SearchWrapper(false);
          auditLogs.createListAndSearchPanel(new ListBuilder<>(() -> new LogEntryList(),
            new AsyncTableCellOptions<>(LogEntry.class, "BrowseDIP_auditLogs")
              .withFilter(new Filter(new SimpleFilterParameter(RodaConstants.LOG_RELATED_OBJECT_ID, dip.getId())))
              .bindOpener()));
          return auditLogs;
        }
      });
    }

    // Permissions
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.permissionsTab()), new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        DisseminationActions dipToolbarActions = DisseminationActions.get(dip.getPermissions());
        return new EditPermissionsTab(new ActionableWidgetBuilder<>(dipToolbarActions).buildGroupedListWithObjects(
          new ActionableObject<>(dip), List.of(DisseminationActions.DisseminationAction.UPDATE_PERMISSIONS),
          List.of(DisseminationActions.DisseminationAction.UPDATE_PERMISSIONS)), IndexedDIP.class.getName(), dip);
      }
    });
  }
}