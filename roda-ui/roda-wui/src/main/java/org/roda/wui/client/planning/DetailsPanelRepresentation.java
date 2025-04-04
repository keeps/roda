package org.roda.wui.client.planning;

import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.common.client.tools.Humanize;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Carlos Afonso <cafonso@keep.pt>
 */
public class DetailsPanelRepresentation extends Composite {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static DetailsPanelRepresentation.MyUiBinder uiBinder = GWT
    .create(DetailsPanelRepresentation.MyUiBinder.class);

  @UiField
  Label repID;

  @UiField
  Label repType;

  @UiField
  Label repCreatedOn;

  @UiField
  Label repCreatedBy;

  @UiField
  Label modifiedOn;

  @UiField
  Label modifiedBy;

  public DetailsPanelRepresentation(IndexedAIP aip, IndexedRepresentation representation) {
    initWidget(uiBinder.createAndBindUi(this));
    init(aip, representation);
  }

  public void init(IndexedAIP aip, IndexedRepresentation representation) {
    GWT.log("DetailsPanel init");

    repID.setText(representation.getId());
    repType.setText(representation.getType());

    repCreatedOn.setText(Humanize.formatDateTime(representation.getCreatedOn()));
    repCreatedBy.setText(representation.getCreatedBy());

    modifiedOn.setText(Humanize.formatDateTime(representation.getUpdatedOn()));
    modifiedBy.setText(representation.getUpdatedBy());

  }

  public void clear() {
    repID.setText("");
    repType.setText("");
    repCreatedOn.setText("");
    repCreatedBy.setText("");
    modifiedOn.setText("");
    modifiedBy.setText("");
  }

  interface MyUiBinder extends UiBinder<Widget, DetailsPanelRepresentation> {
    Widget createAndBindUi(DetailsPanelRepresentation detailsPanel);
  }
}
