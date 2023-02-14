package org.roda.wui.client.common;

import org.roda.core.data.common.RodaConstants;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.common.client.tools.ConfigurationManager;
import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class BadgePanel extends FlowPanel {
  private HTML iconHTML;

  private HTML notificationHTML;

  private Label textLabel;

  public BadgePanel() {
    super();
    this.addStyleName("badge-panel");

    iconHTML = new HTML();
    iconHTML.addStyleName("badge-icon");
    add(iconHTML);
    iconHTML.setVisible(false);

    textLabel = new Label();
    textLabel.addStyleName("badge-label");
    add(textLabel);

    notificationHTML = new HTML(HtmlSnippetUtils.getStackIcon("fas fa-sync-alt", "fas fa-question"));
    notificationHTML.addStyleName("badge-notification");
    notificationHTML.setVisible(false);
    add(notificationHTML);
  }

  public void setIconClass(String classSimpleName) {
    setIcon(ConfigurationManager.getString(RodaConstants.UI_ICONS_CLASS, classSimpleName));
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

  @Override
  public void addStyleName(String style) {
    super.addStyleName(style);
  }

  public void enableNotification(boolean value){
    notificationHTML.setVisible(value);
  }
}
