/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 * 
 */
package org.roda.wui.management.editor.client;

import java.util.List;
import java.util.Map;

import org.roda.core.common.BrowserException;
import org.roda.core.common.EditorException;
import org.roda.core.common.IllegalOperationException;
import org.roda.core.common.InvalidDescriptionObjectException;
import org.roda.core.common.LoginException;
import org.roda.core.common.NoSuchRODAObjectException;
import org.roda.core.common.RODAClientException;
import org.roda.core.common.RODAException;
import org.roda.core.common.UserManagementException;
import org.roda.core.data.DescriptionObject;
import org.roda.core.data.RODAObjectUserPermissions;
import org.roda.core.data.eadc.DescriptionLevel;
import org.roda.core.data.v2.RODAMember;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

/**
 * @author Luis Faria
 * 
 */
public interface EditorService extends RemoteService {

  /**
   * Editor service URI
   */
  public static final String SERVICE_URI = "editorservice";

  /**
   * Utilities
   */
  public static class Util {

    /**
     * Get service instance
     * 
     * @return the service instance
     */
    public static EditorServiceAsync getInstance() {

      EditorServiceAsync instance = (EditorServiceAsync) GWT.create(EditorService.class);
      ServiceDefTarget target = (ServiceDefTarget) instance;
      target.setServiceEntryPoint(GWT.getModuleBaseURL() + SERVICE_URI);
      return instance;
    }
  }

  /**
   * Save the modification made in an object
   * 
   * @param editedObject
   *          the changed object
   * @throws RODAException
   */
  public void saveEdition(DescriptionObject editedObject) throws RODAException;

  /**
   * Create a new top level element
   * 
   * @param unitid
   *          the id of the element
   * @return the pid of the new element
   * @throws RODAException
   */
  public String createCollection() throws RODAException;

  /**
   * Create a new sub-element
   * 
   * @param parentPID
   *          the parent of the new element
   * @param parentLevel
   * @param unitid
   *          the id of the new element
   * @param level
   *          the level of the new element
   * @return the pid of the new element
   * @throws IllegalOperationException
   *           if the level is not adequate for sub-element of that parent
   * @throws RODAException
   */
  public String createChild(String parentPID, String parentLevel) throws RODAException;

  /**
   * Create a copy sibling of the element
   * 
   * @param pid
   *          the pid of the element to copy from
   * @return the pid of the new element
   * @throws RODAException
   */
  public String clone(String pid) throws RODAException;

  /**
   * Remove an element from the repository. If the element has children, these
   * will be removed too.
   * 
   * @param pid
   * @throws RODAException
   */
  public void removeElement(String pid) throws RODAException;

  /**
   * Move an element to a different place in the hierarchy
   * 
   * @param pid
   *          the pid of the element to move
   * @param toFatherPid
   *          the pid of the parent that the element should be moved to
   * @throws LoginException
   * @throws NoSuchRODAObjectException
   * @throws IllegalOperationException
   *           if the destination level is not adequate to be parent of the
   *           element to move
   * @throws RODAException
   */
  public void moveElement(String pid, String toFatherPid) throws RODAException;

  /**
   * Get the list of levels that an element can change to. This is useful when
   * editing the level.
   * 
   * @param pid
   *          the pid of the level
   * @return the list of levels
   * @throws RODAException
   */
  public DescriptionLevel[] getPossibleLevels(String pid) throws RODAException;

  /**
   * Get the list of possible values for a designated field
   * 
   * @param field
   *          the name of the field
   * @return the list of possible values for a designated field
   * 
   */
  public String[] getControlledVocabulary(String field);

  /**
   * Get the default value of a field
   * 
   * @param field
   *          the name of the field
   * @return the default value of a field
   */
  public String getDefaultValue(String field);

  /**
   * Get the default description object to be used when creating description
   * objects
   * 
   * @return the default description object
   */
  public DescriptionObject getDefaultDescriptionObject();

  /**
   * Get producers of a fonds
   * 
   * @param fondsPid
   *          the fonds PID
   * @return the list of producers
   * @throws RODAException
   */
  public List<RODAMember> getProducers(String fondsPid) throws RODAException;

  /**
   * Add a producer to a fonds producer list
   * 
   * @param producer
   *          the producer to add
   * @param fondsPid
   *          the fonds PID
   * @return the updated producer list
   * @throws RODAException
   */
  public List<RODAMember> addProducer(RODAMember producer, String fondsPid) throws RODAException;

  /**
   * Remove producer from fonds producer list
   * 
   * @param producer
   *          the producer to remove
   * @param fondsPid
   *          the fonds PID
   * @return the updated producer list
   * @throws RODAException
   */
  public List<RODAMember> removeProducer(RODAMember producer, String fondsPid) throws RODAException;

  /**
   * Remove producers from fonds producer list
   * 
   * @param producerList
   *          list of producers to remove
   * @param fondsPid
   *          the fonds PID
   * @return the updated producer list
   * @throws RODAException
   */
  public List<RODAMember> removeProducers(List<RODAMember> producerList, String fondsPid) throws RODAException;

  /**
   * Get an object permissions
   * 
   * @param pid
   * @return a map of users and their permissions
   * @throws RODAException
   */
  public Map<RODAMember, ObjectPermissions> getObjectPermissions(String pid) throws RODAException;

  /**
   * Set member permissions for an object. If a user already is in permissions,
   * it will be replaced
   * 
   * @param pid
   *          the object PID
   * @param member
   *          the user or group
   * @param permission
   *          the user permission
   * @return the updated permissions
   * @throws RODAException
   */
  public Map<RODAMember, ObjectPermissions> setPermission(String pid, RODAMember member, ObjectPermissions permission)
    throws RODAException;

  /**
   * Apply permissions
   * 
   * @param pid
   * @param permissions
   * @param recursivelly
   * @return the new permissions
   * @throws RODAException
   */
  public Map<RODAMember, ObjectPermissions> setObjectPermissions(String pid,
    Map<RODAMember, ObjectPermissions> permissions, boolean recursivelly) throws RODAException;

  /**
   * Get the permissions of the authenticated user to an object
   * 
   * @param pid
   *          the object PID
   * @return the permissions
   * @throws RODAException
   */
  public RODAObjectUserPermissions getSelfObjectPermissions(String pid) throws RODAException;

}
