package pt.gov.dgarq.roda.wui.management.editor.server;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.common.RodaClientFactory;
import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.common.InvalidDescriptionLevel;
import pt.gov.dgarq.roda.core.common.LoginException;
import pt.gov.dgarq.roda.core.common.RODAClientException;
import pt.gov.dgarq.roda.core.common.RODAException;
import pt.gov.dgarq.roda.core.common.UserManagementException;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.Group;
import pt.gov.dgarq.roda.core.data.Producers;
import pt.gov.dgarq.roda.core.data.RODAMember;
import pt.gov.dgarq.roda.core.data.RODAObjectPermissions;
import pt.gov.dgarq.roda.core.data.RODAObjectUserPermissions;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.core.data.eadc.ArrangementTable;
import pt.gov.dgarq.roda.core.data.eadc.ArrangementTableGroup;
import pt.gov.dgarq.roda.core.data.eadc.ArrangementTableRow;
import pt.gov.dgarq.roda.core.data.eadc.BioghistChronitem;
import pt.gov.dgarq.roda.core.data.eadc.BioghistChronlist;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;
import pt.gov.dgarq.roda.core.data.eadc.EadCValue;
import pt.gov.dgarq.roda.core.data.eadc.PhysdescElement;
import pt.gov.dgarq.roda.core.data.eadc.Text;
import pt.gov.dgarq.roda.core.stubs.Browser;
import pt.gov.dgarq.roda.core.stubs.Editor;
import pt.gov.dgarq.roda.core.stubs.UserBrowser;
import pt.gov.dgarq.roda.wui.management.editor.client.EditorService;
import pt.gov.dgarq.roda.wui.management.editor.client.ObjectPermissions;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Editor service implementation
 * 
 * @author Luis Faria
 * 
 */
public class EditorServiceImpl extends RemoteServiceServlet implements
		EditorService {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(EditorService.class);

	private static final DescriptionObject DEFAULT_DESCRIPTION_OBJECT = new DescriptionObject();
	static {
		Properties elementDefaults = new Properties();
		InputStream relsStream = EditorServiceImpl.class
				.getResourceAsStream("/config/roda-element-defaults.properties");
		try {
			elementDefaults.load(relsStream);
			for (Entry<Object, Object> entry : elementDefaults.entrySet()) {
				DEFAULT_DESCRIPTION_OBJECT.setValue((String) entry.getKey(),
						new Text((String) entry.getValue()));
			}
		} catch (IOException e) {
			logger.error("Error loading element defaults", e);
		}
	}

	public DescriptionObject getDefaultDescriptionObject() {
		return DEFAULT_DESCRIPTION_OBJECT;
	}

	public void saveEdition(DescriptionObject editedObject)
			throws RODAException {
		try {
			Editor editorService = RodaClientFactory.getRodaClient(
					this.getThreadLocalRequest().getSession())
					.getEditorService();
			safeEncode(editedObject);
			editorService.modifyDescriptionObject(editedObject);
		} catch (RemoteException e) {
			logger.error("Remote Exception", e);
			throw RODAClient.parseRemoteException(e);
		}
	}

	private void safeEncode(DescriptionObject object) {
		for (String element : DescriptionObject.getAllElements()) {
			safeEncode(object, element);
		}
	}

	private static final Set<String> skipEncodingElements = new HashSet<String>(
			Arrays.asList(new String[] { DescriptionObject.COMPLETE_REFERENCE,
					DescriptionObject.HANDLE_URL }));

	private void safeEncode(DescriptionObject object, String element) {
		logger.fatal("Safe encoding " + element);
		if (!skipEncodingElements.contains(element)) {

			EadCValue value = object.getValue(element);
			if (value instanceof Text) {
				Text text = (Text) value;
				text.setText(safeEncode(text.getText()));
			} else if (value instanceof PhysdescElement) {
				PhysdescElement physdesc = (PhysdescElement) value;
				physdesc.setValue(safeEncode(physdesc.getValue()));
				physdesc.setUnit(safeEncode(physdesc.getValue()));
			} else if (value instanceof BioghistChronlist) {
				BioghistChronlist chronlist = (BioghistChronlist) value;
				for (BioghistChronitem item : chronlist.getBioghistChronitems()) {
					item.setEvent(safeEncode(item.getEvent()));
				}
			} else if (value instanceof ArrangementTable) {
				ArrangementTable table = (ArrangementTable) value;
				for (ArrangementTableGroup group : table
						.getArrangementTableGroups()) {
					for (ArrangementTableRow row : group.getHead().getRows()) {
						String[] entries = row.getEntries();
						for (int i = 0; i < entries.length; i++) {
							entries[i] = safeEncode(entries[i]);
						}
					}

					for (ArrangementTableRow row : group.getBody().getRows()) {
						String[] entries = row.getEntries();
						for (int i = 0; i < entries.length; i++) {
							entries[i] = safeEncode(entries[i]);
						}
					}
				}
			}

			if (value != null) {
				object.setValue(element, value);
			}
		}
	}

	private String safeEncode(String s) {
		StringBuffer ret = new StringBuffer();

		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			if (ch < 0x20 && ch != 0x9 && ch != 0xD && ch != 0xA) {
				// ignore control characters
				logger.fatal("Ignoring character " + (int) ch);
			} else {
				ret.append(ch);
			}
		}

		return ret.toString();

	}

	public String createCollection() throws RODAException {
		String pid;
		try {
			Editor editorService = RodaClientFactory.getRodaClient(
					this.getThreadLocalRequest().getSession())
					.getEditorService();

			DescriptionObject fonds = DEFAULT_DESCRIPTION_OBJECT;
			fonds.setLevel(DescriptionLevel.FONDS);
			fonds.setParentPID(null);

			pid = editorService.createDescriptionObject(fonds);
		} catch (RemoteException e) {
			logger.error("Remote Exception", e);
			throw RODAClient.parseRemoteException(e);
		}
		return pid;
	}

	public String createChild(String parentPID, DescriptionLevel parentLevel)
			throws RODAException {
		String childPid;
		DescriptionLevel newLevel = null;
		if (parentLevel.equals(new DescriptionLevel(DescriptionLevel.FONDS))) {
			newLevel = new DescriptionLevel(DescriptionLevel.SERIES);
		} else if (parentLevel.equals(new DescriptionLevel(
				DescriptionLevel.SUBFONDS))) {
			newLevel = new DescriptionLevel(DescriptionLevel.SERIES);
		} else if (parentLevel.equals(new DescriptionLevel(
				DescriptionLevel.CLASS))) {
			newLevel = new DescriptionLevel(DescriptionLevel.SERIES);
		} else if (parentLevel.equals(new DescriptionLevel(
				DescriptionLevel.SUBCLASS))) {
			newLevel = new DescriptionLevel(DescriptionLevel.SERIES);
		} else if (parentLevel.equals(new DescriptionLevel(
				DescriptionLevel.SERIES))) {
			newLevel = new DescriptionLevel(DescriptionLevel.ITEM);
		} else if (parentLevel.equals(new DescriptionLevel(
				DescriptionLevel.SUBSERIES))) {
			newLevel = new DescriptionLevel(DescriptionLevel.ITEM);
		} else if (parentLevel.equals(new DescriptionLevel(
				DescriptionLevel.FILE))) {
			newLevel = new DescriptionLevel(DescriptionLevel.ITEM);
		} else {
			new InvalidDescriptionLevel("Cannot create child of " + parentLevel);
		}

		DescriptionObject child = DEFAULT_DESCRIPTION_OBJECT;
		child.setLevel(newLevel);
		child.setParentPID(parentPID);

		try {
			Editor editorService = RodaClientFactory.getRodaClient(
					this.getThreadLocalRequest().getSession())
					.getEditorService();
			childPid = editorService.createDescriptionObject(child);
		} catch (RemoteException e) {
			logger.error("Remote Exception", e);
			throw RODAClient.parseRemoteException(e);
		}
		return childPid;
	}

	public String clone(String pid) throws RODAException {
		String clonePID;
		DescriptionObject object;
		try {
			Browser browser = RodaClientFactory.getRodaClient(
					this.getThreadLocalRequest().getSession())
					.getBrowserService();
			Editor editor = RodaClientFactory.getRodaClient(
					this.getThreadLocalRequest().getSession())
					.getEditorService();

			object = browser.getDescriptionObject(pid);

			// Append "*" to object id
			object.setId(object.getId() + "*");

			clonePID = editor.createDescriptionObject(object);
		} catch (RemoteException e) {
			logger.error("Remote Exception", e);
			throw RODAClient.parseRemoteException(e);
		}
		return clonePID;
	}

	public void moveElement(String pid, String parentPID) throws RODAException {
		DescriptionObject object;
		try {
			Browser browser = RodaClientFactory.getRodaClient(
					this.getThreadLocalRequest().getSession())
					.getBrowserService();
			Editor editor = RodaClientFactory.getRodaClient(
					this.getThreadLocalRequest().getSession())
					.getEditorService();

			object = browser.getDescriptionObject(pid);

			object.setParentPID(parentPID);

			editor.modifyDescriptionObject(object);
		} catch (RemoteException e) {
			logger.error("Remote Exception", e);
			throw RODAClient.parseRemoteException(e);
		}
	}

	public void removeElement(String pid) throws RODAException {
		try {
			Editor editorService = RodaClientFactory.getRodaClient(
					this.getThreadLocalRequest().getSession())
					.getEditorService();
			editorService.removeDescriptionObject(pid);
		} catch (RemoteException e) {
			logger.error("Remote Exception", e);
			throw RODAClient.parseRemoteException(e);
		}
	}

	public DescriptionLevel[] getPossibleLevels(String pid)
			throws RODAException {
		DescriptionLevel[] ret;
		try {
			Editor editorService = RodaClientFactory.getRodaClient(
					this.getThreadLocalRequest().getSession())
					.getEditorService();
			ret = editorService.getDOPossibleLevels(pid);
		} catch (RemoteException e) {
			logger.error("Remote Exception", e);
			throw RODAClient.parseRemoteException(e);
		}
		return ret;
	}

	private static Map<String, String[]> controlledVoc = new HashMap<String, String[]>();

	private static Map<String, String> defaultValue = new HashMap<String, String>();

	static {
		// Country code
		controlledVoc.put(DescriptionObject.COUNTRYCODE, new String[] { "PT",
				"BR", "UK", "USA", "IT", "SP" });
		defaultValue.put(DescriptionObject.COUNTRYCODE, "PT");

		// Languages
		controlledVoc.put(DescriptionObject.LANGMATERIAL_LANGUAGES,
				new String[] { "pt", "en" });
		defaultValue.put(DescriptionObject.LANGMATERIAL_LANGUAGES, "pt");

		// Dimensions units
		controlledVoc.put(DescriptionObject.PHYSDESC_DIMENSIONS + "_unit",
				new String[] { null, "m x m", "cm x cm", "mm x mm", "m", "cm",
						"mm" });
		defaultValue.put(DescriptionObject.PHYSDESC_DIMENSIONS + "_unit", null);

		// PhysFacet units
		controlledVoc.put(DescriptionObject.PHYSDESC_PHYSFACET + "_unit",
				new String[] { null, "pages", "tables", "photos" });
		defaultValue.put(DescriptionObject.PHYSDESC_PHYSFACET + "_unit", null);

		// Extent units
		controlledVoc.put(DescriptionObject.PHYSDESC_EXTENT + "_unit",
				new String[] { null, "B", "KB", "MB", "GB", "TB" });
		defaultValue.put(DescriptionObject.PHYSDESC_EXTENT + "_unit", null);

	}

	public String[] getControlledVocabulary(String field) {
		return (String[]) controlledVoc.get(field);
	}

	public String getDefaultValue(String field) {
		return (String) defaultValue.get(field);
	}

	private List<RODAMember> translateProducers(Producers producers)
			throws LoginException, RODAClientException,
			UserManagementException, RemoteException {
		List<RODAMember> producerList = new ArrayList<RODAMember>();
		UserBrowser userBrowser = RodaClientFactory.getRodaClient(
				this.getThreadLocalRequest().getSession())
				.getUserBrowserService();
		for (String username : producers.getUsers()) {
			User user = userBrowser.getUser(username);
			producerList.add(user);
		}

		for (String groupname : producers.getGroups()) {
			Group group = userBrowser.getGroup(groupname);
			producerList.add(group);
		}

		return producerList;
	}

	private Producers untranslateProducers(List<RODAMember> producerList,
			String pid) {
		Producers producers = new Producers();
		producers.setDescriptionObjectPID(pid);

		List<String> usernames = new ArrayList<String>();
		List<String> groupnames = new ArrayList<String>();

		for (RODAMember producer : producerList) {
			if (producer instanceof User) {
				usernames.add(producer.getName());

			} else if (producer instanceof Group) {
				groupnames.add(producer.getName());
			}
		}

		producers.addUsers(usernames.toArray(new String[] {}));
		producers.addGroups(groupnames.toArray(new String[] {}));

		return producers;

	}

	/**
	 * Get producers of a fonds
	 * 
	 * @param fondsPid
	 *            the fonds PID
	 * @return the list of producers
	 * @throws RODAException
	 */
	public List<RODAMember> getProducers(String fondsPid) throws RODAException {

		List<RODAMember> producerList = new ArrayList<RODAMember>();
		Browser browserService = RodaClientFactory.getRodaClient(
				this.getThreadLocalRequest().getSession()).getBrowserService();
		try {
			Producers producers = browserService.getProducers(fondsPid);
			producerList = translateProducers(producers);

		} catch (RemoteException e) {
			logger.error("Remote Exception", e);
			throw RODAClient.parseRemoteException(e);
		}
		return producerList;
	}

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
	public List<RODAMember> addProducer(RODAMember producer, String fondsPid)
			throws RODAException {
		// Get current producers
		List<RODAMember> fondsProducers = getProducers(fondsPid);

		if (!fondsProducers.contains(producer)) {
			// check if fondsProducers contains any super-group of producer
			boolean containsSuperGroup = false;
			for (String superGroupName : producer.getAllGroups()) {
				Group superGroup = new Group(superGroupName);
				if (fondsProducers.contains(superGroup)) {
					containsSuperGroup = true;
					break;
				}
			}

			if (!containsSuperGroup) {
				fondsProducers.add(producer);

				if (producer instanceof Group) {
					// remove any member of this group from list
					Group group = (Group) producer;
					List<RODAMember> membersToRemove = new ArrayList<RODAMember>();
					for (RODAMember member : fondsProducers) {
						if (Arrays.asList(member.getAllGroups()).contains(
								group.getName())) {
							membersToRemove.add(member);
						}
					}
					fondsProducers.removeAll(membersToRemove);
				}

				Producers producers = untranslateProducers(fondsProducers,
						fondsPid);

				try {
					RodaClientFactory.getRodaClient(
							this.getThreadLocalRequest().getSession())
							.getEditorService().setProducers(fondsPid,
									producers);
				} catch (RemoteException e) {
					logger.error("Remote Exception", e);
					throw RODAClient.parseRemoteException(e);
				}

			} else {
				logger.info("producer already included in a producing group");
				// TODO send exception
			}

		} else {
			logger.info("producer already added");
			// TODO send exception
		}

		return fondsProducers;
	}

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
	public List<RODAMember> removeProducer(RODAMember producer, String fondsPid)
			throws RODAException {
		// Get current producers
		List<RODAMember> fondsProducers = getProducers(fondsPid);

		if (fondsProducers.contains(producer)) {
			fondsProducers.remove(producer);

			Producers producers = untranslateProducers(fondsProducers, fondsPid);

			try {
				RodaClientFactory.getRodaClient(
						this.getThreadLocalRequest().getSession())
						.getEditorService().setProducers(fondsPid, producers);
			} catch (RemoteException e) {
				logger.error("Remote Exception", e);
				throw RODAClient.parseRemoteException(e);
			}
		} else {
			logger.info("cannot remove producer because it is not in list");
			// TODO send exception
		}

		return fondsProducers;
	}

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
	public List<RODAMember> removeProducers(List<RODAMember> producerList,
			String fondsPid) throws RODAException {
		// Get current producers
		List<RODAMember> fondsProducers = getProducers(fondsPid);

		fondsProducers.removeAll(producerList);

		Producers producers = untranslateProducers(fondsProducers, fondsPid);

		try {
			RodaClientFactory.getRodaClient(
					this.getThreadLocalRequest().getSession())
					.getEditorService().setProducers(fondsPid, producers);
		} catch (RemoteException e) {
			logger.error("Remote Exception", e);
			throw RODAClient.parseRemoteException(e);
		}

		return fondsProducers;
	}

	private Map<RODAMember, ObjectPermissions> translatePermissions(
			RODAObjectPermissions rodaPermissions) throws LoginException,
			RODAClientException, UserManagementException, RemoteException {
		Map<RODAMember, ObjectPermissions> permissions = new LinkedHashMap<RODAMember, ObjectPermissions>();
		UserBrowser userBrowser = RodaClientFactory.getRodaClient(
				this.getThreadLocalRequest().getSession())
				.getUserBrowserService();

		// All user names defined in permissions
		Set<String> usernames = new TreeSet<String>();

		// Put user permissions into lists
		List<String> readUsersList = Arrays.asList(rodaPermissions
				.getReadUsers());
		List<String> modifyUsersList = Arrays.asList(rodaPermissions
				.getModifyUsers());
		List<String> removeUsersList = Arrays.asList(rodaPermissions
				.getRemoveUsers());
		List<String> grantUsersList = Arrays.asList(rodaPermissions
				.getGrantUsers());

		// Add all users to user name list
		usernames.addAll(readUsersList);
		usernames.addAll(modifyUsersList);
		usernames.addAll(removeUsersList);
		usernames.addAll(grantUsersList);

		// All group names defined in permissions
		Set<String> groupnames = new TreeSet<String>();

		// Put group permissions into lists
		List<String> readGroupsList = Arrays.asList(rodaPermissions
				.getReadGroups());
		List<String> modifyGroupsList = Arrays.asList(rodaPermissions
				.getModifyGroups());
		List<String> removeGroupsList = Arrays.asList(rodaPermissions
				.getRemoveGroups());
		List<String> grantGroupsList = Arrays.asList(rodaPermissions
				.getGrantGroups());

		// Add all groups to group name list
		groupnames.addAll(readGroupsList);
		groupnames.addAll(modifyGroupsList);
		groupnames.addAll(removeGroupsList);
		groupnames.addAll(grantGroupsList);

		// Process all users
		for (String username : usernames) {

			User user = userBrowser.getUser(username);

			// Get all groups that this user belongs to
			String[] allGroups = user.getAllGroups();

			// Intersect all user groups with each groups permissions
			List<String> readAllUserGroups = new ArrayList<String>(Arrays
					.asList(allGroups));
			readAllUserGroups.retainAll(readGroupsList);
			List<String> modifyAllUserGroups = new ArrayList<String>(Arrays
					.asList(allGroups));
			modifyAllUserGroups.retainAll(modifyGroupsList);
			List<String> removeAllUserGroups = new ArrayList<String>(Arrays
					.asList(allGroups));
			removeAllUserGroups.retainAll(removeGroupsList);
			List<String> grantAllUserGroups = new ArrayList<String>(Arrays
					.asList(allGroups));
			grantAllUserGroups.retainAll(grantGroupsList);

			// Calculate permissions
			boolean canRead = readUsersList.contains(username)
					|| readAllUserGroups.size() > 0;
			boolean canModify = modifyUsersList.contains(username)
					|| modifyAllUserGroups.size() > 0;
			boolean canRemove = removeUsersList.contains(username)
					|| removeAllUserGroups.size() > 0;
			boolean canGrant = grantUsersList.contains(username)
					|| grantAllUserGroups.size() > 0;

			// Create object permissions and add to map
			ObjectPermissions objPermissions = new ObjectPermissions(canRead,
					canModify, canRemove, canGrant);
			permissions.put(user, objPermissions);
		}

		// Process all groups
		for (String groupname : groupnames) {
			Group group = userBrowser.getGroup(groupname);

			// Get all groups that this group belongs to
			String[] allGroups = group.getAllGroups();

			// Intersect all group groups with each groups permissions
			List<String> readAllGroupGroups = new ArrayList<String>(Arrays
					.asList(allGroups));
			readAllGroupGroups.retainAll(readGroupsList);
			List<String> modifyAllGroupGroups = new ArrayList<String>(Arrays
					.asList(allGroups));
			modifyAllGroupGroups.retainAll(readGroupsList);
			List<String> removeAllGroupGroups = new ArrayList<String>(Arrays
					.asList(allGroups));
			removeAllGroupGroups.retainAll(readGroupsList);
			List<String> grantAllGroupGroups = new ArrayList<String>(Arrays
					.asList(allGroups));
			grantAllGroupGroups.retainAll(readGroupsList);

			// Calculate permissions
			boolean canRead = readGroupsList.contains(groupname)
					|| readAllGroupGroups.size() > 0;
			boolean canModify = modifyGroupsList.contains(groupname)
					|| modifyAllGroupGroups.size() > 0;
			boolean canRemove = removeGroupsList.contains(groupname)
					|| removeAllGroupGroups.size() > 0;
			boolean canGrant = grantGroupsList.contains(groupname)
					|| grantAllGroupGroups.size() > 0;

			// Create object permissions and add to map
			ObjectPermissions objPermissions = new ObjectPermissions(canRead,
					canModify, canRemove, canGrant);
			permissions.put(group, objPermissions);
		}

		return permissions;
	}

	private RODAObjectPermissions untranslatePermissions(
			Map<RODAMember, ObjectPermissions> permissions, String pid) {

		// Create new RODA permissions
		RODAObjectPermissions rodaPermissions = new RODAObjectPermissions();

		// Create lists for all user permissions
		List<String> readUsersList = new ArrayList<String>();
		List<String> modifyUsersList = new ArrayList<String>();
		List<String> removeUsersList = new ArrayList<String>();
		List<String> grantUsersList = new ArrayList<String>();

		// Create lists for all group permissions
		List<String> readGroupsList = new ArrayList<String>();
		List<String> modifyGroupsList = new ArrayList<String>();
		List<String> removeGroupsList = new ArrayList<String>();
		List<String> grantGroupsList = new ArrayList<String>();

		// Populate user and group permissions lists
		for (Entry<RODAMember, ObjectPermissions> entry : permissions
				.entrySet()) {
			ObjectPermissions permission = entry.getValue();
			if (entry.getKey() instanceof User) {
				User user = (User) entry.getKey();
				if (permission.isRead()) {
					readUsersList.add(user.getName());
				}
				if (permission.isEditMetadata()) {
					modifyUsersList.add(user.getName());
				}
				if (permission.isRemove()) {
					removeUsersList.add(user.getName());
				}
				if (permission.isGrant()) {
					grantUsersList.add(user.getName());
				}

			} else if (entry.getKey() instanceof Group) {
				Group group = (Group) entry.getKey();
				if (permission.isRead()) {
					readGroupsList.add(group.getName());
				}
				if (permission.isEditMetadata()) {
					modifyGroupsList.add(group.getName());
				}
				if (permission.isRemove()) {
					removeGroupsList.add(group.getName());
				}
				if (permission.isGrant()) {
					grantGroupsList.add(group.getName());
				}
			}
		}

		// Add all permissions lists to RODA permissions
		rodaPermissions.addReadUsers(readUsersList.toArray(new String[] {}));
		rodaPermissions
				.addModifyUsers(modifyUsersList.toArray(new String[] {}));
		rodaPermissions
				.addRemoveUsers(removeUsersList.toArray(new String[] {}));
		rodaPermissions.addGrantUsers(grantUsersList.toArray(new String[] {}));

		rodaPermissions.addReadGroups(readGroupsList.toArray(new String[] {}));
		rodaPermissions.addModifyGroups(modifyGroupsList
				.toArray(new String[] {}));
		rodaPermissions.addRemoveGroups(removeGroupsList
				.toArray(new String[] {}));
		rodaPermissions
				.addGrantGroups(grantGroupsList.toArray(new String[] {}));

		// Set RODA permissions object PID
		rodaPermissions.setObjectPID(pid);

		return rodaPermissions;
	}

	/**
	 * Get an object permissions
	 * 
	 * @param pid
	 * @return a map of users and their permissions
	 * @throws RODAException
	 */
	public Map<RODAMember, ObjectPermissions> getObjectPermissions(String pid)
			throws RODAException {
		Map<RODAMember, ObjectPermissions> ret;
		Browser browserService = RodaClientFactory.getRodaClient(
				this.getThreadLocalRequest().getSession()).getBrowserService();
		try {
			RODAObjectPermissions rodaPermissions = browserService
					.getRODAObjectPermissions(pid);
			ret = translatePermissions(rodaPermissions);
		} catch (RemoteException e) {
			logger.error("Remote Exception", e);
			throw RODAClient.parseRemoteException(e);
		}
		return ret;
	}

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
	public Map<RODAMember, ObjectPermissions> setPermission(String pid,
			RODAMember member, ObjectPermissions permission)
			throws RODAException {
		Map<RODAMember, ObjectPermissions> permissions = getObjectPermissions(pid);
		permissions.put(member, permission);
		RODAObjectPermissions rodaPermissions = untranslatePermissions(
				permissions, pid);
		RODAObjectPermissions newRodaPermissions;
		Map<RODAMember, ObjectPermissions> newPermissions;
		try {
			newRodaPermissions = RodaClientFactory.getRodaClient(
					this.getThreadLocalRequest().getSession())
					.getEditorService().setRODAObjectPermissions(
							rodaPermissions, false);
			newPermissions = translatePermissions(newRodaPermissions);
		} catch (RemoteException e) {
			logger.error("Remote Exception", e);
			throw RODAClient.parseRemoteException(e);
		}

		return newPermissions;
	}

	public Map<RODAMember, ObjectPermissions> setObjectPermissions(String pid,
			Map<RODAMember, ObjectPermissions> permissions, boolean recursivelly)
			throws RODAException {
		RODAObjectPermissions rodaPermissions = untranslatePermissions(
				permissions, pid);
		RODAObjectPermissions newRodaPermissions;
		Map<RODAMember, ObjectPermissions> newPermissions;
		try {
			newRodaPermissions = RodaClientFactory.getRodaClient(
					this.getThreadLocalRequest().getSession())
					.getEditorService().setRODAObjectPermissions(
							rodaPermissions, recursivelly);
			newPermissions = translatePermissions(newRodaPermissions);
		} catch (RemoteException e) {
			logger.error("Remote Exception", e);
			throw RODAClient.parseRemoteException(e);
		}

		return newPermissions;
	}

	public RODAObjectUserPermissions getSelfObjectPermissions(String pid)
			throws RODAException {
		try {
			return RodaClientFactory.getRodaClient(
					this.getThreadLocalRequest().getSession())
					.getBrowserService().getRODAObjectUserPermissions(pid);
		} catch (RemoteException e) {
			logger.error("Remote Exception", e);
			throw RODAClient.parseRemoteException(e);
		}
	}

}
