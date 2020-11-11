package org.roda.wui.client.common;

import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalPolicySummaryPanel extends FlowPanel {
  private HTML iconHTML;
  private Label textLabel;

  public DisposalPolicySummaryPanel() {
    super();
    this.addStyleName("disposal-policy-summary");

    iconHTML = new HTML();
    iconHTML.addStyleName("h5 disposal-policy-summary-icon");
    add(iconHTML);
    iconHTML.setVisible(false);

    textLabel = new Label();
    textLabel.addStyleName("h5 disposal-policy-summary-text");
    add(textLabel);
  }

  public void setIcon(String iconCss) {
    // set default if empty
    if (StringUtils.isBlank(iconCss)) {
      iconCss = "fa fa-question-circle";
    }

    setIcon(SafeHtmlUtils.fromSafeConstant("<i class=\"" + iconCss + "\"></i>"));
  }

  public void setIcon(SafeHtml iconSafeHtml) {
    iconHTML.setHTML(iconSafeHtml);
    iconHTML.setVisible(true);
  }

  public void setText(String text) {
    textLabel.setText(text);
  }

  public void reset() {
    iconHTML.setHTML("");
    textLabel.setText("");
  }
}
