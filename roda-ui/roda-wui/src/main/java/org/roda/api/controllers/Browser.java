package org.roda.api.controllers;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.StreamingOutput;

import org.roda.common.UserUtility;
import org.roda.model.DescriptiveMetadata;
import org.roda.model.ModelServiceException;
import org.roda.storage.StorageServiceException;

import pt.gov.dgarq.roda.common.RodaCoreService;
import pt.gov.dgarq.roda.core.common.AuthorizationDeniedException;
import pt.gov.dgarq.roda.core.common.Pair;
import pt.gov.dgarq.roda.core.common.RODAException;
import pt.gov.dgarq.roda.core.data.adapter.facet.Facets;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.core.data.v2.RodaUser;
import pt.gov.dgarq.roda.core.data.v2.SimpleDescriptionObject;
import pt.gov.dgarq.roda.wui.common.client.GenericException;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.BrowseItemBundle;

/**
 * FIXME 1) verify all checkObject*Permissions (because now also a permission
 * for insert is available)
 */
public class Browser extends RodaCoreService {

  private Browser() {
    super();
  }

  public static BrowseItemBundle getItemBundle(RodaUser user, String aipId, String localeString)
    throws AuthorizationDeniedException, GenericException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, "browse");

    // delegate
    BrowseItemBundle itemBundle = BrowserHelper.getItemBundle(aipId, localeString);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, "Browser", "getItemBundle", aipId, duration, "aipId", aipId);

    return itemBundle;
  }

  public static IndexResult<SimpleDescriptionObject> findDescriptiveMetadata(RodaUser user, Filter filter,
    Sorter sorter, Sublist sublist, Facets facets) throws RODAException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, "browse");

    // delegate
    IndexResult<SimpleDescriptionObject> descriptiveMetadata = BrowserHelper.findDescriptiveMetadata(filter, sorter,
      sublist, facets);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, "Browser", "findDescriptiveMetadata", null, duration, "filter", filter.toString(), "sorter",
      sorter.toString(), "sublist", sublist.toString());

    return descriptiveMetadata;
  }

  public static Long countDescriptiveMetadata(RodaUser user, Filter filter)
    throws AuthorizationDeniedException, GenericException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, "browse");

    // delegate
    Long count = BrowserHelper.countDescriptiveMetadata(filter);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, "Browser", "countDescriptiveMetadata", null, duration, "filter", filter.toString());

    return count;
  }

  public static SimpleDescriptionObject getSimpleDescriptionObject(RodaUser user, String aipId)
    throws AuthorizationDeniedException, GenericException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, "browse");

    // delegate
    SimpleDescriptionObject sdo = BrowserHelper.getSimpleDescriptionObject(aipId);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, "Browser", "getSimpleDescriptionObject", aipId, duration, "aipId", aipId);

    return sdo;
  }

  public static List<SimpleDescriptionObject> getAncestors(RodaUser user, SimpleDescriptionObject sdo)
    throws AuthorizationDeniedException, GenericException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, "browse");

    // delegate
    List<SimpleDescriptionObject> ancestors = BrowserHelper.getAncestors(sdo);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, "Browser", "getAncestors", sdo.getId(), duration, "sdo", sdo.toString());

    return ancestors;
  }

  public static Pair<String, StreamingOutput> getAipDescritiveMetadata(RodaUser user, String aipId, String metadataId)
    throws AuthorizationDeniedException, GenericException, ModelServiceException, StorageServiceException {
    Date startDate = new Date();

    // check user permissions
    SimpleDescriptionObject sdo = BrowserHelper.getSimpleDescriptionObject(aipId);
    UserUtility.checkObjectModifyPermissions(user, sdo);

    // delegate
    Pair<String, StreamingOutput> aipsAipIdDescriptiveMetadataMetadataIdGet = BrowserHelper
      .getAipDescritiveMetadata(aipId, metadataId);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, "Browser", "getAipDescritiveMetadata", aipId, duration, "metadataId", metadataId);

    return aipsAipIdDescriptiveMetadataMetadataIdGet;

  }

  public static SimpleDescriptionObject moveInHierarchy(RodaUser user, String aipId, String parentId)
    throws AuthorizationDeniedException, GenericException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, "administration.metadata_editor");
    SimpleDescriptionObject sdo = BrowserHelper.getSimpleDescriptionObject(aipId);
    UserUtility.checkObjectModifyPermissions(user, sdo);
    sdo = BrowserHelper.getSimpleDescriptionObject(parentId);
    UserUtility.checkObjectModifyPermissions(user, sdo);

    // delegate
    sdo = BrowserHelper.moveInHierarchy(aipId, parentId);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, "Browser", "moveInHierarchy", sdo.getId(), duration, "aip", aipId, "toParent", parentId);

    return sdo;

  }

  public static Pair<String, StreamingOutput> getAipRepresentation(RodaUser user, String aipId, String representationId)
    throws AuthorizationDeniedException, GenericException, ModelServiceException, StorageServiceException {
    Date startDate = new Date();

    // check user permissions
    SimpleDescriptionObject sdo = BrowserHelper.getSimpleDescriptionObject(aipId);
    UserUtility.checkObjectReadPermissions(user, sdo);

    // delegate
    Pair<String, StreamingOutput> aipRepresentation = BrowserHelper.getAipRepresentation(aipId, representationId);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, "Browser", "getAipRepresentation", aipId, duration, "aip", aipId, "representationId",
      representationId);

    return aipRepresentation;
  }

  public static Pair<String, StreamingOutput> aipsAipIdPreservationMetadataGet(RodaUser user, String aipId,
    String start, String limit)
      throws AuthorizationDeniedException, GenericException, ModelServiceException, StorageServiceException {
    Date startDate = new Date();

    // check user permissions
    SimpleDescriptionObject sdo = BrowserHelper.getSimpleDescriptionObject(aipId);
    UserUtility.checkObjectReadPermissions(user, sdo);

    // delegate
    Pair<String, StreamingOutput> aipsAipIdPreservationMetadataGet = BrowserHelper
      .aipsAipIdPreservationMetadataGet(aipId, start, limit);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, "Browser", "aipsAipIdPreservationMetadataGet", aipId, duration, "aip", aipId);

    return aipsAipIdPreservationMetadataGet;
  }

  public static Pair<String, StreamingOutput> listAipDescriptiveMetadata(RodaUser user, String aipId, String start,
    String limit)
      throws AuthorizationDeniedException, GenericException, ModelServiceException, StorageServiceException {
    Date startDate = new Date();

    // check user permissions
    SimpleDescriptionObject sdo = BrowserHelper.getSimpleDescriptionObject(aipId);
    UserUtility.checkObjectReadPermissions(user, sdo);

    // delegate
    Pair<String, StreamingOutput> aipRepresentation = BrowserHelper.listAipDescriptiveMetadata(aipId, start, limit);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, "Browser", "listAipDescriptiveMetadata", aipId, duration, "aip", aipId);

    return aipRepresentation;
  }

  public static SimpleDescriptionObject createNewItem(RodaUser user, String itemId, String parentId)
    throws AuthorizationDeniedException, GenericException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, "administration.metadata_editor");
    SimpleDescriptionObject parentSDO = BrowserHelper.getSimpleDescriptionObject(parentId);
    UserUtility.checkObjectModifyPermissions(user, parentSDO);

    // delegate
    SimpleDescriptionObject sdo = BrowserHelper.createNewItem(user, itemId, parentId);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, "Browser", "createNewItem", sdo.getId(), duration, "itemId", itemId, "parentId", parentId);

    return sdo;

  }

  public static SimpleDescriptionObject addNewMetadataFile(RodaUser user, String itemId, InputStream metadataStream,
    String descriptiveMetadataId) throws AuthorizationDeniedException, GenericException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, "administration.metadata_editor");
    SimpleDescriptionObject sdo = BrowserHelper.getSimpleDescriptionObject(itemId);
    UserUtility.checkObjectModifyPermissions(user, sdo);

    // delegate
    sdo = BrowserHelper.addNewMetadataFile(itemId, metadataStream, descriptiveMetadataId);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, "Browser", "addNewMetadataFile", sdo.getId(), duration, "itemId", itemId,
      "descriptiveMetadataId", descriptiveMetadataId);

    return sdo;
  }

  public static SimpleDescriptionObject editMetadataFile(RodaUser user, String itemId, InputStream metadataStream,
    String descriptiveMetadataId) throws AuthorizationDeniedException, GenericException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, "administration.metadata_editor");
    SimpleDescriptionObject sdo = BrowserHelper.getSimpleDescriptionObject(itemId);
    UserUtility.checkObjectModifyPermissions(user, sdo);

    // delegate
    sdo = BrowserHelper.editMetadataFile(itemId, metadataStream, descriptiveMetadataId);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, "Browser", "editMetadataFile", sdo.getId(), duration, "itemId", itemId,
      "descriptiveMetadataId", descriptiveMetadataId);

    return sdo;
  }

  public static SimpleDescriptionObject removeMetadataFile(RodaUser user, String itemId, String descriptiveMetadataId)
    throws AuthorizationDeniedException, GenericException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, "administration.metadata_editor");
    SimpleDescriptionObject sdo = BrowserHelper.getSimpleDescriptionObject(itemId);
    UserUtility.checkObjectModifyPermissions(user, sdo);

    // delegate
    sdo = BrowserHelper.removeMetadataFile(itemId, descriptiveMetadataId);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, "Browser", "removeMetadataFile", sdo.getId(), duration, "itemId", itemId,
      "descriptiveMetadataId", descriptiveMetadataId);

    return sdo;
  }

  public static DescriptiveMetadata retrieveMetadataFile(RodaUser user, String itemId, String descriptiveMetadataId)
    throws AuthorizationDeniedException, GenericException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, "administration.metadata_editor");
    SimpleDescriptionObject sdo = BrowserHelper.getSimpleDescriptionObject(itemId);
    UserUtility.checkObjectModifyPermissions(user, sdo);

    // delegate
    DescriptiveMetadata dm = BrowserHelper.retrieveMetadataFile(itemId, descriptiveMetadataId);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, "Browser", "retrieveMetadataFile", itemId, duration, "itemId", itemId, "descriptiveMetadataId",
      descriptiveMetadataId);

    return dm;
  }

}
