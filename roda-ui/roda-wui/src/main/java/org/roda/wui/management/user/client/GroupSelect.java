/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.management.user.client;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.roda.wui.common.client.images.CommonImageBundle;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SourcesChangeEvents;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.UserManagementConstants;

/**
 * 
 * @author Luis Faria <lfaria@keep.pt>
 *
 */
public class GroupSelect extends HorizontalPanel implements SourcesChangeEvents {

  private static UserManagementConstants constants = (UserManagementConstants) GWT
    .create(UserManagementConstants.class);

  private static CommonImageBundle commonImageBundle = (CommonImageBundle) GWT.create(CommonImageBundle.class);

  private final List<ChangeListener> changeListeners;

  private final GroupListBox allGroups;

  private final ListBox memberGroups;

  private final Image addGroup;

  private final Image removeGroup;

  private boolean enabled;

  /**
   * Create a new group selection widget
   * 
   * @param visible
   *          start as visible or wait until its initialized
   */
  public GroupSelect(boolean visible) {

    this.setVisible(visible);
    changeListeners = new Vector<ChangeListener>();

    VerticalPanel allGroupsPanel = new VerticalPanel();
    Label allGroupsLabel = new Label(constants.allGroups());
    allGroupsLabel.setWordWrap(false);

    allGroups = new GroupListBox(false);
    allGroups.setMultipleSelect(true);
    allGroupsPanel.add(allGroupsLabel);
    allGroupsPanel.add(allGroups);

    this.add(allGroupsPanel);

    VerticalPanel groupControlButtons = new VerticalPanel();

    addGroup = commonImageBundle.plus().createImage();
    removeGroup = commonImageBundle.minus().createImage();

    groupControlButtons.add(addGroup);
    groupControlButtons.add(removeGroup);

    this.add(groupControlButtons);

    VerticalPanel memberGroupsPanel = new VerticalPanel();
    Label memberGroupsLabel = new Label(constants.memberGroups());
    memberGroupsLabel.setWordWrap(false);

    memberGroups = new ListBox();
    memberGroups.setVisibleItemCount(allGroups.getItemCount());
    memberGroups.setMultipleSelect(true);

    memberGroupsPanel.add(memberGroupsLabel);
    memberGroupsPanel.add(memberGroups);

    this.add(memberGroupsPanel);

    addGroup.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {

        for (int i = allGroups.getItemCount() - 1; i >= 0; i--) {
          if (allGroups.isItemSelected(i)) {
            String groupName = allGroups.getValue(i);
            memberGroups.addItem(groupName, groupName);
            allGroups.removeItem(i);
            GroupSelect.this.onChange();
          }
        }

      }

    });

    removeGroup.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {

        for (int i = memberGroups.getItemCount() - 1; i >= 0; i--) {
          if (memberGroups.isItemSelected(i)) {
            String groupName = memberGroups.getValue(i);
            allGroups.addItem(groupName, groupName);
            memberGroups.removeItem(i);
            GroupSelect.this.onChange();
          }
        }
      }

    });

    this.setCellVerticalAlignment(groupControlButtons, HorizontalPanel.ALIGN_MIDDLE);

    this.addStyleName("groupselect");
    allGroupsLabel.addStyleName("group-label");
    memberGroupsLabel.addStyleName("group-label");

    allGroups.addStyleName("group-box");
    memberGroups.addStyleName("group-box");

    groupControlButtons.addStyleName("group-button-panel");
    addGroup.addStyleName("group-button-add");
    removeGroup.addStyleName("group-button-remove");

    enabled = true;
  }

  protected void onChange() {
    for (ChangeListener listener : changeListeners) {
      listener.onChange(this);
    }
  }

  public void addChangeListener(ChangeListener listener) {
    this.changeListeners.add(listener);

  }

  public void removeChangeListener(ChangeListener listener) {
    this.changeListeners.remove(listener);
  }

  /**
   * Get selected groups
   * 
   * @return
   */
  public Set<String> getMemberGroups() {
    Set<String> ret = new HashSet<String>();
    for (int i = 0; i < memberGroups.getItemCount(); i++) {
      ret.add(memberGroups.getValue(i));
    }
    return ret;
  }

  /**
   * Set selected groups
   * 
   * @param superGroups
   */
  public void setMemberGroups(Set<String> superGroups) {
    for (String groupname : superGroups) {
      this.memberGroups.addItem(groupname);
      allGroups.exclude(groupname);
    }
  }

  /**
   * Set group list box enabled
   * 
   * @param enabled
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
    this.addGroup.setVisible(enabled);
    this.removeGroup.setVisible(enabled);
    this.allGroups.setEnabled(enabled);
    this.memberGroups.setEnabled(enabled);
  }

  /**
   * Is group list box enabled
   * 
   * @return
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Exclude possible selectable groups
   * 
   * @param groupname
   */
  public void exclude(String groupname) {
    allGroups.exclude(groupname);
    boolean foundit = false;
    for (int i = memberGroups.getItemCount() - 1; i >= 0 && !foundit; i--) {
      if (memberGroups.getValue(i).equals(groupname)) {
        memberGroups.removeItem(i);
        foundit = true;
      }
    }
  }
}
