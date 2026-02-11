package org.roda.wui.client.common.panels;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;
import org.roda.wui.client.common.labels.Header;

import java.util.concurrent.Flow;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class PermissionsPanel extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  FlowPanel permissionsDescription;

  @UiField
  Header permissionsTitle;

  @UiField
  Label permissionsEmpty;

  @UiField
  FlowPanel permissionsPanel;



  interface MyUiBinder extends UiBinder<Widget, PermissionsPanel> {
  }
}
