package org.roda.wui.client.browse.tabs;

import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.client.common.ActionsToolbar;
import org.roda.wui.client.planning.DetailsPanelAIP;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.roda.wui.client.planning.DetailsPanelFile;
import org.roda.wui.client.planning.DetailsPanelRepresentation;

/**
 * @author Carlos Afonso <cafonso@keep.pt>
 */
public class DetailsTab extends Tabs {
  public void initAipDetails(IndexedAIP aip) {
      // Tab button
      SafeHtml buttonTitle = SafeHtmlUtils.fromString(messages.detailsTab());
      // Content container
      FlowPanel content = new FlowPanel();
      content.addStyleName("descriptiveMetadataTabContainer roda6CardWithHeader");
      // Create Toolbar
      ActionsToolbar descriptiveMetadataToolbar = new ActionsToolbar();
      descriptiveMetadataToolbar.setLabelVisible(false);
      descriptiveMetadataToolbar.setTagsVisible(false);
      // Get metadata and populate widget
      FlowPanel cardBody = new FlowPanel();
      content.add(cardBody);
      cardBody.setStyleName("cardBody");
      DetailsPanelAIP detailsPanel = new DetailsPanelAIP(aip);


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

  public void initRepresentationDetails(IndexedAIP aip, IndexedRepresentation rep) {
    // Tab button
    SafeHtml buttonTitle = SafeHtmlUtils.fromString(messages.detailsTab());
    // Content container
    FlowPanel content = new FlowPanel();
    content.addStyleName("descriptiveMetadataTabContainer roda6CardWithHeader");
    // Create Toolbar
    ActionsToolbar descriptiveMetadataToolbar = new ActionsToolbar();
    descriptiveMetadataToolbar.setLabelVisible(false);
    descriptiveMetadataToolbar.setTagsVisible(false);
    // Get metadata and populate widget
    FlowPanel cardBody = new FlowPanel();
    content.add(cardBody);
    cardBody.setStyleName("cardBody");
    DetailsPanelRepresentation detailsPanel = new DetailsPanelRepresentation(aip, rep);


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

  public void initFileDetails(IndexedAIP aip, IndexedRepresentation rep, IndexedFile file) {
    // Tab button
    SafeHtml buttonTitle = SafeHtmlUtils.fromString(messages.detailsTab());
    // Content container
    FlowPanel content = new FlowPanel();
    content.addStyleName("descriptiveMetadataTabContainer roda6CardWithHeader");
    // Create Toolbar
    ActionsToolbar descriptiveMetadataToolbar = new ActionsToolbar();
    descriptiveMetadataToolbar.setLabelVisible(false);
    descriptiveMetadataToolbar.setTagsVisible(false);
    // Get metadata and populate widget
    FlowPanel cardBody = new FlowPanel();
    content.add(cardBody);
    cardBody.setStyleName("cardBody");
    DetailsPanelFile detailsPanel = new DetailsPanelFile(aip, rep, file);


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
}
