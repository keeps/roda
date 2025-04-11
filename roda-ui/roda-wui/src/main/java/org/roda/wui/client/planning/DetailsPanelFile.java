package org.roda.wui.client.planning;

import java.util.List;
import java.util.Map;

import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.wui.client.common.slider.InfoSliderHelper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Carlos Afonso <cafonso@keep.pt>
 */
public class DetailsPanelFile extends Composite {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static DetailsPanelFile.MyUiBinder uiBinder = GWT.create(DetailsPanelFile.MyUiBinder.class);
  @UiField
  FlowPanel details;

  public DetailsPanelFile(IndexedFile file, List<String> riRules) {
    initWidget(uiBinder.createAndBindUi(this));
    init(file, riRules);
  }

  public void init(IndexedFile file, List<String> riRules) {
    GWT.log("DetailsPanel init");

    Map<String, Widget> detailsMap = InfoSliderHelper.getFileInfoDetailsMap(file, riRules);

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
      details.add(fieldPanel);
    }
  }

  public void clear() {
    details.clear();
  }

  interface MyUiBinder extends UiBinder<Widget, DetailsPanelFile> {
    Widget createAndBindUi(DetailsPanelFile detailsPanel);
  }

}
