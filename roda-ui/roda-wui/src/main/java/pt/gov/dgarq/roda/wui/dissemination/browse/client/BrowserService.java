/**
 *
 */
package pt.gov.dgarq.roda.wui.dissemination.browse.client;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

import pt.gov.dgarq.roda.core.common.RODAException;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.RepresentationPreservationObject;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.v2.SimpleDescriptionObject;

/**
 * @author Luis Faria
 * @author Vladislav Korecký <vladislav_korecky@gordic.cz>
 */
public interface BrowserService extends RemoteService {

	/**
	 * Service location
	 */
	public static final String SERVICE_URI = "browserservice";

	/**
	 * Utilities
	 *
	 */
	public static class Util {

		/**
		 * Get singleton instance
		 *
		 * @return the instance
		 */
		public static BrowserServiceAsync getInstance() {

			BrowserServiceAsync instance = (BrowserServiceAsync) GWT.create(BrowserService.class);
			ServiceDefTarget target = (ServiceDefTarget) instance;
			target.setServiceEntryPoint(GWT.getModuleBaseURL() + SERVICE_URI);
			return instance;
		}
	}

	/**
	 * Get collection total count
	 *
	 * @param filter
	 *
	 * @return the number of collections that fit the filter
	 * @throws RODAException
	 */
	public Integer getCollectionsCount(Filter filter) throws RODAException;

	/**
	 * Get collections
	 *
	 * @param adapter
	 *
	 * @return get collections
	 * @throws RODAException
	 */
	public SimpleDescriptionObject[] getCollections(Filter filter, Sorter sorter, Sublist sublist) throws RODAException;

	/**
	 * Get sub elements count
	 *
	 * @param pid
	 *            the parent pid
	 * @param filter
	 * @return get sub elements count
	 * @throws RODAException
	 */
	public Integer getSubElementsCount(String pid, Filter filter) throws RODAException;

	/**
	 * Get sub elements
	 *
	 * @param pid
	 *            the parent pid
	 * @param adapter
	 * @return the sub elements
	 * @throws RODAException
	 */
	public SimpleDescriptionObject[] getSubElements(String pid, Filter filter, Sorter sorter, Sublist sublist)
			throws RODAException;

	/**
	 * Get a RODA object
	 *
	 * @param pid
	 *            the object id
	 * @return {@link RODAObject}
	 * @throws RODAException
	 */
	// public RODAObject getRODAObject(String pid) throws RODAException;

	/**
	 * Get simple description object
	 *
	 * @param pid
	 *            the object id
	 * @return {@link SimpleDescriptionObject}
	 * @throws RODAException
	 */
	public SimpleDescriptionObject getSimpleDescriptionObject(String pid) throws RODAException;

	/**
	 * Get description object
	 *
	 * @param pid
	 *            the object id
	 * @return {@link DescriptionObject}
	 * @throws RODAException
	 */
	public DescriptionObject getDescriptionObject(String pid) throws RODAException;

	/**
	 * Get the elements parent
	 *
	 * @param pid
	 *            the elements pid
	 * @return the parent pid, or null if none
	 * @throws RODAException
	 */
	public String getParent(String pid) throws RODAException;

	/**
	 * Get the pid of all ancestors of the node.
	 *
	 * @param pid
	 *            the pid of the node
	 * @return A array that starts in the fonds of witch this node belongs to,
	 *         and ends in the node itself
	 * @throws RODAException
	 */
	public String[] getAncestors(String pid) throws RODAException;

	/**
	 * Get the index of a collection
	 *
	 * @param collectionPID
	 *            the collection id
	 * @param filter
	 * @param sorter
	 * @return the index of the collection
	 * @throws RODAException
	 */
	// public Integer getCollectionIndex(String collectionPID, Filter filter,
	// Sorter sorter) throws RODAException;

	/**
	 * Get an item index
	 *
	 * @param parentPID
	 *            the parent pid
	 * @param childPID
	 *            the item pid
	 * @param filter
	 * @param sorter
	 * @return the item index
	 * @throws RODAException
	 */
	// public Integer getItemIndex(String parentPID, String childPID, Filter
	// filter, Sorter sorter) throws RODAException;

	/**
	 * get sub elements
	 *
	 * @param pid
	 *            the parent id
	 * @param focusOnChild
	 *            the pid of the first item to fetch
	 * @param count
	 *            the maximum number of items to fetch
	 * @param filter
	 * @param sorter
	 * @return the sub elements list
	 * @throws RODAException
	 */
	// public SimpleDescriptionObject[] getSubElements(String pid, String
	// focusOnChild, int count, Filter filter,
	// Sorter sorter) throws RODAException;

	/**
	 * Get representations information
	 *
	 * @param pid
	 *            the id of the associated description object
	 * @return the list of representation informations
	 * @throws RODAException
	 */
	public List<RepresentationInfo> getRepresentationsInfo(String doPID) throws RODAException;

	/**
	 * Get the Representation Preservation Objects associated with a Descriptive
	 * Object
	 *
	 * @param doPID
	 *            the Description Object PID
	 * @return The list of associated Representation Preservation Objects
	 * @throws RODAException
	 */
	public List<RepresentationPreservationObject> getDOPreservationObjects(String doPID) throws RODAException;

	/**
	 * Get the preservation information
	 *
	 * @param doPID
	 *            the PID of the associated description object
	 * @return A list of preservations information
	 * @throws RODAException
	 */
	public List<PreservationInfo> getPreservationsInfo(String doPID) throws RODAException;

	/**
	 * Get preservation timeline info
	 *
	 * @param repPIDs
	 *            the PIDs of the representations to show
	 * @param icons
	 *            the icons to use in each representation, by order
	 * @param colors
	 *            the colors to use in each representation, by order
	 * @param locale
	 * @return {@link TimelineInfo}
	 * @throws RODAException
	 */
	public TimelineInfo getPreservationTimeline(List<String> repPIDs, List<String> icons, List<String> colors,
			String locale) throws RODAException;
}
