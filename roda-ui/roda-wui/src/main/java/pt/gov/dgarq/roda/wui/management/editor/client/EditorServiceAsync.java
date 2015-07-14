/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.editor.client;

import java.util.List;
import java.util.Map;

import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.RODAMember;
import pt.gov.dgarq.roda.core.data.RODAObjectUserPermissions;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Luis Faria
 * 
 */
public interface EditorServiceAsync {

	/**
	 * Save the modification made in an object
	 * 
	 * @param editedObject
	 *            the changed object
	 * @throws RODAException
	 */
	public void saveEdition(DescriptionObject editedObject, AsyncCallback<Void> callback);

	/**
	 * Create a new top level element
	 * 
	 * @param unitid
	 *            the id of the element
	 * @return the pid of the new element
	 * @throws RODAException
	 */
	public void createCollection(AsyncCallback<String> callback);

	/**
	 * Create a new sub-element
	 * 
	 * @param parentPID
	 *            the parent of the new element
	 * @param parentLevel
	 * @param unitid
	 *            the id of the new element
	 * @param level
	 *            the level of the new element
	 * @return the pid of the new element
	 * @throws IllegalOperationException
	 *             if the level is not adequate for sub-element of that parent
	 * @throws RODAException
	 */
	public void createChild(String parentPID, String parentLevel, AsyncCallback<String> callback);

	/**
	 * Create a copy sibling of the element
	 * 
	 * @param pid
	 *            the pid of the element to copy from
	 * @return the pid of the new element
	 * @throws RODAException
	 */
	public void clone(String pid, AsyncCallback<String> callback);

	/**
	 * Remove an element from the repository. If the element has children, these
	 * will be removed too.
	 * 
	 * @param pid
	 * @throws RODAException
	 */
	public void removeElement(String pid, AsyncCallback<Void> callback);

	/**
	 * Move an element to a different place in the hierarchy
	 * 
	 * @param pid
	 *            the pid of the element to move
	 * @param toFatherPid
	 *            the pid of the parent that the element should be moved to
	 * @throws LoginException
	 * @throws NoSuchRODAObjectException
	 * @throws IllegalOperationException
	 *             if the destination level is not adequate to be parent of the
	 *             element to move
	 * @throws RODAException 
	 */
	public void moveElement(String pid, String toFatherPid, AsyncCallback<Void> callback);

	/**
	 * Get the list of levels that an element can change to. This is useful when
	 * editing the level.
	 * 
	 * @param pid
	 *            the pid of the level
	 * @return the list of levels
	 * @throws RODAException
	 */
	public void getPossibleLevels(String pid, AsyncCallback<DescriptionLevel[]> callback);

	/**
	 * Get the list of possible values for a designated field
	 * 
	 * @param field
	 *            the name of the field
	 * @return the list of possible values for a designated field
	 * 
	 */
	public void getControlledVocabulary(String field, AsyncCallback<String[]> callback);

	/**
	 * Get the default value of a field
	 * 
	 * @param field
	 *            the name of the field
	 * @return the default value of a field
	 */
	public void getDefaultValue(String field, AsyncCallback<String> callback);

	/**
	 * Get the default description object to be used when creating description
	 * objects
	 * 
	 * @return the default description object
	 */
	public void getDefaultDescriptionObject(AsyncCallback<DescriptionObject> callback);

	/**
	 * Get producers of a fonds
	 * 
	 * @param fondsPid
	 *            the fonds PID
	 * @return the list of producers
	 * @throws RODAException
	 */
	public void getProducers(String fondsPid, AsyncCallback<List<RODAMember>> callback);

	/**
	 * Add a producer to a fonds producer list
	 * 
	 * @param producer
	 *            the producer to add
	 * @param fondsPid
	 *            the fonds PID
	 * @return the updated producer list
	 * @throws RODAException
	 */
	public void addProducer(RODAMember producer, String fondsPid, AsyncCallback<List<RODAMember>> callback);

	/**
	 * Remove producer from fonds producer list
	 * 
	 * @param producer
	 *            the producer to remove
	 * @param fondsPid
	 *            the fonds PID
	 * @return the updated producer list
	 * @throws RODAException
	 */
	public void removeProducer(RODAMember producer, String fondsPid, AsyncCallback<List<RODAMember>> callback);

	/**
	 * Remove producers from fonds producer list
	 * 
	 * @param producerList
	 *            list of producers to remove
	 * @param fondsPid
	 *            the fonds PID
	 * @return the updated producer list
	 * @throws RODAException
	 */
	public void removeProducers(List<RODAMember> producerList,
			String fondsPid, AsyncCallback<List<RODAMember>> callback);

	/**
	 * Get an object permissions
	 * 
	 * @param pid
	 * @return a map of users and their permissions
	 * @throws RODAException
	 */
	public void getObjectPermissions(String pid, AsyncCallback<Map<RODAMember, ObjectPermissions>> callback);

	/**
	 * Set member permissions for an object. If a user already is in
	 * permissions, it will be replaced
	 * 
	 * @param pid
	 *            the object PID
	 * @param member
	 *            the user or group
	 * @param permission
	 *            the user permission
	 * @return the updated permissions
	 * @throws RODAException
	 */
	public void setPermission(String pid,
			RODAMember member, ObjectPermissions permission, AsyncCallback<Map<RODAMember, ObjectPermissions>> callback);

	/**
	 * Apply permissions
	 * 
	 * @param pid
	 * @param permissions
	 * @param recursivelly
	 * @return the new permissions
	 * @throws RODAException
	 */
	public void setObjectPermissions(String pid,
			Map<RODAMember, ObjectPermissions> permissions, boolean recursivelly, AsyncCallback<Map<RODAMember, ObjectPermissions>> callback);

	/**
	 * Get the permissions of the authenticated user to an object
	 * 
	 * @param pid
	 *            the object PID
	 * @return the permissions
	 * @throws RODAException
	 */
	public void getSelfObjectPermissions(String pid, AsyncCallback<RODAObjectUserPermissions> callback);

}
