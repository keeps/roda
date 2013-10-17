/**
 *
 */
package pt.gov.dgarq.roda.wui.dissemination.browse.client;

import java.util.List;

import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.RODAObject;
import pt.gov.dgarq.roda.core.data.RepresentationPreservationObject;
import pt.gov.dgarq.roda.core.data.SimpleDescriptionObject;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Luis Faria
 * @author Vladislav Korecký <vladislav_korecky@gordic.cz>
 */
public interface BrowserServiceAsync {

    /**
     * Get collection total count
     *
     * @param filter
     *
     * @return the number of collections that fit the filter
     * @throws RODAException
     */
    public void getCollectionsCount(Filter filter, AsyncCallback<Integer> callback);

    /**
     * Get collections
     *
     * @param adapter
     *
     * @return get collections
     * @throws RODAException
     */
    public void getCollections(ContentAdapter adapter, AsyncCallback<SimpleDescriptionObject[]> callback);

    /**
     * Get sub elements count
     *
     * @param pid the parent pid
     * @param filter
     * @return get sub elements count
     * @throws RODAException
     */
    public void getSubElementsCount(String pid, Filter filter, AsyncCallback<Integer> callback);

    /**
     * Get sub elements
     *
     * @param pid the parent pid
     * @param adapter
     * @return the sub elements
     * @throws RODAException
     */
    public void getSubElements(String pid,
            ContentAdapter adapter, AsyncCallback<SimpleDescriptionObject[]> callback);

    /**
     * Get a RODA object
     *
     * @param pid the object id
     * @return {@link RODAObject}
     * @throws RODAException
     */
    public void getRODAObject(String pid, AsyncCallback<RODAObject> callback);

    /**
     * Get simple description object
     *
     * @param pid the object id
     * @return {@link SimpleDescriptionObject}
     * @throws RODAException
     */
    public void getSimpleDescriptionObject(String pid, AsyncCallback<SimpleDescriptionObject> callback);

    /**
     * Get description object
     *
     * @param pid the object id
     * @return {@link DescriptionObject}
     * @throws RODAException
     */
    public void getDescriptionObject(String pid, AsyncCallback<DescriptionObject> callback);

    /**
     * Get the elements parent
     *
     * @param pid the elements pid
     * @return the parent pid, or null if none
     * @throws RODAException
     */
    public void getParent(String pid, AsyncCallback<String> callback);

    /**
     * Get the pid of all ancestors of the node.
     *
     * @param pid the pid of the node
     * @return A array that starts in the fonds of witch this node belongs to,
     * and ends in the node itself
     * @throws RODAException
     */
    public void getAncestors(String pid, AsyncCallback<String[]> callback);

    /**
     * Get the index of a collection
     *
     * @param collectionPID the collection id
     * @param filter
     * @param sorter
     * @return the index of the collection
     * @throws RODAException
     */
    public void getCollectionIndex(String collectionPID, Filter filter,
            Sorter sorter, AsyncCallback<Integer> callback);

    /**
     * Get an item index
     *
     * @param parentPID the parent pid
     * @param childPID the item pid
     * @param filter
     * @param sorter
     * @return the item index
     * @throws RODAException
     */
    public void getItemIndex(String parentPID, String childPID,
            Filter filter, Sorter sorter, AsyncCallback<Integer> callback);

    /**
     * get sub elements
     *
     * @param pid the parent id
     * @param focusOnChild the pid of the first item to fetch
     * @param count the maximum number of items to fetch
     * @param filter
     * @param sorter
     * @return the sub elements list
     * @throws RODAException
     */
    public void getSubElements(String pid,
            String focusOnChild, int count, Filter filter, Sorter sorter, AsyncCallback<SimpleDescriptionObject[]> callback);

    /**
     * Get representations information
     *
     * @param pid the id of the associated description object
     * @return the list of representation informations
     * @throws RODAException
     */
    public void getRepresentationsInfo(String doPID, AsyncCallback<List<RepresentationInfo>> callback);

    /**
     * Get the Representation Preservation Objects associated with a Descriptive
     * Object
     *
     * @param doPID the Description Object PID
     * @return The list of associated Representation Preservation Objects
     * @throws RODAException
     */
    public void getDOPreservationObjects(
            String doPID, AsyncCallback<List<RepresentationPreservationObject>> callback);

    /**
     * Get the preservation information
     *
     * @param doPID the PID of the associated description object
     * @return A list of preservations information
     * @throws RODAException
     */
    public void getPreservationsInfo(String doPID, AsyncCallback<List<PreservationInfo>> callback);

    /**
     * Get preservation timeline info
     *
     * @param repPIDs the PIDs of the representations to show
     * @param icons the icons to use in each representation, by order
     * @param colors the colors to use in each representation, by order
     * @param locale
     * @return {@link TimelineInfo}
     * @throws RODAException
     */
    public void getPreservationTimeline(List<String> repPIDs,
            List<String> icons, List<String> colors, String locale, AsyncCallback<TimelineInfo> callback);    
}
