package org.roda.wui.client.common;

import org.roda.wui.client.common.utils.JavascriptUtils;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Carlos Afonso <cafonso@keep.pt>
 */
public class UpSalePanel extends FlowPanel {

  private HTML icon;

  private final Label title;

  private Label description;

  private Anchor learnMore;

  private Button learnMoreButton;
  private final Button close;

  public UpSalePanel(String title, String learnMoreText, String learnMoreLink, String cardIdentifier) {
    super();

    this.setStyleName("ingest-tab-panel");
    this.title = new Label(title);
    this.title.addStyleName("ingest-tab-panel-label");

    this.icon = new HTML();
    this.icon.setHTML("<i class=\"far fa-lightbulb\"></i>");

    this.learnMore = new Anchor(learnMoreText);
    this.learnMore.addClickHandler(e -> Window.open(learnMoreLink, "_blank", ""));

    this.close = new Button();
    this.close.setHTML("<i class=\"fas fa-times\"></i>");
    this.close.setStyleName("ingestTabPanelclose");
    this.close.addClickHandler(clickEvent -> {
      this.setVisible(false);
      JavascriptUtils.setLocalStorage(cardIdentifier, false);
    });

    add(this.icon);
    add(this.title);
    add(this.learnMore);
    add(this.close);
  }

  public UpSalePanel(String title, String description, String learnMoreText, String learnMoreLink,
    String cardIdentifier) {
    super();

    this.setStyleName("collapsable-card-panel");
    this.title = new Label(title);
    this.title.addStyleName("h5");

    this.description = new Label(description);

    this.learnMoreButton = new Button(learnMoreText);
    this.learnMoreButton.addStyleName("btn collapsable-card-panel-action");
    this.learnMoreButton.addClickHandler(e -> Window.open(learnMoreLink, "_blank", ""));

    this.close = new Button();
    this.close.setHTML("<i class=\"fas fa-times\"></i>");
    this.close.addStyleName("collapsable-card-panel-close");
    this.close.addClickHandler(clickEvent -> {
      this.setVisible(false);
      JavascriptUtils.setLocalStorage(cardIdentifier, false);
    });

    add(this.title);
    add(this.description);
    add(this.learnMoreButton);
    add(this.close);
  }

}
