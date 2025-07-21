/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.generics.UpdatePermissionsRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.BasicSearchFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.NotSimpleFilterParameter;
import org.roda.core.data.v2.index.filter.OrFiltersParameters;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.HasPermissions;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Permissions.PermissionType;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.LoadingAsyncCallback;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.dialogs.MemberSelectDialog;
import org.roda.wui.client.common.lists.utils.ClientSelectedItemsUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class EditPermissions extends Composite {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private final String objectClass;
  @UiField
  FlowPanel editPermissionsDescription;
  @UiField
  Label userPermissionsEmpty;
  @UiField
  Label groupPermissionsEmpty;
  @UiField
  FlowPanel userPermissionsPanel;
  @UiField
  FlowPanel groupPermissionsPanel;
  @UiField
  Button buttonApplyToAll;
  @UiField
  TitlePanel title;
  private List<HasPermissions> objects = new ArrayList<>();
  private String objectId = null;
  private SelectedItems<?> selectedItems;

  public EditPermissions(String objectClass, SelectedItems<?> items) {
    this.objectClass = objectClass;
    this.selectedItems = items;
    initWidget(uiBinder.createAndBindUi(this));
    buttonApplyToAll.setVisible(IndexedAIP.class.getName().equals(objectClass));
    editPermissionsDescription.add(new HTMLWidgetWrapper("EditMultiplePermissionsDescription.html"));
  }

  public EditPermissions(String objectClass, HasPermissions object) {
    this.objects.add(object);
    this.objectClass = objectClass;
    this.objectId = object.getId();
    this.selectedItems = SelectedItemsList.create(objectClass, objectId);
    initWidget(uiBinder.createAndBindUi(this));
    buttonApplyToAll.setVisible(IndexedAIP.class.getName().equals(objectClass));
    createPermissionPanel();
  }

  public EditPermissions(String objectClass, SelectedItems<? extends HasPermissions> selectedItems,
    List<? extends HasPermissions> list) {
    this.objects.addAll(list);
    this.objectClass = objectClass;
    this.selectedItems = selectedItems;
    initWidget(uiBinder.createAndBindUi(this));
    buttonApplyToAll.setVisible(IndexedAIP.class.getName().equals(objectClass));
    editPermissionsDescription.add(new HTMLWidgetWrapper("EditPermissionsDescription.html"));
    createPermissionPanelList();
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  private void createPermissionPanelList() {
    Map<String, Set<PermissionType>> userPermissionsToShow = new HashMap<>();
    Map<String, Set<PermissionType>> groupPermissionsToShow = new HashMap<>();

    if (!objects.isEmpty()) {
      Permissions firstAIPPermissions = objects.get(0).getPermissions();

      for (String userName : firstAIPPermissions.getUsernames()) {
        userPermissionsToShow.put(userName, firstAIPPermissions.getUserPermissions(userName));
      }

      for (String groupName : firstAIPPermissions.getGroupnames()) {
        groupPermissionsToShow.put(groupName, firstAIPPermissions.getGroupPermissions(groupName));
      }

      for (int i = 1; i < objects.size(); i++) {
        Permissions permissions = objects.get(i).getPermissions();

        for (Iterator<Entry<String, Set<PermissionType>>> userIterator = userPermissionsToShow.entrySet()
          .iterator(); userIterator.hasNext();) {
          Entry<String, Set<PermissionType>> entry = userIterator.next();
          if (permissions.getUsernames().contains(entry.getKey())) {
            Set<PermissionType> userPermissionType = entry.getValue();

            for (Iterator<PermissionType> permissionTypeIterator = userPermissionType.iterator(); permissionTypeIterator
              .hasNext();) {
              PermissionType permissionType = permissionTypeIterator.next();

              if (!permissions.getUserPermissions(entry.getKey()).contains(permissionType)) {
                permissionTypeIterator.remove();
              }
            }
          } else {
            userIterator.remove();
          }
        }

        for (Iterator<Entry<String, Set<PermissionType>>> groupIterator = groupPermissionsToShow.entrySet()
          .iterator(); groupIterator.hasNext();) {
          Entry<String, Set<PermissionType>> entry = groupIterator.next();
          if (permissions.getGroupnames().contains(entry.getKey())) {
            Set<PermissionType> groupPermissionType = entry.getValue();

            for (Iterator<PermissionType> permissionTypeIterator = groupPermissionType
              .iterator(); permissionTypeIterator.hasNext();) {
              PermissionType permissionType = permissionTypeIterator.next();

              if (!permissions.getGroupPermissions(entry.getKey()).contains(permissionType)) {
                permissionTypeIterator.remove();
              }
            }
          } else {
            groupIterator.remove();
          }
        }

      }
    }

    userPermissionsEmpty.setVisible(userPermissionsToShow.isEmpty());
    groupPermissionsEmpty.setVisible(groupPermissionsToShow.isEmpty());

    for (Entry<String, Set<PermissionType>> entry : userPermissionsToShow.entrySet()) {
      PermissionPanel permissionPanel = new PermissionPanel(entry.getKey(), true, entry.getValue());
      userPermissionsPanel.add(permissionPanel);
      bindUpdateEmptyVisibility(permissionPanel);
    }

    for (Entry<String, Set<PermissionType>> entry : groupPermissionsToShow.entrySet()) {
      PermissionPanel permissionPanel = new PermissionPanel(entry.getKey(), false, entry.getValue());
      groupPermissionsPanel.add(permissionPanel);
      bindUpdateEmptyVisibility(permissionPanel);
    }
  }

  private void createPermissionPanel() {
    Permissions permissions = objects.get(0).getPermissions();

    userPermissionsEmpty.setVisible(permissions.getUsernames().isEmpty());
    groupPermissionsEmpty.setVisible(permissions.getGroupnames().isEmpty());

    for (String username : permissions.getUsernames()) {
      PermissionPanel permissionPanel = new PermissionPanel(username, true, permissions.getUserPermissions(username));
      bindUpdateEmptyVisibility(permissionPanel);
      userPermissionsPanel.add(permissionPanel);
    }

    for (String groupname : permissions.getGroupnames()) {
      PermissionPanel permissionPanel = new PermissionPanel(groupname, false,
        permissions.getGroupPermissions(groupname));
      bindUpdateEmptyVisibility(permissionPanel);
      groupPermissionsPanel.add(permissionPanel);
    }
  }

  public void addPermissionPanel(RODAMember member) {
    PermissionPanel permissionPanel = new PermissionPanel(member);
    if (member.isUser()) {
      bindUpdateEmptyVisibility(permissionPanel);
      userPermissionsPanel.insert(permissionPanel, 0);
    } else {
      bindUpdateEmptyVisibility(permissionPanel);
      groupPermissionsPanel.insert(permissionPanel, 0);
    }
  }

  private void bindUpdateEmptyVisibility(PermissionPanel permissionPanel) {
    permissionPanel.addAttachHandler(new Handler() {

      @Override
      public void onAttachOrDetach(AttachEvent event) {
        // running later to let attach/detach take effect
        Scheduler.get().scheduleDeferred(new Command() {
          @Override
          public void execute() {
            userPermissionsEmpty.setVisible(userPermissionsPanel.getWidgetCount() == 0);
            groupPermissionsEmpty.setVisible(groupPermissionsPanel.getWidgetCount() == 0);
          }
        });

      }
    });
  }

  @UiHandler("buttonAdd")
  void buttonAddHandler(ClickEvent e) {
    Filter filter = new Filter();

    for (String username : getAssignedUserNames()) {
      filter.add(new NotSimpleFilterParameter(RodaConstants.MEMBERS_ID, username));
    }

    for (String groupname : getAssignedGroupNames()) {
      filter.add(new NotSimpleFilterParameter(RodaConstants.MEMBERS_ID, groupname));
    }

    if (getAssignedUserNames().isEmpty() && getAssignedGroupNames().isEmpty()) {
      filter = new Filter(new BasicSearchFilterParameter(RodaConstants.INDEX_SEARCH, RodaConstants.INDEX_WILDCARD));
    }

    MemberSelectDialog selectDialog = new MemberSelectDialog(messages.selectUserOrGroupToAdd(), filter);
    selectDialog.showAndCenter();
    selectDialog.addValueChangeHandler(new ValueChangeHandler<RODAMember>() {

      @Override
      public void onValueChange(ValueChangeEvent<RODAMember> event) {
        RODAMember selected = event.getValue();
        if (selected != null) {
          addPermissionPanel(selected);
        }
      }
    });
  }

  public List<String> getAssignedUserNames() {
    List<String> ret = new ArrayList<>();
    for (int i = 0; i < userPermissionsPanel.getWidgetCount(); i++) {
      PermissionPanel permissionPanel = (PermissionPanel) userPermissionsPanel.getWidget(i);

      if (permissionPanel.isUser()) {
        ret.add(permissionPanel.getName());
      }
    }

    return ret;
  }

  public List<String> getAssignedGroupNames() {
    List<String> ret = new ArrayList<>();
    for (int i = 0; i < groupPermissionsPanel.getWidgetCount(); i++) {
      PermissionPanel permissionPanel = (PermissionPanel) groupPermissionsPanel.getWidget(i);

      if (!permissionPanel.isUser()) {
        ret.add(permissionPanel.getName());
      }
    }

    return ret;
  }

  public Permissions getPermissions() {
    Permissions permissions = new Permissions();

    for (int i = 0; i < userPermissionsPanel.getWidgetCount(); i++) {
      PermissionPanel permissionPanel = (PermissionPanel) userPermissionsPanel.getWidget(i);

      if (permissionPanel.isUser()) {
        permissions.setUserPermissions(permissionPanel.getName(), permissionPanel.getPermissions());
      } else {
        permissions.setGroupPermissions(permissionPanel.getName(), permissionPanel.getPermissions());
      }
    }

    for (int i = 0; i < groupPermissionsPanel.getWidgetCount(); i++) {
      PermissionPanel permissionPanel = (PermissionPanel) groupPermissionsPanel.getWidget(i);

      if (permissionPanel.isUser()) {
        permissions.setUserPermissions(permissionPanel.getName(), permissionPanel.getPermissions());
      } else {
        permissions.setGroupPermissions(permissionPanel.getName(), permissionPanel.getPermissions());
      }
    }

    return permissions;
  }

  private void apply(final boolean recursive) {
    final Permissions permissions = getPermissions();

    Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
      RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
      new AsyncCallback<String>() {

        @Override
        public void onFailure(Throwable caught) {
          // do nothing
        }

        @Override
        public void onSuccess(String details) {
          if (IndexedAIP.class.getName().equals(objectClass)) {
            SelectedItems<IndexedAIP> aips = (SelectedItems<IndexedAIP>) selectedItems;
            BrowserService.Util.getInstance().updateAIPPermissions(aips, permissions, details, recursive,
              new LoadingAsyncCallback<Job>() {

                @Override
                public void onSuccessImpl(Job result) {
                  Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());
                  Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable caught) {
                      // do nothing
                    }

                    @Override
                    public void onSuccess(final Void nothing) {
                      HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
                    }
                  });
                }
              });
          } else if (IndexedDIP.class.getName().equals(objectClass)) {
            Services services = new Services("Update DIP permissions", "update");
            UpdatePermissionsRequest<IndexedDIP> request = new UpdatePermissionsRequest<>();
            request.setPermissions(permissions);
            request.setItemsToUpdate((SelectedItems<IndexedDIP>) selectedItems);
            request.setDetails(details);
            services.dipResource(s -> s.updatePermissions(request)).whenComplete((job, throwable) -> {
              Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());
              Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

                @Override
                public void onFailure(Throwable caught) {
                  // do nothing
                }

                @Override
                public void onSuccess(final Void nothing) {
                  HistoryUtils.newHistory(ShowJob.RESOLVER, job.getId());
                }
              });
            });
          }
        }
      });
  }

  @UiHandler("buttonApply")
  void buttonApplyHandler(ClickEvent e) {
    apply(false);
  }  public static final HistoryResolver AIP_RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        final String aipId = historyTokens.get(0);
        Services services = new Services("Retrieve AIP", "get");
        services.aipResource(s -> s.findByUuid(aipId, LocaleInfo.getCurrentLocale().getLocaleName()))
          .whenComplete((aip, throwable) -> {
            if (throwable != null) {
              HistoryUtils.newHistory(BrowseTop.RESOLVER);
              callback.onSuccess(null);
            } else {
              EditPermissions edit = new EditPermissions(IndexedAIP.class.getName(), aip);
              edit.title.setIcon(DescriptionLevelUtils.getElementLevelIconSafeHtml(aip.getLevel(), false));
              edit.title.setText(aip.getTitle() != null ? aip.getTitle() : aip.getId());
              callback.onSuccess(edit);
            }
          });
      } else if (historyTokens.isEmpty()) {
        LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();

        SelectedItems<? extends IsIndexed> selected = selectedItems.getSelectedItems();
        if (selected instanceof SelectedItemsList<?>) {
          if (!ClientSelectedItemsUtils.isEmpty(selected)) {
            List<String> ids = ((SelectedItemsList<? extends IsIndexed>) selected).getIds();
            List<FilterParameter> collect = ids.stream()
              .map(m -> new SimpleFilterParameter(RodaConstants.INDEX_UUID, m)).collect(Collectors.toList());
            OrFiltersParameters orFiltersParameters = new OrFiltersParameters(collect);
            FindRequest findRequest = FindRequest
              .getBuilder(IndexedAIP.class.getName(), new Filter(orFiltersParameters), true)
              .withSublist(new Sublist(0, 20)).build();
            Services services = new Services("Find AIPs", "get");
            services.aipResource(s -> s.find(findRequest, LocaleInfo.getCurrentLocale().getLocaleName()))
              .whenComplete((result, throwable) -> {
                if (throwable != null) {
                  HistoryUtils.newHistory(BrowseTop.RESOLVER);
                  callback.onFailure(throwable);
                } else {
                  List<? extends HasPermissions> hasPermissionsObjects = result.getResults();
                  EditPermissions edit = new EditPermissions(IndexedAIP.class.getName(),
                    (SelectedItems<IndexedAIP>) selectedItems.getSelectedItems(), hasPermissionsObjects);
                  callback.onSuccess(edit);
                }
              });
          }
        } else {
          EditPermissions editPermissions = new EditPermissions(IndexedAIP.class.getName(), selected);
          callback.onSuccess(editPermissions);
        }
      } else {
        HistoryUtils.newHistory(BrowseTop.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {EditPermissions.AIP_RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(BrowseTop.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "edit_permissions";
    }
  };

  @UiHandler("buttonApplyToAll")
  void buttonApplyToAllHandler(ClickEvent e) {
    apply(true);
  }

  @UiHandler("buttonClose")
  void buttonCancelHandler(ClickEvent e) {
    if (objectId != null) {
      if (IndexedAIP.class.getName().equals(objectClass)) {
        HistoryUtils.openBrowse(objectId);
      } else {
        HistoryUtils.openBrowse((IndexedDIP) objects.get(0));
      }
    } else {
      HistoryUtils.newHistory(BrowseTop.RESOLVER);
    }
  }

  interface MyUiBinder extends UiBinder<Widget, EditPermissions> {
  }

  public class PermissionPanel extends Composite {
    private FlowPanel panel;
    private FlowPanel panelBody;
    private FlowPanel rightPanel;
    private HTML type;
    private Label nameLabel;
    private FlowPanel editPermissionsPanel;
    private Button removePanel;
    private Button selectAllPanel;

    private String name;
    private boolean isUser;

    public PermissionPanel(RODAMember member) {
      this(member.getName(), member.isUser(), new HashSet<>());
    }

    public PermissionPanel(String name, boolean isUser, Set<PermissionType> permissions) {
      this.name = name;
      this.isUser = isUser;

      panel = new FlowPanel();
      panelBody = new FlowPanel();

      type = new HTML(
        SafeHtmlUtils.fromSafeConstant(isUser ? "<i class='fa fa-user'></i>" : "<i class='fa fa-users'></i>"));
      nameLabel = new Label(name);

      rightPanel = new FlowPanel();
      editPermissionsPanel = new FlowPanel();
      boolean allSelected = true;

      for (PermissionType permissionType : Permissions.PermissionType.values()) {
        ValueCheckBox valueCheckBox = new ValueCheckBox(permissionType);
        if (permissions.contains(permissionType)) {
          valueCheckBox.setValue(true);
        } else {
          allSelected = false;
        }

        editPermissionsPanel.add(valueCheckBox);
        valueCheckBox.addStyleName("permission-edit-checkbox");
      }

      removePanel = new Button(messages.removeButton());
      if (!allSelected) {
        selectAllPanel = new Button(messages.selectAllButton());
      } else {
        selectAllPanel = new Button(messages.clearButton());
      }

      panelBody.add(type);
      panelBody.add(nameLabel);
      panelBody.add(rightPanel);

      rightPanel.add(editPermissionsPanel);
      rightPanel.add(selectAllPanel);
      rightPanel.add(removePanel);

      panel.add(panelBody);

      initWidget(panel);

      panel.addStyleName("panel permission");
      panel.addStyleName(isUser ? "permission-user" : "permission-group");
      panelBody.addStyleName("panel-body");
      type.addStyleName("permission-type");
      nameLabel.addStyleName("permission-name");
      rightPanel.addStyleName("pull-right");
      editPermissionsPanel.addStyleName("permission-edit");
      removePanel.addStyleName("permission-remove btn btn-danger btn-ban");
      selectAllPanel.addStyleName("permission-remove permission-clear btn btn-check");

      removePanel.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
          PermissionPanel.this.removeFromParent();
        }
      });

      selectAllPanel.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
          boolean selectAll = selectAllPanel.getText().equals(messages.selectAllButton());

          for (int i = 0; i < editPermissionsPanel.getWidgetCount(); i++) {
            ValueCheckBox valueCheckBox = (ValueCheckBox) editPermissionsPanel.getWidget(i);
            valueCheckBox.setValue(selectAll);
          }

          if (!selectAll) {
            selectAllPanel.setText(messages.selectAllButton());
          } else {
            selectAllPanel.setText(messages.clearButton());
          }
        }
      });

    }

    public Set<PermissionType> getPermissions() {
      HashSet<PermissionType> permissions = new HashSet<>();
      for (int i = 0; i < editPermissionsPanel.getWidgetCount(); i++) {
        ValueCheckBox valueCheckBox = (ValueCheckBox) editPermissionsPanel.getWidget(i);
        if (valueCheckBox.getValue()) {
          permissions.add(valueCheckBox.getPermissionType());
        }
      }
      return permissions;
    }

    public String getName() {
      return name;
    }

    public boolean isUser() {
      return isUser;
    }

    public class ValueCheckBox extends CheckBox {
      private PermissionType permissionType;

      public ValueCheckBox(PermissionType permissionType) {
        super(messages.objectPermission(permissionType));
        setTitle(messages.objectPermissionDescription(permissionType));
        this.permissionType = permissionType;
      }

      public PermissionType getPermissionType() {
        return permissionType;
      }
    }
  }



  public static final HistoryResolver DIP_RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        final String dipId = historyTokens.get(0);
        Services services = new Services("Retrieve DIP", "get");
        services.dipResource(s -> s.findByUuid(dipId, LocaleInfo.getCurrentLocale().getLocaleName()))
          .whenComplete((indexedDIP, throwable) -> {
            if (throwable != null) {
              HistoryUtils.newHistory(BrowseDIP.RESOLVER, dipId);
              callback.onSuccess(null);
            } else {
              EditPermissions edit = new EditPermissions(IndexedDIP.class.getName(), indexedDIP);
              callback.onSuccess(edit);
            }
          });
      } else if (historyTokens.isEmpty()) {
        LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
        final SelectedItems<IndexedDIP> selected = (SelectedItems<IndexedDIP>) selectedItems.getSelectedItems();

        if (!ClientSelectedItemsUtils.isEmpty(selected)) {
          BrowserService.Util.getInstance().retrieve(IndexedDIP.class.getName(), selected,
            RodaConstants.DIP_PERMISSIONS_FIELDS_TO_RETURN, new AsyncCallback<List<IndexedDIP>>() {

              @Override
              public void onFailure(Throwable caught) {
                HistoryUtils.newHistory(BrowseTop.RESOLVER);
                callback.onSuccess(null);
              }

              @Override
              public void onSuccess(List<IndexedDIP> dips) {
                List<? extends HasPermissions> hasPermissionsObjects = dips;
                EditPermissions edit = new EditPermissions(IndexedDIP.class.getName(), selected, hasPermissionsObjects);
                callback.onSuccess(edit);
              }
            });
        }
      } else {
        HistoryUtils.newHistory(BrowseTop.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {EditPermissions.DIP_RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(BrowseTop.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "edit_dip_permissions";
    }
  };

}
