/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.user.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DisclosureEvent;
import com.google.gwt.user.client.ui.DisclosureHandler;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import pt.gov.dgarq.roda.core.data.v2.RODAMember;
import pt.gov.dgarq.roda.core.data.v2.RodaGroup;
import pt.gov.dgarq.roda.core.data.v2.RodaUser;
import pt.gov.dgarq.roda.core.data.v2.User;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.images.CommonImageBundle;
import pt.gov.dgarq.roda.wui.common.client.widgets.AlphabetListItem;
import pt.gov.dgarq.roda.wui.management.user.client.images.UserManagementImageBundle;

/**
 * @author Luis Faria
 * 
 */
public class UserDisclosurePanel extends SimplePanel implements AlphabetListItem {

  private static CommonImageBundle commonImageBundle = (CommonImageBundle) GWT.create(CommonImageBundle.class);

  private static UserManagementImageBundle userManagementImageBundle = (UserManagementImageBundle) GWT
    .create(UserManagementImageBundle.class);

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private final User user;

  private final DisclosurePanel disclosurePanel;

  private final HorizontalPanel header;

  private final VerticalPanel groupList;

  private Image userIcon;

  private Label userName;

  private Label userFullName;

  private Image viewActionsReport;

  private GroupMiniPanel selectedGroupPanel;

  private Map<ChangeListener, DisclosureHandler> changeToDisclosureEvents;

  /**
   * Create a new user disclosure panel
   * 
   * @param user
   *          the user
   */
  public UserDisclosurePanel(User user) {
    this.user = user;
    this.header = createHeader();
    this.disclosurePanel = new DisclosurePanel(header);

    this.groupList = new VerticalPanel();

    this.changeToDisclosureEvents = new HashMap<ChangeListener, DisclosureHandler>();

    this.selectedGroupPanel = null;

    setWidget(disclosurePanel);

    this.disclosurePanel.setContent(groupList);
    this.disclosurePanel.addEventHandler(new DisclosureHandler() {
      private boolean loaded = false;

      public void onClose(DisclosureEvent arg0) {
        if (selectedGroupPanel != null) {
          selectedGroupPanel.setSelected(false);
          selectedGroupPanel = null;
        }
      }

      public void onOpen(DisclosureEvent arg0) {
        if (!loaded) {
          UserManagementService.Util.getInstance().getUser(UserDisclosurePanel.this.user.getName(),
            new AsyncCallback<RodaUser>() {

              public void onFailure(Throwable caught) {
                logger.error("Error getting user " + UserDisclosurePanel.this.user.getName(), caught);
              }

              public void onSuccess(RodaUser user) {
                Set<String> groups = user.getDirectGroups();
                Iterator<String> it = groups.iterator();
                while (it.hasNext()) {
                  String groupname = it.next();
                  final GroupMiniPanel groupPanel = new GroupMiniPanel(groupname);
                  groupPanel.addChangeListener(new ChangeListener() {

                    public void onChange(Widget sender) {
                      if (selectedGroupPanel != null) {
                        selectedGroupPanel.setSelected(false);
                      }
                      if (groupPanel.isSelected()) {
                        selectedGroupPanel = groupPanel;
                      } else {
                        selectedGroupPanel = null;
                      }
                      UserDisclosurePanel.this.onChange();
                    }

                  });
                  groupList.add(groupPanel.getWidget());

                }
                loaded = true;
              }
            });
        }
      }

    });

    this.viewActionsReport.addClickListener(new ClickListener() {

      public void onClick(Widget arg0) {
        ActionReportWindow actionReportWindow = new ActionReportWindow(UserDisclosurePanel.this.user);
        actionReportWindow.show();

      }

    });

    this.addStyleName("wui-UserDisclosurePanel");
    groupList.addStyleName("wui-GroupList");
  }

  private HorizontalPanel createHeader() {
    HorizontalPanel layout = new HorizontalPanel();
    userIcon = user.isActive() ? userManagementImageBundle.user().createImage() : userManagementImageBundle
      .inactiveUser().createImage();
    userName = new Label(user.getName());
    userFullName = new Label(user.getFullName());
    viewActionsReport = commonImageBundle.report().createImage();

    layout.add(userIcon);
    layout.add(userName);
    layout.add(userFullName);
    layout.add(viewActionsReport);

    layout.setCellWidth(userFullName, "100%");
    layout.setCellHorizontalAlignment(viewActionsReport, HasAlignment.ALIGN_CENTER);
    layout.setCellVerticalAlignment(viewActionsReport, HasAlignment.ALIGN_MIDDLE);

    layout.addStyleName("user-header");
    userIcon.addStyleName("user-icon");
    userName.addStyleName("user-name");
    userFullName.addStyleName("user-fullname");
    viewActionsReport.addStyleName("user-report");
    return layout;
  }

  /**
   * Get the user defined by this panel
   * 
   * @return the user
   */
  public User getUser() {
    return user;
  }

  public Widget getWidget() {
    return this;
  }

  public boolean isSelected() {
    return disclosurePanel.isOpen();
  }

  public void setSelected(boolean selected) {
    logger.info((selected ? "opening" : "closing") + " user disclosure panel");
    disclosurePanel.setOpen(selected);
  }

  /**
   * Get the selectedGroupPanel user or group
   * 
   * @param callback
   *          handle the user if disclosure is selectedGroupPanel but no group
   *          is selectedGroupPanel, the group if both disclosure and group are
   *          selectedGroupPanel or null if disclosure is closed
   */
  public void getSelected(final AsyncCallback<RODAMember> callback) {
    if (isSelected() && this.selectedGroupPanel == null) {
      callback.onSuccess(user);
    } else if (isSelected()) {
      this.selectedGroupPanel.getGroup(new AsyncCallback<RodaGroup>() {

        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }

        public void onSuccess(RodaGroup group) {
          callback.onSuccess(group);
        }

      });
    } else {
      callback.onSuccess(null);
    }
  }

  public void addChangeListener(final ChangeListener listener) {
    DisclosureHandler disclosureHandler = new DisclosureHandler() {

      public void onClose(DisclosureEvent event) {
        listener.onChange(UserDisclosurePanel.this);
      }

      public void onOpen(DisclosureEvent event) {
        listener.onChange(UserDisclosurePanel.this);
      }

    };

    this.disclosurePanel.addEventHandler(disclosureHandler);
    this.changeToDisclosureEvents.put(listener, disclosureHandler);
  }

  public void removeChangeListener(ChangeListener listener) {
    DisclosureHandler disclosureHandler = (DisclosureHandler) changeToDisclosureEvents.get(listener);
    this.disclosurePanel.removeEventHandler(disclosureHandler);
    this.changeToDisclosureEvents.remove(listener);

  }

  protected void onChange() {
    for (ChangeListener listener : changeToDisclosureEvents.keySet()) {
      listener.onChange(getWidget());
    }
  }

  public String getKeyword() {
    return user.getName();
  }

  public boolean matches(String regex) {
    return user.getName().matches(regex) || user.getFullName().matches(regex);
  }

}
