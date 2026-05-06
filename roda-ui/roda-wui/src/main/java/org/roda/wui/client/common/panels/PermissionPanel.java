package org.roda.wui.client.common.panels;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.wui.client.common.labels.Tag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PermissionPanel extends Composite {
    private static final ClientMessages messages = GWT.create(ClientMessages.class);
    
    private FlowPanel panel;
    private FlowPanel panelBody;
    private FlowPanel rightPanel;
    private HTML type;
    private Label nameLabel;
    private FlowPanel permissionTagsPanel;

    private String name;
    private boolean isUser;

    public PermissionPanel(String name, boolean isUser, Set<Permissions.PermissionType> permissions) {
        this.name = name;
        this.isUser = isUser;

        panel = new FlowPanel();
        panelBody = new FlowPanel();

        type = new HTML(
                SafeHtmlUtils.fromSafeConstant(isUser ? "<i class='fa fa-user'></i>" : "<i class='fa fa-users'></i>"));
        nameLabel = new Label(name);

        rightPanel = new FlowPanel();
        permissionTagsPanel = new FlowPanel();

        Map<Permissions.PermissionType, Tag.TagStyle> tagStyles = new HashMap<>();
        tagStyles.put(Permissions.PermissionType.GRANT, Tag.TagStyle.BORDER_BLACK);
        tagStyles.put(Permissions.PermissionType.READ, Tag.TagStyle.BORDER_BLACK);
        tagStyles.put(Permissions.PermissionType.DELETE, Tag.TagStyle.BORDER_DANGER);
        tagStyles.put(Permissions.PermissionType.CREATE, Tag.TagStyle.BORDER_BLACK);
        tagStyles.put(Permissions.PermissionType.UPDATE, Tag.TagStyle.BORDER_BLACK);
        for (Permissions.PermissionType permissionType : permissions) {
            Tag permissionTag = Tag.fromText(messages.objectPermission(permissionType), tagStyles.get(permissionType));
            permissionTagsPanel.add(permissionTag);
            permissionTag.addStyleName("permission-tag");
        }

        panelBody.add(type);
        panelBody.add(nameLabel);
        panelBody.add(rightPanel);

        rightPanel.add(permissionTagsPanel);

        panel.add(panelBody);

        initWidget(panel);

        panel.addStyleName("panel permission");
        panel.addStyleName(isUser ? "permission-user" : "permission-group");
        panelBody.addStyleName("panel-body");
        type.addStyleName("permission-type");
        nameLabel.addStyleName("permission-name");
        rightPanel.addStyleName("pull-right");
        permissionTagsPanel.addStyleName("permission-tags");
    }

    public Set<Permissions.PermissionType> getPermissions() {
        HashSet<Permissions.PermissionType> permissions = new HashSet<>();
        for (int i = 0; i < permissionTagsPanel.getWidgetCount(); i++) {
            ValueLabel valueCheckBox = (ValueLabel) permissionTagsPanel.getWidget(i);
            permissions.add(valueCheckBox.getPermissionType());
        }
        return permissions;
    }

    public String getName() {
        return name;
    }

    public boolean isUser() {
        return isUser;
    }

    public class ValueLabel extends Label {
        private Permissions.PermissionType permissionType;

        public ValueLabel(Permissions.PermissionType permissionType) {
            super(messages.objectPermission(permissionType));
            this.permissionType = permissionType;
        }

        public Permissions.PermissionType getPermissionType() {
            return permissionType;
        }
    }
}