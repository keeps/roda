package org.roda.wui.client.browse.tabs;

import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.client.browse.IndexedFilePreview;
import org.roda.wui.client.browse.Viewers;
import org.roda.wui.client.common.model.BrowseFileResponse;
import org.roda.wui.client.services.Services;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class BrowseFileTabs extends Tabs {
  public void init(Viewers viewers, IndexedFile file, BrowseFileResponse browseFileResponse, Services services) {
    IndexedAIP aip = browseFileResponse.getIndexedAIP();
    IndexedRepresentation representation = browseFileResponse.getIndexedRepresentation();

    boolean justActive = AIPState.ACTIVE.equals(aip.getState());

    // PREVIEW
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.file()), new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        return new IndexedFilePreview(viewers, file, file.isAvailable(), justActive,
          browseFileResponse.getIndexedAIP().getPermissions(), () -> {
          });
      }
    });
  }
}