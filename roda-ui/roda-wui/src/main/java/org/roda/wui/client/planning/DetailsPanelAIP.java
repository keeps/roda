/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */

package org.roda.wui.client.planning;

import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.wui.client.common.model.BrowseAIPResponse;
import org.roda.wui.client.common.slider.InfoSliderHelper;
import org.roda.wui.client.common.utils.PermissionClientUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class DetailsPanelAIP extends Composite {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  @UiField
  FlowPanel aipDetails;
  @UiField
  Label ingestLabel;
  @UiField
  FlowPanel ingestDetails;

  public DetailsPanelAIP(BrowseAIPResponse response) {
    initWidget(uiBinder.createAndBindUi(this));
    init(response);
  }

  public void init(BrowseAIPResponse response) {
    ingestLabel.setVisible(false);
    Map<String, Widget> detailsMap = InfoSliderHelper.getAipInfoDetailsMap(response);

    boolean canAccessJobs = PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_CREATE_JOB);

    for (Map.Entry<String, Widget> field : detailsMap.entrySet()) {
      String fieldLabelString = field.getKey();
      Widget fieldValueWidget = field.getValue();

      FlowPanel fieldPanel = new FlowPanel();
      fieldPanel.setStyleName("field");

      Label fieldLabel = new Label(fieldLabelString);
      fieldLabel.setStyleName("label");

      FlowPanel fieldValuePanel = new FlowPanel();
      fieldValuePanel.setStyleName("value");
      fieldValuePanel.add(fieldValueWidget);

      fieldPanel.add(fieldLabel);
      fieldPanel.add(fieldValuePanel);

      if ((fieldLabelString.equals(messages.updateProcessIdTitle())
        || fieldLabelString.equals(messages.processIdTitle()) || fieldLabelString.equals(messages.sipId()))
        && canAccessJobs) {
        ingestDetails.add(fieldPanel);
        ingestLabel.setVisible(true);
      } else {
        aipDetails.add(fieldPanel);
      }
    }
  }

  public void clear() {
    aipDetails.clear();
    ingestDetails.clear();
  }

  interface MyUiBinder extends UiBinder<Widget, DetailsPanelAIP> {
    Widget createAndBindUi(DetailsPanelAIP detailsPanel);
  }
}
