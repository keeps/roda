/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.management;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sort.SortParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.common.client.ClientLogger;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.LocaleInfo;
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
public class GroupSelect extends FlowPanel implements HasValueChangeHandlers<List<Group>> {

  private class GroupCheckbox extends HorizontalPanel
    implements HasValueChangeHandlers<Group>, Comparable<GroupCheckbox> {

    private final String sortingkeyword;

    private final Group group;

    private final CheckBox checkbox;

    private final Label descriptionLabel;

    public GroupCheckbox(Group group, String description, String sortingkeyword) {
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

    public boolean isChecked() {
      return checkbox.getValue();
    }

    public void setChecked(boolean checked) {
      checkbox.setValue(checked);
    }

    public Group getGroup() {
      return group;
    }

    @Override
    public int compareTo(GroupCheckbox groupCheckbox) {
      return sortingkeyword.compareTo(groupCheckbox.sortingkeyword);
    }

    @SuppressWarnings("unused")
    public String getSortingkeyword() {
      return sortingkeyword;
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Group> handler) {
      return addHandler(handler, ValueChangeEvent.getType());
    }

    protected void onChange() {
      ValueChangeEvent.fire(this, getValue());
    }

    public Group getValue() {
      return getGroup();
    }
  }

  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  private final List<String> blacklist;

  private final List<GroupCheckbox> groups;

  private HashMap<String, Group> userSelections;

  private boolean enabled;

  /**
   * Create a new group selection widget
   *
   * @param visible
   *          start as visible or wait until its initialized
   */
  public GroupSelect(boolean visible) {
    this.groups = new Vector<GroupCheckbox>();
    this.blacklist = new Vector<String>();
    this.userSelections = new HashMap<String, Group>();
    enabled = true;
    this.addStyleName("groups");
  }

  public void init(final AsyncCallback<Boolean> callback) {

    // TODO use RodaMemberList instead of a list of checkboxes

    boolean isUser = false;
    boolean justActive = true;
    Filter filter = new Filter();

    filter.add(new SimpleFilterParameter(RodaConstants.MEMBERS_IS_USER, Boolean.toString(isUser)));

    Sorter sorter = new Sorter(new SortParameter(RodaConstants.MEMBERS_FULLNAME, false));

    BrowserService.Util.getInstance().find(RODAMember.class.getName(), filter, sorter, Sublist.ALL, Facets.NONE,
      LocaleInfo.getCurrentLocale().getLocaleName(), justActive, new AsyncCallback<IndexResult<RODAMember>>() {

        @Override
        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }

        @Override
        public void onSuccess(IndexResult<RODAMember> members) {
          for (RODAMember member : members.getResults()) {
            if (member instanceof Group) {
              Group group = (Group) member;
              GroupCheckbox groupCheckbox = new GroupCheckbox(group, group.getFullName(), group.getId());
              groups.add(groupCheckbox);
            }
          }

          for (final GroupCheckbox groupCheckbox : groups) {
            GroupSelect.this.add(groupCheckbox);
            groupCheckbox.addValueChangeHandler(new ValueChangeHandler<Group>() {

              @Override
              public void onValueChange(ValueChangeEvent<Group> event) {
                if (userSelections.keySet().contains(event.getValue().getId())) {
                  userSelections.remove(event.getValue().getId());
                } else {
                  userSelections.put(event.getValue().getId(), event.getValue());
                }
                onChange();
              }
            });
          }
          callback.onSuccess(true);
        }
      });
  }

  public Map<String, Group> getUserSelections() {
    return userSelections;
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
        if (g.getGroup().getId().equals(group)) {
          foundit = true;
          g.setChecked(true);
          userSelections.put(group, g.getGroup());
        }
      }
    }
  }

  public Set<String> getMemberGroups() {
    Set<String> memberGroups = new HashSet<String>();
    for (GroupCheckbox g : groups) {
      if (g.isChecked()) {
        memberGroups.add(g.getGroup().getId());
      }
    }
    return memberGroups;
  }

  public void addGroupToBlacklist(String group) {
    blacklist.add(group);
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<Group>> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  protected void onChange() {
    ValueChangeEvent.fire(this, getValue());
  }

  public List<Group> getValue() {
    return new Vector<Group>(getUserSelections().values());
  }
}
