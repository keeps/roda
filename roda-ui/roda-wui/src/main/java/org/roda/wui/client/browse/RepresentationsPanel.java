/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse;

import java.util.List;

import org.roda.core.data.v2.ip.IndexedRepresentation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class RepresentationsPanel extends Composite {

  interface MyUiBinder extends UiBinder<Widget, RepresentationsPanel> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  FlowPanel representationsPanel;

  public RepresentationsPanel() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  public void createRepresentationsPanel(String aipId, BrowseItemBundle itemBundle) {
    representationsPanel.clear();

    List<IndexedRepresentation> representations = itemBundle.getRepresentations();

    for (IndexedRepresentation representation : representations) {
      representationsPanel.add(new RepresentationPanel(aipId, representation,
        itemBundle.getRepresentationsDescriptiveMetadata().get(representation.getUUID())));
    }
  }
}
