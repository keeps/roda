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
package org.roda.wui.client.browse;

import java.util.List;

import org.roda.core.data.DescriptionObject;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RODAException;
import org.roda.core.data.v2.IndexResult;
import org.roda.core.data.v2.RepresentationPreservationObject;
import org.roda.core.data.v2.SimpleDescriptionObject;
import org.roda.core.data.v2.SimpleFile;
import org.roda.core.data.v2.TransferredResource;
import org.roda.wui.client.search.SearchField;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Luis Faria
 * 
 */
public interface BrowserServiceAsync {

  void countDescriptiveMetadata(Filter filter, AsyncCallback<Long> callback);

  void findDescriptiveMetadata(Filter filter, Sorter sorter, Sublist sublist, Facets facets, String locale,
    AsyncCallback<IndexResult<SimpleDescriptionObject>> callback);

  void getItemBundle(String aipId, String localeString, AsyncCallback<BrowseItemBundle> callback);

  void getDescriptiveMetadataEditBundle(String aipId, String descId,
    AsyncCallback<DescriptiveMetadataEditBundle> callback);

  /**
   * Get simple description object
   * 
   * @param pid
   *          the object id
   * @return {@link SimpleDescriptionObject}
   * @throws RODAException
   */
  public void getSimpleDescriptionObject(String pid, AsyncCallback<SimpleDescriptionObject> callback);

  /**
   * Get description object
   * 
   * @param pid
   *          the object id
   * @return {@link DescriptionObject}
   * @throws RODAException
   */
  public void getDescriptionObject(String pid, AsyncCallback<DescriptionObject> callback);

  /**
   * Get the pid of all ancestors of the node.
   * 
   * @param pid
   *          the pid of the node
   * @return A array that starts in the fonds of witch this node belongs to, and
   *         ends in the node itself
   * @throws RODAException
   */
  public void getAncestors(SimpleDescriptionObject sdo, AsyncCallback<List<SimpleDescriptionObject>> callback);

  void getSearchFields(String locale, AsyncCallback<List<SearchField>> callback);

  /**
   * Get the index of a collection
   * 
   * @param collectionPID
   *          the collection id
   * @param filter
   * @param sorter
   * @return the index of the collection
   * @throws RODAException
   */
  // public void getCollectionIndex(String collectionPID, Filter filter,
  // Sorter sorter, AsyncCallback<Integer> callback);

  /**
   * Get an item index
   * 
   * @param parentPID
   *          the parent pid
   * @param childPID
   *          the item pid
   * @param filter
   * @param sorter
   * @return the item index
   * @throws RODAException
   */
  // public void getItemIndex(String parentPID, String childPID, Filter
  // filter, Sorter sorter,
  // AsyncCallback<Integer> callback);

  /**
   * get sub elements
   * 
   * @param pid
   *          the parent id
   * @param focusOnChild
   *          the pid of the first item to fetch
   * @param count
   *          the maximum number of items to fetch
   * @param filter
   * @param sorter
   * @return the sub elements list
   * @throws RODAException
   */
  // public void getSubElements(String pid, String focusOnChild, int count,
  // Filter filter, Sorter sorter,
  // AsyncCallback<SimpleDescriptionObject[]> callback);

  /**
   * Get representations information
   * 
   * @param pid
   *          the id of the associated description object
   * @return the list of representation informations
   * @throws RODAException
   */
  public void getRepresentationsInfo(String doPID, AsyncCallback<List<RepresentationInfo>> callback);

  /**
   * Get the Representation Preservation Objects associated with a Descriptive
   * Object
   * 
   * @param doPID
   *          the Description Object PID
   * @return The list of associated Representation Preservation Objects
   * @throws RODAException
   */
  public void getDOPreservationObjects(String doPID, AsyncCallback<List<RepresentationPreservationObject>> callback);

  /**
   * Get the preservation information
   * 
   * @param doPID
   *          the PID of the associated description object
   * @return A list of preservations information
   * @throws RODAException
   */
  public void getPreservationsInfo(String doPID, AsyncCallback<List<PreservationInfo>> callback);

  /**
   * Get preservation timeline info
   * 
   * @param repPIDs
   *          the PIDs of the representations to show
   * @param icons
   *          the icons to use in each representation, by order
   * @param colors
   *          the colors to use in each representation, by order
   * @param locale
   * @return {@link TimelineInfo}
   * @throws RODAException
   */
  public void getPreservationTimeline(List<String> repPIDs, List<String> icons, List<String> colors, String locale,
    AsyncCallback<TimelineInfo> callback);

  public void moveInHierarchy(String aipId, String parentId, AsyncCallback<SimpleDescriptionObject> callback);

  void createAIP(String parentId, AsyncCallback<String> callback);

  void removeAIP(String aipId, AsyncCallback<Void> callback);

  void updateDescriptiveMetadataFile(String aipId, DescriptiveMetadataEditBundle bundle, AsyncCallback<Void> callback);

  public void removeDescriptiveMetadataFile(String itemId, String descriptiveMetadataId, AsyncCallback<Void> callback);

  void createDescriptiveMetadataFile(String aipId, DescriptiveMetadataEditBundle newBundle,
    AsyncCallback<Void> asyncCallback);

  // public void retrieveMetadataFile(String itemId, String
  // descriptiveMetadataId,
  // AsyncCallback<DescriptiveMetadata> callback);

  void findTransferredResources(Filter filter, Sorter sorter, Sublist sublist, Facets facets,
    AsyncCallback<IndexResult<TransferredResource>> callback);

  void retrieveTransferredResource(String transferredResourceId, AsyncCallback<TransferredResource> callback);

  void createTransferredResourcesFolder(String parent, String folderName, AsyncCallback<String> callback);

  void removeTransferredResources(List<String> ids, AsyncCallback<Void> callback);

  void isTransferFullyInitialized(AsyncCallback<Boolean> callback);

  void getRepresentationFiles(Filter filter, Sorter sorter, Sublist sublist, Facets facets, String localeString,
    AsyncCallback<IndexResult<SimpleFile>> callback);
}
