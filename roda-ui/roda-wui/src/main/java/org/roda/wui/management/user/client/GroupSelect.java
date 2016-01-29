/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.management.user.client;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.roda.core.data.v2.user.Group;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.widgets.LoadingPopup;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * 
 * @author Luis Faria <lfaria@keep.pt>
 *
 */
public class GroupSelect extends FlowPanel implements HasValueChangeHandlers<String> {

  private class GroupCheckbox extends HorizontalPanel
    implements HasValueChangeHandlers<Boolean>, Comparable<GroupCheckbox> {

    private final String sortingkeyword;

    private final String group;

    private final CheckBox checkbox;

    private final Label descriptionLabel;

    public GroupCheckbox(String group, String description, String sortingkeyword) {
      this.group = group;
      this.checkbox = new CheckBox();
      this.descriptionLabel = new Label(description);
      this.sortingkeyword = sortingkeyword;
      this.add(checkbox);
      this.add(descriptionLabel);

      this.descriptionLabel.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
          checkbox.setValue(!checkbox.getValue());
          onChange();
        }
      });

      this.checkbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

        @Override
        public void onValueChange(ValueChangeEvent<Boolean> event) {
          onChange();
        }
      });

      this.addStyleName("group");
      checkbox.addStyleName("group-checkbox");
      descriptionLabel.setStylePrimaryName("group-description");
    }

    protected void onChange() {
      ValueChangeEvent.fire(this, checkbox.getValue());
    }

    public boolean isChecked() {
      return checkbox.getValue();
    }

    public void setChecked(boolean checked) {
      checkbox.setValue(checked);
    }

    public String getGroup() {
      return group;
    }

    public int compareTo(GroupCheckbox groupCheckbox) {
      return sortingkeyword.compareTo(groupCheckbox.sortingkeyword);
    }

    @SuppressWarnings("unused")
    public String getSortingkeyword() {
      return sortingkeyword;
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Boolean> handler) {
      return addHandler(handler, ValueChangeEvent.getType());
    }
  }

  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  private final List<String> blacklist;

  private final List<GroupCheckbox> groups;

  private boolean enabled;

  private final LoadingPopup loading;

  /**
   * Create a new group selection widget
   * 
   * @param visible
   *          start as visible or wait until its initialized
   */
  public GroupSelect(boolean visible) {
    this.groups = new Vector<GroupCheckbox>();
    this.blacklist = new Vector<String>();
    loading = new LoadingPopup(this);
    loading.show();

    enabled = true;

    this.addStyleName("groups");
  }

  public void init(final AsyncCallback<Boolean> callback) {
    UserManagementService.Util.getInstance().listAllGroups(new AsyncCallback<List<Group>>() {
      @Override
      public void onFailure(Throwable caught) {
        loading.hide();
        callback.onFailure(caught);
      }

      @Override
      public void onSuccess(List<Group> allGroups) {
        for (Group group : allGroups) {
          if (!blacklist.contains(group.getId())) {
            GroupCheckbox groupCheckbox = new GroupCheckbox(group.getId(), group.getFullName(), group.getId());
            groups.add(groupCheckbox);
          }
        }

        Collections.sort(groups);

        for (final GroupCheckbox groupCheckbox : groups) {
          GroupSelect.this.add(groupCheckbox);
          groupCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
              onChange();
            }
          });
        }
        loading.hide();
        callback.onSuccess(true);
      }
    });
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  protected void onChange() {
    ValueChangeEvent.fire(this, "");
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setMemberGroups(Set<String> memberGroups) {
    Iterator<String> it = memberGroups.iterator();

    while (it.hasNext()) {
      String group = it.next();
      boolean foundit = false;
      for (Iterator<GroupCheckbox> j = groups.iterator(); j.hasNext() && !foundit;) {
        GroupCheckbox g = j.next();
        if (g.getGroup().equals(group)) {
          foundit = true;
          g.setChecked(true);
        }
      }
    }
  }

  public Set<String> getMemberGroups() {
    Set<String> memberGroups = new HashSet<String>();
    for (GroupCheckbox g : groups) {
      if (g.isChecked()) {
        memberGroups.add(g.getGroup());
      }
    }
    return memberGroups;
  }

  public void addGroupToBlacklist(String group) {
    blacklist.add(group);
  }
}
