package org.roda.core.index.schema.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.roda.core.index.schema.AbstractSolrCollection;
import org.roda.core.index.schema.CopyField;
import org.roda.core.index.schema.Field;
import org.roda.core.index.schema.SolrCollection;
import org.roda.core.index.utils.SolrUtils;

public class MemberCollection extends AbstractSolrCollection<RODAMember, RODAMember> {

  // private static final Logger LOGGER =
  // LoggerFactory.getLogger(MemberCollection.class);

  @Override
  public Class<RODAMember> getIndexClass() {
    return RODAMember.class;
  }

  @Override
  public Class<RODAMember> getModelClass() {
    return RODAMember.class;
  }

  @Override
  public String getIndexName() {
    return RodaConstants.INDEX_MEMBERS;
  }

  @Override
  public List<String> getCommitIndexNames() {
    return Arrays.asList(RodaConstants.INDEX_MEMBERS);
  }

  @Override
  public String getUniqueId(RODAMember modelObject) {
    return modelObject.getUUID();
  }

  @Override
  public List<Field> getFields() {
    List<Field> fields = new ArrayList<>(super.getFields());

    fields.add(new Field(RodaConstants.MEMBERS_NAME, Field.TYPE_STRING).setRequired(true));
    fields.add(new Field(RodaConstants.MEMBERS_IS_ACTIVE, Field.TYPE_BOOLEAN).setRequired(true));
    fields.add(new Field(RodaConstants.MEMBERS_IS_USER, Field.TYPE_BOOLEAN).setRequired(true));
    fields.add(new Field(RodaConstants.MEMBERS_GROUPS, Field.TYPE_STRING).setMultiValued(true));
    fields.add(new Field(RodaConstants.MEMBERS_USERS, Field.TYPE_STRING).setMultiValued(true));
    fields.add(new Field(RodaConstants.MEMBERS_ROLES_DIRECT, Field.TYPE_STRING).setMultiValued(true));
    fields.add(new Field(RodaConstants.MEMBERS_ROLES_ALL, Field.TYPE_STRING).setMultiValued(true));
    fields.add(new Field(RodaConstants.MEMBERS_FULLNAME, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.MEMBERS_EMAIL, Field.TYPE_STRING));

    return fields;
  }

  @Override
  public List<CopyField> getCopyFields() {
    return Arrays.asList(SolrCollection.getCopyAllToSearchField());
  }

  @Override
  public SolrInputDocument toSolrDocument(RODAMember member, Map<String, Object> preCalculatedFields,
    Map<String, Object> accumulators, Flags... flags)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    SolrInputDocument doc = super.toSolrDocument(member, preCalculatedFields, accumulators, flags);

    doc.addField(RodaConstants.MEMBERS_IS_ACTIVE, member.isActive());
    doc.addField(RodaConstants.MEMBERS_IS_USER, member.isUser());
    doc.addField(RodaConstants.MEMBERS_NAME, member.getName());

    if (member.getDirectRoles() != null) {
      doc.addField(RodaConstants.MEMBERS_ROLES_DIRECT, new ArrayList<>(member.getDirectRoles()));
    }
    if (member.getAllRoles() != null) {
      doc.addField(RodaConstants.MEMBERS_ROLES_ALL, new ArrayList<>(member.getAllRoles()));
    }

    if (StringUtils.isNotBlank(member.getFullName())) {
      doc.addField(RodaConstants.MEMBERS_FULLNAME, member.getFullName());
    }

    // Add user specific fields
    if (member instanceof User) {
      User user = (User) member;
      doc.addField(RodaConstants.MEMBERS_EMAIL, user.getEmail());
      if (user.getGroups() != null) {
        doc.addField(RodaConstants.MEMBERS_GROUPS, new ArrayList<>(user.getGroups()));
      }
    }

    // Add group specific fields
    if (member instanceof Group) {
      Group group = (Group) member;
      if (group.getUsers() != null) {
        doc.addField(RodaConstants.MEMBERS_USERS, new ArrayList<>(group.getUsers()));
      }
    }

    return doc;
  }

  @Override
  public RODAMember fromSolrDocument(SolrDocument doc, List<String> fieldsToReturn) throws GenericException {

    // Cannot use super class method because the RODAMember is an interface with
    // an implementation depending on the isUser field

    final String id = SolrUtils.objectToString(doc.get(RodaConstants.INDEX_ID), null);
    final String name = SolrUtils.objectToString(doc.get(RodaConstants.MEMBERS_NAME), null);

    final boolean isActive = SolrUtils.objectToBoolean(doc.get(RodaConstants.MEMBERS_IS_ACTIVE), Boolean.FALSE);
    final boolean isUser = SolrUtils.objectToBoolean(doc.get(RodaConstants.MEMBERS_IS_USER), Boolean.FALSE);
    final String fullName = SolrUtils.objectToString(doc.get(RodaConstants.MEMBERS_FULLNAME), null);

    final String email = SolrUtils.objectToString(doc.get(RodaConstants.MEMBERS_EMAIL), null);
    final Set<String> groups = new HashSet<>(SolrUtils.objectToListString(doc.get(RodaConstants.MEMBERS_GROUPS)));
    final Set<String> users = new HashSet<>(SolrUtils.objectToListString(doc.get(RodaConstants.MEMBERS_USERS)));
    final Set<String> directRoles = new HashSet<>(
      SolrUtils.objectToListString(doc.get(RodaConstants.MEMBERS_ROLES_DIRECT)));
    final Set<String> allRoles = new HashSet<>(SolrUtils.objectToListString(doc.get(RodaConstants.MEMBERS_ROLES_ALL)));

    if (isUser) {
      User user = new User();
      user.setId(id);
      user.setName(name);

      user.setActive(isActive);
      user.setFullName(fullName);
      user.setDirectRoles(directRoles);
      user.setAllRoles(allRoles);

      user.setEmail(email);
      user.setGroups(groups);

      return user;
    } else {
      Group group = new Group();
      group.setId(id);
      group.setName(name);

      group.setActive(isActive);
      group.setFullName(fullName);
      group.setDirectRoles(directRoles);
      group.setAllRoles(allRoles);

      group.setUsers(users);

      return group;
    }

  }

}
