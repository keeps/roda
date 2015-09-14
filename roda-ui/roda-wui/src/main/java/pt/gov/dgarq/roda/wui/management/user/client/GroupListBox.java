package pt.gov.dgarq.roda.wui.management.user.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ListBox;

import pt.gov.dgarq.roda.core.data.v2.Group;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;

/**
 * @author Luis Faria <lfaria@keep.pt>
 * 
 *         ListBox containing all groups. Each item on the ListBox is the group
 *         full name and the value is the group name
 * 
 *         The ListBox can be collapse, showing only one group at a time, or not
 *         collapsed, showing all groups at a time
 * 
 */
public class GroupListBox extends ListBox {

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private boolean colapsed;

  private boolean enabled;

  private Set<String> excludeSet;

  private boolean initialized;

  /**
   * Create a new group list box
   * 
   * @param colapsed
   */
  public GroupListBox(boolean colapsed) {
    this.colapsed = colapsed;
    enabled = false;
    initialized = false;
    excludeSet = new HashSet<String>();
    initGroupList();

  }

  private void initGroupList() {
    UserManagementService.Util.getInstance().listAllGroups(new AsyncCallback<List<Group>>() {

      @Override
      public void onFailure(Throwable caught) {
        logger.error("Error while getting group list", caught);
      }

      @Override
      public void onSuccess(List<Group> groups) {
        for (Group group : groups) {
          String groupname = group.getName();
          if (!excludeSet.contains(groupname)) {
            GroupListBox.this.addItem(groupname, groupname);
          }
        }

        initialized = true;

        if (colapsed) {
          GroupListBox.this.setVisibleItemCount(1);
        } else {
          GroupListBox.this.setVisibleItemCount(groups.size());
        }

      }
    });
  }

  /**
   * Exclude a group from the list
   * 
   * @param groupname
   *          the group name
   */
  public void exclude(String groupname) {
    excludeSet.add(groupname);
    if (initialized) {
      int index = -1;
      for (int i = 0; i < getItemCount() && index == -1; i++) {
        if (getValue(i).equals(groupname)) {
          index = i;
        }
      }

      if (index != -1) {
        removeItem(index);
      } else {
        List<String> items = new ArrayList<String>();
        for (int i = 0; i < getItemCount(); i++) {
          items.add(getValue(i));
        }
        logger.error("Group " + groupname + " could not be excluded from list " + items);
      }
    }
  }

  /**
   * Is group list box collapsed
   * 
   * @return
   */
  public boolean isCollapsed() {
    return colapsed;
  }

  /**
   * Set group list box collapsed
   * 
   * @param collapsed
   */
  public void setCollapsed(boolean collapsed) {
    this.colapsed = collapsed;
    if (collapsed) {
      setVisibleItemCount(1);
    } else {
      setVisibleItemCount(this.getItemCount());
    }
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
    super.setEnabled(enabled);
    super.setVisible(enabled);
  }

  public boolean isEnabled() {
    return this.enabled;
  }

}