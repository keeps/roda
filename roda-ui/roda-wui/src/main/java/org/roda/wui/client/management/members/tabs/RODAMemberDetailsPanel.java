package org.roda.wui.client.management.members.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;

import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.generics.MetadataValue;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.common.actions.RODAMemberAction;
import org.roda.wui.client.common.actions.RODAMemberToolbarActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.panels.GenericMetadataCardPanel;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.common.client.tools.StringUtils;

import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class RODAMemberDetailsPanel extends GenericMetadataCardPanel<RODAMember> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public RODAMemberDetailsPanel(RODAMember member) {
    super(createConfiguredToolbar(member));
    setData(member);
  }

  private static FlowPanel createConfiguredToolbar(RODAMember member) {
    if (member == null) {
      return null;
    }

    return new ActionableWidgetBuilder<RODAMember>(RODAMemberToolbarActions.get()).buildGroupedListWithObjects(
      new ActionableObject<>(member), List.of(RODAMemberAction.EDIT, RODAMemberAction.CHANGE_PASSWORD),
      List.of(RODAMemberAction.EDIT, RODAMemberAction.CHANGE_PASSWORD));
  }

  @Override
  public void setData(RODAMember data) {
    // 1. Clear any existing fields in case setData is called multiple times
    metadataContainer.clear();

    if (data == null) {
      return;
    }

    if (data.isUser()) {
      User user = (User) data;
      addFieldIfNotNull(messages.username(), RODAMember::getId, data);
      addFieldIfNotNull(messages.fullname(), RODAMember::getFullName, data);
      addFieldIfNotNull(messages.email(), user.getEmail());

      if (!user.getExtra().isEmpty()) {
        String pendingSeparatorLabel = null;

        for (MetadataValue extra : user.getExtra()) {
          if ("separator".equals(extra.getOptions().get("type"))) {
            // Store the separator label but do not render it yet
            pendingSeparatorLabel = HtmlSnippetUtils.getMetadataValueLabel(extra);
          } else {
            String value = extra.get("value");
            // Check if this field actually has a value
            if (StringUtils.isNotBlank(value)) {

              // If we have a pending separator, render it NOW before the field
              if (pendingSeparatorLabel != null) {
                addSeparator(pendingSeparatorLabel);
                pendingSeparatorLabel = null; // Clear it so it doesn't render again
              }

              addFieldIfNotNull(HtmlSnippetUtils.getMetadataValueLabel(extra), value);
            }
          }
        }
      }
      addFieldIfNotNull(messages.showUserStatusLabel(), HtmlSnippetUtils.getUserStateHtml(user));

    } else {
      addFieldIfNotNull(messages.username(), RODAMember::getId, data);
      addFieldIfNotNull(messages.fullname(), RODAMember::getFullName, data);
    }
  }
}
