package org.roda.wui.client.management.members.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.wui.client.management.members.MemberManagement;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.ConfigurationManager;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.LoadingPopup;
import org.roda.wui.common.client.widgets.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class PermissionsPanel extends FlowPanel implements HasValueChangeHandlers<List<String>> {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @SuppressWarnings("unused")
  private final ClientLogger logger = new ClientLogger(getClass().getName());

  private final List<Permission> allPermissions;
  private final Map<String, CategoryPanel> categoryPanels;
  private final LoadingPopup loading;
  private final FlowPanel categoriesContainer;
  private final boolean viewOnly;
  private final boolean expanded;
  private List<String> userSelections;
  private boolean enabled;
  // New instance fields to support refreshing
  private RODAMember member;
  private Runnable onDataLoadedCallback;

  public PermissionsPanel(RODAMember member, boolean viewOnly, boolean expanded) {
    this.allPermissions = new ArrayList<>();
    this.userSelections = new ArrayList<>();
    this.categoryPanels = new LinkedHashMap<>();

    this.member = member;
    this.viewOnly = viewOnly;
    this.expanded = expanded;

    loading = new LoadingPopup(this);

    this.enabled = true;
    this.addStyleName("permissions-dashboard");

    // 2. Container for Categories
    categoriesContainer = new FlowPanel();
    categoriesContainer.setStyleName("permissions-categories");
    this.add(categoriesContainer);

    refresh(); // Trigger initial load
  }

  public void refresh() {
    loading.show();

    // Clear old elements and state before rebuilding
    categoriesContainer.clear();
    allPermissions.clear();
    categoryPanels.clear();
    userSelections.clear();

    // 1. Fetch the absolute latest member data (to get freshly edited roles)
    Services userServices = new Services("Get updated member", "get");
    userServices.membersResource(s -> s.getMember(member.getUUID())).whenComplete(this::handleMemberRefresh);
  }

  private void handleMemberRefresh(RODAMember updatedMember, Throwable error) {
    if (error != null) {
      loading.hide();
      Toast.showError("Unable to fetch updated user data");
      HistoryUtils.newHistory(MemberManagement.RESOLVER.getHistoryPath());
      return;
    }

    // Update local reference so init() uses the new roles
    this.member = updatedMember;

    // 2. Fetch the groups
    if (member.isUser()) {
      Services groupServices = new Services("Fetch user's group", "get");
      groupServices.membersResource(s -> s.getUserGroups(member.getId())).whenComplete((groups, throwable) -> {
        if (throwable != null) {
          loading.hide();
          Toast.showError("Unable to fetch user's groups");
          ClientLogger clientLogger = new ClientLogger();
          clientLogger.error("Error fetching user's groups", throwable);
          HistoryUtils.newHistory(MemberManagement.RESOLVER.getHistoryPath());
        } else {
          init(member, groups, viewOnly, expanded);
        }
      });
    } else {
      // If it's a group, we can skip directly to init()
      init(member, Set.of(), viewOnly, expanded);
    }
  }

  public void init(RODAMember member, Set<Group> groups, boolean viewOnly, boolean expanded) {
    if (!viewOnly) {
      userSelections = new ArrayList<>(member.getDirectRoles());
    }

    List<String> roles = ConfigurationManager.getStringList("ui.role");

    for (String role : roles) {
      String description;
      try {
        description = messages.role(role);
      } catch (MissingResourceException e) {
        description = role + " (needs translation)";
      }

      Permission permission = new Permission(role, description);
      permission.setChecked(member.getAllRoles().contains(role));
      permission.setEnabled(!viewOnly);
      permission.setLocked(viewOnly);
      allPermissions.add(permission);

      permission.addValueChangeHandler(new ValueChangeHandler<String>() {
        @Override
        public void onValueChange(ValueChangeEvent<String> event) {
          if (userSelections.contains(event.getValue())) {
            userSelections.remove(event.getValue());
          } else {
            userSelections.add(event.getValue());
          }
          onChange();
        }
      });

      // Grouping logic
      String categoryName = determineCategory(role);
      CategoryPanel categoryPanel = categoryPanels.get(categoryName);
      if (categoryPanel == null) {
        categoryPanel = new CategoryPanel(categoryName, viewOnly, expanded);
        categoryPanels.put(categoryName, categoryPanel);
        categoriesContainer.add(categoryPanel);
      }
      categoryPanel.addPermission(permission);
    }

    if (!viewOnly) {
      checkPermissions(member.getDirectRoles(), groups, true, "Permission locked because it's inherited from a group");
    }

    loading.hide();

    if (onDataLoadedCallback != null) {
      onDataLoadedCallback.run();
    }
  }

  // --- The rest of your existing logic remains untouched ---

  private String determineCategory(String role) {
    String lowerRole = role.toLowerCase();
    if (lowerRole.startsWith("aip") || (lowerRole.startsWith("representation"))
      || lowerRole.startsWith("descriptive_metadata") || lowerRole.startsWith("preservation_metadata")) {
      return messages.catalogueAndSearchGroupLabel();
    }

    if (lowerRole.startsWith("transfer") || lowerRole.startsWith("job")) {
      return messages.ingestPreservationActionsInternalActionsGroupLabel();
    }

    if (lowerRole.startsWith("disposal_")) {
      return messages.disposalGroupLabel();
    }

    if (lowerRole.startsWith("member") || lowerRole.startsWith("access_key") || lowerRole.startsWith("permission")
      || lowerRole.startsWith("notification") || lowerRole.startsWith("log_entry")
      || lowerRole.startsWith("distributed_instances") || lowerRole.startsWith("local_instance_configuration")) {
      return messages.administrationGroupLabel();
    }

    if (lowerRole.startsWith("ri") || lowerRole.startsWith("risk")) {
      return messages.planningGroupLabel();
    }

    return "Undefined";
  }

  public List<String> getUserSelections() {
    for (Permission permission : allPermissions) {
      if (permission.isChecked() && !permission.isLocked() && permission.isEnabled()) {
        if (!userSelections.contains(permission.getRole())) {
          userSelections.add(permission.getRole());
        }
      } else {
        userSelections.remove(permission.getRole());
      }
    }

    return userSelections;
  }

  public void checkPermissions(Set<String> directRoles, Set<Group> groups, boolean lock, String description) {
    Set<String> allRoles = groups.stream().flatMap(group -> group.getAllRoles().stream()).collect(Collectors.toSet());

    if (allRoles.containsAll(directRoles)) {
      checkPermissions(allRoles, true, description);
    } else {
      checkPermissions(directRoles, false);
      checkPermissions(allRoles, true, description);
    }
  }

  public void checkPermissions(Set<String> roles, boolean lock) {
    checkPermissions(roles, lock, null);
  }

  private void checkPermissions(Set<String> roles, boolean lock, String description) {
    for (String role : roles) {
      boolean foundIt = false;
      for (Iterator<Permission> j = allPermissions.iterator(); j.hasNext() && !foundIt;) {
        Permission p = j.next();
        if (p.getRole().equals(role)) {
          foundIt = true;
          p.setChecked(true);
          if (description != null) {
            p.setTooltip(description);
          }
          p.setLocked(lock);
        }
      }
    }
  }

  @Override
  public void clear() {
    for (Permission p : allPermissions) {
      p.setChecked(false);
      p.setLocked(false);
    }
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
    for (Permission p : allPermissions) {
      p.setEnabled(enabled);
    }
  }

  public void updateLockedPermissions(Set<String> memberGroups) {
    if (!memberGroups.isEmpty()) {
      this.setEnabled(false);
      loading.show();
    }
  }

  public Set<String> getDirectRoles() {
    List<Permission> checkedPermissions = new ArrayList<>();
    for (Permission p : allPermissions) {
      if (p.isChecked() && !p.isLocked()) {
        checkedPermissions.add(p);
      }
    }

    Set<String> specialRoles = new HashSet<>();
    for (Permission checkedPermission : checkedPermissions) {
      specialRoles.add(checkedPermission.getRole());
    }
    return specialRoles;
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<String>> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  protected void onChange() {
    ValueChangeEvent.fire(this, getValue());
  }

  public List<String> getValue() {
    return getUserSelections();
  }

  public void setOnDataLoadedCallback(Runnable callback) {
    this.onDataLoadedCallback = callback;
  }

  // --- INNER CLASSES ---
  private class CategoryPanel extends FlowPanel {
    private final FlowPanel itemsContainer;
    private final List<Permission> permissionsInCat = new ArrayList<>();
    private final CheckBox selectAllBox;
    private final CheckBox readOnlyPermissions;
    private final Label iconLabel;
    private boolean isExpanded = false;

    public CategoryPanel(String titleText, boolean viewOnly, boolean expanded) {
      this.isExpanded = expanded;
      this.setStyleName("permission-category");
      // --- HEADER ---
      FlowPanel header = new FlowPanel();
      header.setStyleName("permission-category-header");

      // Title on the Left
      Label title = new Label(titleText);
      title.setStyleName("permission-category-title");

      // Right side wrapper: Select All + Arrow
      FlowPanel headerRight = new FlowPanel();
      headerRight.setStyleName("permission-category-header-right");

      // Select All Checkbox (Passing the label text directly to the checkbox)
      selectAllBox = new CheckBox(messages.selectAllButton());
      selectAllBox.setStyleName("permission-category-checkbox");

      // Prevent header expand/collapse when interacting with the checkbox
      selectAllBox.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          event.stopPropagation();
        }
      });

      selectAllBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
        @Override
        public void onValueChange(ValueChangeEvent<Boolean> event) {
          readOnlyPermissions.setValue(false);
          triggerSelectAll(event.getValue());
        }
      });

      readOnlyPermissions = new CheckBox(messages.editPermissionsReadOnlyPermissionsText());
      readOnlyPermissions.setStyleName("permission-category-checkbox");
      // Prevent header expand/collapse when interacting with the checkbox
      readOnlyPermissions.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          event.stopPropagation();
        }
      });

      readOnlyPermissions.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
        @Override
        public void onValueChange(ValueChangeEvent<Boolean> event) {
          selectAllBox.setValue(false);
          triggerSelectReadOnly(event.getValue());
        }
      });

      // Arrow Icon
      iconLabel = new Label();
      iconLabel.setStyleName("fas fa-chevron-down permission-category-icon");

      // Add elements to the right wrapper
      if (!viewOnly) {
        headerRight.add(readOnlyPermissions);
        headerRight.add(selectAllBox);
      }
      headerRight.add(iconLabel);

      // Add left title and right wrapper to the main flex header
      header.add(title);
      header.add(headerRight);

      // Accordion Expand/Collapse Logic
      // --- FIXED ACCORDION TOGGLE LOGIC ---
      header.addDomHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

          // FIX: Check exactly what element was clicked.
          // If it was the Select All box OR its text label, abort the toggle.
          com.google.gwt.dom.client.Element target = com.google.gwt.dom.client.Element
            .as(event.getNativeEvent().getEventTarget());

          if (selectAllBox.getElement().isOrHasChild(target) || readOnlyPermissions.getElement().isOrHasChild(target)) {
            return;
          }

          // Otherwise, toggle normally
          isExpanded = !isExpanded;
          itemsContainer.setVisible(isExpanded);
          if (isExpanded) {
            header.removeStyleName("collapsed");
            iconLabel.setStyleName("fas fa-chevron-up permission-category-icon");
          } else {
            header.addStyleName("collapsed");
            iconLabel.setStyleName("fas fa-chevron-down permission-category-icon");
          }
        }
      }, ClickEvent.getType());

      // --- ITEMS CONTAINER ---
      itemsContainer = new FlowPanel();
      itemsContainer.setStyleName("permission-category-items");

      if (isExpanded) {
        header.removeStyleName("collapsed");
        iconLabel.setStyleName("fas fa-chevron-up permission-category-icon");
        itemsContainer.setVisible(true);
      } else {
        itemsContainer.setVisible(false);
      }

      this.add(header);
      this.add(itemsContainer);
    }

    public void addPermission(Permission p) {
      permissionsInCat.add(p);
      itemsContainer.add(p);

      // Uncheck "Select All" in the header if a single item is unchecked
      p.addValueChangeHandler(new ValueChangeHandler<String>() {
        @Override
        public void onValueChange(ValueChangeEvent<String> event) {
          if (!p.isChecked()) {
            selectAllBox.setValue(false, false);
          }
        }
      });
    }

    private void triggerSelectAll(boolean isChecked) {
      for (Permission p : permissionsInCat) {
        if (p.isVisible() && p.isEnabled() && !p.isLocked()) {
          if (p.isChecked() != isChecked) {
            p.setChecked(isChecked);
            p.onChange();
          }
        }
      }
    }

    private void triggerSelectReadOnly(boolean isChecked) {
      for (Permission p : permissionsInCat) {
        if (p.isVisible() && p.isEnabled() && !p.isLocked())
          if (p.getRole().contains("read") || p.getRole().contains("view")) {
            p.setChecked(isChecked);
            p.onChange();
          } else {
            p.setChecked(false);
            p.onChange();
          }
      }
    }
  }

  private class Permission extends FlowPanel implements HasValueChangeHandlers<String> {

    private final String role;
    private final CheckBox checkbox;
    private final Label descriptionLabel;
    private boolean locked;
    private boolean enabled;

    public Permission(String role, String description) {
      this.role = role;
      this.checkbox = new CheckBox();
      this.descriptionLabel = new Label(description);

      this.add(checkbox);
      this.add(descriptionLabel);
      this.locked = false;
      this.enabled = true;

      this.descriptionLabel.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          if (isEnabled() && !locked) {
            checkbox.setValue(!checkbox.getValue());
            onChange();
          }
        }
      });

      this.checkbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
        @Override
        public void onValueChange(ValueChangeEvent<Boolean> event) {
          onChange();
        }
      });

      this.addStyleName("permission-category-item");
      checkbox.addStyleName("permission-checkbox");
      descriptionLabel.setStylePrimaryName("permission-description");
    }

    public boolean isLocked() {
      return locked;
    }

    public void setLocked(boolean locked) {
      this.locked = locked;
      checkbox.setEnabled(!locked);
    }

    public void setTooltip(String description) {
      this.setTitle(description);
    }

    public boolean isChecked() {
      return checkbox.getValue();
    }

    public void setChecked(boolean checked) {
      checkbox.setValue(checked);
    }

    public String getRole() {
      return role;
    }

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
      if (!locked) {
        this.checkbox.setEnabled(enabled);
      }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
      return addHandler(handler, ValueChangeEvent.getType());
    }

    protected void onChange() {
      ValueChangeEvent.fire(this, getValue());
    }

    public String getValue() {
      return getRole();
    }
  }
}