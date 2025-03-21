package org.roda.wui.client.browse.tabs;

import com.google.gwt.core.client.GWT;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataInfo;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataInfos;
import org.roda.wui.client.browse.BrowseTop;
import org.roda.wui.client.browse.CreateDescriptiveMetadata;
import org.roda.wui.client.common.ActionsToolbar;
import org.roda.wui.client.planning.DetailsPanel;
import org.roda.wui.common.client.tools.HistoryUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Carlos Afonso <cafonso@keep.pt>
 */
public class DetailsTab extends Tabs {
  public void init(IndexedAIP aip, DescriptiveMetadataInfos descriptiveMetadataInfos) {
    for (DescriptiveMetadataInfo metadataInfo : descriptiveMetadataInfos.getDescriptiveMetadataInfoList()) {
      // Tab button
      SafeHtml buttonTitle = SafeHtmlUtils.fromString(messages.detailsTab());
      // Content container
      FlowPanel content = new FlowPanel();
      content.addStyleName("descriptiveMetadataTabContainer roda6CardWithHeader");
      // Create Toolbar
      String metadataID = SafeHtmlUtils.htmlEscape(metadataInfo.getId());
      ActionsToolbar descriptiveMetadataToolbar = new ActionsToolbar();
      descriptiveMetadataToolbar.setLabelVisible(false);
      descriptiveMetadataToolbar.setTagsVisible(false);
      // Get metadata and populate widget
      FlowPanel cardBody = new FlowPanel();
      content.add(cardBody);
      cardBody.setStyleName("cardBody");
      DetailsPanel detailsPanel = new DetailsPanel(aip);


      cardBody.add(detailsPanel);
      // Create and add tab
      // This descriptive metadata content is NOT lazy loading!
      createAndAddTab(buttonTitle, new TabContentBuilder() {
        @Override
        public Widget buildTabWidget() {
          return content;
        }
      });
    }
    // Create button
    Button createButton = new Button(messages.newButton());
    createButton.setStyleName("linkButton btn-plus");
    createButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        HistoryUtils.newHistory(BrowseTop.RESOLVER, CreateDescriptiveMetadata.RESOLVER.getHistoryToken(),
          RodaConstants.RODA_OBJECT_AIP, aip.getId());
      }
    });
    addStaticElement(createButton);
  }
}
