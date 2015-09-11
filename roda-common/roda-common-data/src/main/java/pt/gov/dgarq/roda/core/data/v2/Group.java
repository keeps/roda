package pt.gov.dgarq.roda.core.data.v2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.gov.dgarq.roda.core.data.RODAMember;

/**
 * This is a group in RODA.
 * 
 * @author Rui Castro
 */
public class Group extends RodaGroup {

  private static final long serialVersionUID = -4051946961307715630L;

  private List<String> memberUserNames = new ArrayList<String>();

  private List<String> memberGroupNames = new ArrayList<String>();

  /**
   * Constructs a new empty group.
   */
  public Group() {
    super();
  }

  /**
   * Constructs a new Group with the given name.
   * 
   * @param name
   *          the name of the group.
   */
  public Group(String name) {
    super(name, name);
  }

  /**
   * Constructs a new Group cloning a given Group.
   * 
   * @param group
   *          the Group to be cloned.
   */
  public Group(Group group) {
    super(group.getId(), group.getName(), group.getDirectGroups(), group.getAllGroups(), group.getDirectRoles(),
      group.getAllRoles());
    setActive(true);
    setMemberUserNames(group.getMemberUserNames());
    setMemberGroupNames(group.getMemberGroupNames());
  }

  /**
   * @see RODAMember#toString()
   */
  public String toString() {
    return "Group (" + super.toString() + ", memberUserNames=" + this.memberUserNames + ", memberGroupNames="
      + this.memberGroupNames + ")";
  }

  /**
   * @return the memberUserNames
   */
  public String[] getMemberUserNames() {
    return (String[]) memberUserNames.toArray(new String[memberUserNames.size()]);
  }

  /**
   * @param memberUserNames
   *          the memberUserNames to set
   */
  public void setMemberUserNames(String[] memberUserNames) {
    this.memberUserNames.clear();
    if (memberUserNames != null) {
      this.memberUserNames.addAll(Arrays.asList(memberUserNames));
    }
  }

  /**
   * @return the memberGroupNames
   */
  public String[] getMemberGroupNames() {
    return (String[]) memberGroupNames.toArray(new String[memberGroupNames.size()]);
  }

  /**
   * Sets the names of the {@link Group}s that belong to this {@link Group}.
   * <p>
   * <strong>NOTE:</strong> using this method doesn't change the member groups
   * when UserManagement.modifyGroup(Group group) is called. Use
   * {@link RODAMember#addGroup(String)} or
   * {@link RODAMember#setGroups(String[])} to change the {@link Group}s of a
   * {@link RODAMember} ({@link User} or {@link Group}).
   * </p>
   * 
   * @param memberGroupNames
   *          the memberGroupNames to set
   */
  public void setMemberGroupNames(String[] memberGroupNames) {
    this.memberGroupNames.clear();
    if (memberGroupNames != null) {
      this.memberGroupNames.addAll(Arrays.asList(memberGroupNames));
    }
  }

  /**
   * Returns the member in position <code>index</code>.
   * 
   * @param index
   *          the <code>index</code> number of the members list.
   * @return the name of the member in position <code>index</code>.
   */
  /*
   * public String getMemberName(int index) { return memberUserNames.get(index);
   * }
   */

  /**
   * Adds a new member name to the list of member names.
   * 
   * @param memberUserName
   *          the name of the new member to add.
   * @return true if the member was added, false otherwise.
   */
  public boolean addMemberUser(String memberUserName) {
    return this.memberUserNames.add(memberUserName);
  }

  /**
   * Removes a member name to the list of member names.
   * 
   * @param memberUserName
   *          the name of the member to remove.
   * @return true if the member was removed, false otherwise.
   */
  public boolean removeMemberUser(String memberUserName) {
    return this.memberUserNames.remove(memberUserName);
  }

  /**
   * Adds a new group name to the list of member group names.
   * 
   * @param memberGroupName
   *          the name of the new member group to add.
   * @return true if the member group was added, false otherwise.
   */
  public boolean addMemberGroup(String memberGroupName) {
    return this.memberGroupNames.add(memberGroupName);
  }

  /**
   * Removes a member group name to the list of member group names.
   * 
   * @param memberGroupName
   *          the name of the member group to remove.
   * @return true if the member group was removed, false otherwise.
   */
  public boolean removeMemberGroup(String memberGroupName) {
    return this.memberGroupNames.remove(memberGroupName);
  }

}
