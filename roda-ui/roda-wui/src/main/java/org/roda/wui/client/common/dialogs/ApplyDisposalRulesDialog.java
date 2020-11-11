package org.roda.wui.client.common.dialogs;

import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

import config.i18n.client.ClientMessages;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */
public class ApplyDisposalRulesDialog {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public ApplyDisposalRulesDialog(String title) {
      FlowPanel layout = new FlowPanel();
      FlowPanel applyToRepositoryPanel = new FlowPanel();
      FlowPanel applyToIngestRulesPanel = new FlowPanel();
      FlowPanel footer = new FlowPanel();
      final DialogBox dialogBox = new DialogBox(false, true);
      dialogBox.setText(title);

      layout.addStyleName("content");
      layout.addStyleName("wui-dialog-layout");
      footer.addStyleName("wui-dialog-layout-footer");

      Button cancelButton = new Button(messages.cancelButton());
      cancelButton.addStyleName("btn btn-link");
      cancelButton.addClickHandler(event -> {
          dialogBox.hide();
      });

      Label label = new Label();
      label.setText(messages.applyRulesToRepository());
      label.addStyleName("h5");

      HTMLPanel info = new HTMLPanel("");
      info.add(new HTMLWidgetWrapper("ApplyRulesToRepository.html"));
      info.addStyleName("page-description");

      Button apply = new Button();
      apply.setText(messages.applyToRepositoryButton());
      apply.addStyleName("btn btn-danger btn-play");

      applyToRepositoryPanel.add(label);
      applyToRepositoryPanel.add(info);
      applyToRepositoryPanel.add(apply);

      Label labelIngest = new Label();
      labelIngest.setText(messages.applyRulesToIngest());
      labelIngest.addStyleName("h5");

      HTMLPanel infoIngest = new HTMLPanel("");
      infoIngest.add(new HTMLWidgetWrapper("ApplyRulesToIngest.html"));
      infoIngest.addStyleName("page-description");

      Button applyIngest = new Button();
      applyIngest.setText(messages.applyButton());
      applyIngest.addStyleName("btn btn-play");

      applyToIngestRulesPanel.add(labelIngest);
      applyToIngestRulesPanel.add(infoIngest);
      applyToIngestRulesPanel.add(applyIngest);

      layout.add(applyToRepositoryPanel);
      layout.add(applyToIngestRulesPanel);
      footer.add(cancelButton);
      layout.add(footer);

      dialogBox.setGlassEnabled(true);
      dialogBox.setAnimationEnabled(false);

      dialogBox.setWidget(layout);
      dialogBox.center();
      dialogBox.show();
  }

}
