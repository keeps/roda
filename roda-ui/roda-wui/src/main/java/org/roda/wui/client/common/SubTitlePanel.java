package org.roda.wui.client.common;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import org.roda.core.data.common.RodaConstants;
import org.roda.wui.common.client.tools.ConfigurationManager;
import org.roda.wui.common.client.tools.StringUtils;

/**
 * @author Miguel Guimarãese <mguimaraes@keep.pt>
 */
public class SubTitlePanel extends FlowPanel {
  private HTML iconHTML;
  private Label textLabel;

  public Label getLabel() {
    return textLabel;
  }

  public SubTitlePanel() {
    super();
    this.addStyleName("browseItemPanel");

    iconHTML = new HTML();
    iconHTML.addStyleName("h4 browseItemIcon");
    add(iconHTML);
    iconHTML.setVisible(false);

    textLabel = new Label();
    textLabel.addStyleName("h4 browseItemText");
    add(textLabel);
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

  public void reset() {
    iconHTML.setHTML("");
    textLabel.setText("");
  }
}
