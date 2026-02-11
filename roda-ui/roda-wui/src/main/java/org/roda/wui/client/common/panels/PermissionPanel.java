package org.roda.wui.client.common.panels;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;

import java.util.Set;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class PermissionPanel extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private final Set<String> list;
  private final boolean isUser;

  // Private constructor forces the use of the Builder
  private PermissionPanel(Builder builder) {
    this.list = builder.list;
    this.isUser = builder.isUser;

    FlowPanel superPanel = new FlowPanel();
    initWidget(superPanel);

    for (String str : list) {
      FlowPanel panel = new FlowPanel();

      FlowPanel panelBody = new FlowPanel();
      HTML type = new HTML(
        SafeHtmlUtils.fromSafeConstant(isUser ? "<i class='fa fa-users'></i>" : "<i class='fa fa-user'></i>"));
      panelBody.add(type);
      type.addStyleName("permission-type");
      Label label = new Label(str);
      panelBody.add(label);
      panel.add(panelBody);
      panelBody.addStyleName("panel-body");
      label.addStyleName("permission-name");

      panel.addStyleName("panel permission");
      panel.addStyleName(isUser ? "permission-group" : "permission-user");

      superPanel.add(panel);
    }
  }

  public Set<String> getList() {
    return list;
  }

  public boolean isUser() {
    return isUser;
  }

  // --- BUILDER IMPLEMENTATION ---

  public static class Builder {
    private final Set<String> list;
    private final boolean isUser;

    public Builder(RODAMember member) {
      this.isUser = member.isUser();
      if (this.isUser) {
        list = ((User) member).getGroups();
      } else {
        list = ((Group) member).getUsers();
      }
    }

    public PermissionPanel build() {
      return new PermissionPanel(this);
    }
  }
}