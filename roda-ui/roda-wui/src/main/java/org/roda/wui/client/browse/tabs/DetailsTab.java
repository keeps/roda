/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse.tabs;

import org.roda.core.data.v2.risks.IndexedRisk;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;
import org.roda.wui.client.planning.DetailsPanelRisk;

/**
 * @author Carlos Afonso <cafonso@keep.pt>
 */
public class DetailsTab extends Composite {
  protected static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static DetailsTab.MyUiBinder uiBinder = GWT.create(DetailsTab.MyUiBinder.class);

  @UiField
  FlowPanel content;

  public DetailsTab(IndexedRisk risk) {
    initWidget(uiBinder.createAndBindUi(this));

    DetailsPanelRisk detailsPanel = new DetailsPanelRisk(risk, "RiskShowPanel_riskIncidences");
    content.add(detailsPanel);
  }

  interface MyUiBinder extends UiBinder<Widget, DetailsTab> {
  }
}
